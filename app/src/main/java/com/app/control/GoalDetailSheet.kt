package com.app.control

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
internal fun GoalDetailSheet(ledger: Ledger,goal: SavingsGoal,onClose: () -> Unit,onEdit: () -> Unit,onArchive: () -> Unit,onAdd: () -> Unit,onWithdraw: () -> Unit) {
    val saved=ledger.goalBalance(goal.id)
    val colors=MaterialTheme.colorScheme
    val dateFormat=DateTimeFormatter.ofPattern("dd/MM/yyyy")
    FormDialog(onDismissRequest=onClose,title={ Text(goal.name) },subtitle="Consulta tu progreso y administra los fondos de tu apartado.",icon=goal.icon,
        text={ Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Surface(shape=RoundedCornerShape(18.dp),color=colors.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("${percent(saved,goal.target)} de tu meta",color=colors.onPrimaryContainer,fontWeight=FontWeight.SemiBold,fontSize=13.sp)
                    Text(money(saved),fontSize=28.sp,fontWeight=FontWeight.Bold,color=colors.onPrimaryContainer)
                    Text("de ${money(goal.target)}",fontSize=13.sp,color=colors.onPrimaryContainer)
                    LinearProgressIndicator(progress={ if(goal.target<=0) 0f else (saved.toDouble()/goal.target).toFloat().coerceIn(0f,1f) },modifier=Modifier.fillMaxWidth().height(7.dp),color=colors.primary,trackColor=colors.surface.copy(alpha=.55f))
                    Text(if(saved>=goal.target) "¡Meta alcanzada!" else "Faltan ${money((goal.target-saved).coerceAtLeast(0))}",fontSize=13.sp,color=colors.onPrimaryContainer)
                    goal.deadline?.let { DetailLine("calendar","Fecha de la meta",it.format(dateFormat)) }
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick=onEdit,modifier=Modifier.weight(1f)) { Glyph("edit",colors.primary,Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Editar meta",fontSize=12.sp) }
                OutlinedButton(onClick=onArchive,modifier=Modifier.weight(1f)) { Glyph("archive",colors.primary,Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Archivar",fontSize=12.sp) }
            }
            val accounts=ledger.accounts.filter { ledger.goalBalance(goal.id,it.id)>0 }
            if(accounts.isNotEmpty()) DetailGroup("Fondos por cuenta") {
                accounts.forEach { DetailLine("bank",it.name,money(ledger.goalBalance(goal.id,it.id))) }
            }
            val plans=ledger.plans.filter { it.goal==goal.id && it.status==PlanStatus.PENDING }
            if(plans.isNotEmpty()) DetailGroup("Aportaciones previstas") {
                plans.sortedBy { it.date }.forEach { DetailLine("calendar",it.date.format(dateFormat),money(it.cents)) }
            }
            DetailGroup("Movimientos del apartado") {
                val movements=ledger.movements.filter { it.goal==goal.id }.sortedByDescending { it.date }
                if(movements.isEmpty()) Text("Aún no hay movimientos. Agrega fondos para comenzar.",fontSize=13.sp,color=colors.onSurfaceVariant)
                movements.forEach { movement ->
                    val style=movement.type.iconStyle()
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Glyph(style.icon,style.color,Modifier.size(23.dp))
                        Column(Modifier.weight(1f)) {
                            Text(movement.type.label(),fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                            Text(movement.date.format(dateFormat),fontSize=11.sp,color=colors.onSurfaceVariant)
                        }
                        Text(money(movement.cents),fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                    }
                }
            }
            TextButton(onClick=onClose,modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("Cerrar") }
        } },
        confirmButton={ Button(onClick=onAdd,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Glyph("plus",colors.onPrimary,Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("Agregar fondos",fontSize=12.sp) } },
        dismissButton={ OutlinedButton(onClick=onWithdraw,enabled=saved>0,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Glyph("withdraw",if(saved>0) colors.primary else colors.onSurface.copy(alpha=.38f),Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("Retirar fondos",fontSize=12.sp) } })
}

@Composable
private fun DetailGroup(title: String,content: @Composable ColumnScope.() -> Unit) {
    Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(title,fontSize=14.sp,fontWeight=FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DetailLine(icon: String,label: String,value: String) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Glyph(icon,MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(20.dp))
        Text(label,modifier=Modifier.weight(1f),fontSize=12.sp)
        Text(value,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
}
