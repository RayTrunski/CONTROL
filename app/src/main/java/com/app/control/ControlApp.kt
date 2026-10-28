package com.app.control

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
fun ControlApp() {
    var ledger by remember { mutableStateOf(Ledger()) }
    var section by rememberSaveable { mutableStateOf(Section.HOME) }
    var alias by rememberSaveable { mutableStateOf("") }
    var operation by remember { mutableStateOf<Operation?>(null) }
    var operationGoal by remember { mutableStateOf<String?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var analysis by remember { mutableStateOf(false) }
    var profileOpen by remember { mutableStateOf(false) }
    var accountOpen by remember { mutableStateOf(false) }
    var newGoal by remember { mutableStateOf(false) }
    var detailGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var detailMovement by remember { mutableStateOf<Movement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    BackHandler(settings) { settings=false }
    Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={ if(!settings) BottomBar(section) { section=it; error=null } }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if(settings) Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                TextButton(onClick={ settings=false }) { Text("‹ Volver") }
                Text("Ajustes y perfil",fontSize=26.sp,fontWeight=FontWeight.Bold,color=Ink)
                Text(if(alias.isBlank()) "Tu perfil local" else "Hola, $alias",fontWeight=FontWeight.SemiBold)
                OutlinedButton(onClick={ profileOpen=true }) { Text("Editar nombre") }
                Text("Mis cuentas",fontSize=18.sp,fontWeight=FontWeight.SemiBold)
                if(ledger.accounts.isEmpty()) Text("Aún no tienes cuentas.",color=Muted)
                ledger.accounts.forEach { a ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(a.name,fontWeight=FontWeight.SemiBold)
                            Text("Saldo: ${money(ledger.balance(a.id))}")
                            Text("Disponible: ${money(ledger.available(a.id))}",color=Muted,fontSize=13.sp)
                        }
                    }
                }
                Button(onClick={ accountOpen=true }) { Text("Crear cuenta") }
                Text("Moneda: MXN · Formato: México",color=Muted,fontSize=13.sp)
                Text("Versión de desarrollo. Los datos de esta sesión aún no se conservan al cerrar o recrear la aplicación.",color=Muted,fontSize=12.sp)
            } else when(section) {
                Section.HOME -> HomeScreen(ledger,alias,{ settings=true },{ if(it==Operation.RESERVE) newGoal=true else { operationGoal=null; operation=it } },{ analysis=true },{ section=Section.MOVEMENTS },{ detailMovement=it })
                Section.MOVEMENTS -> MovementsScreen(ledger,{ settings=true },{ operationGoal=null; operation=Operation.EXPENSE },{ detailMovement=it })
                Section.SAVINGS -> SavingsScreen(ledger,{ settings=true },{ newGoal=true },{ detailGoal=it; error=null })
            }
        }
    }
    operation?.let { selected -> MovementDialog(ledger,selected,operationGoal,{ operation=null; operationGoal=null }) { ledger=it; operation=null; operationGoal=null } }
    if(profileOpen) TextEntryDialog("Tu perfil","Nombre o alias (opcional)",alias,{ profileOpen=false }) { alias=it.trim(); profileOpen=false }
    if(accountOpen) TextEntryDialog("Nueva cuenta","Nombre de la cuenta","",{ accountOpen=false }) { ledger=ledger.addAccount(it); accountOpen=false }
    if(newGoal) NewGoalDialog({ newGoal=false }) { goal -> ledger=ledger.addGoal(goal); newGoal=false; detailGoal=goal }
    detailGoal?.let { goal ->
        val saved=ledger.goalBalance(goal.id)
        AlertDialog(onDismissRequest={ detailGoal=null },title={ Text(goal.name) },text={
            Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("${money(saved)} / ${money(goal.target)}",fontWeight=FontWeight.SemiBold)
                Text("${percent(saved,goal.target)} de tu meta",color=Green)
                Text("Faltan ${money((goal.target-saved).coerceAtLeast(0))}",color=Muted)
                goal.deadline?.let { Text("Meta: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",fontSize=13.sp) }
                ledger.accounts.filter { ledger.goalBalance(goal.id,it.id)>0 }.forEach { Text("${it.name}: ${money(ledger.goalBalance(goal.id,it.id))}",fontSize=13.sp) }
                Button(onClick={ operationGoal=goal.id; operation=Operation.RESERVE; detailGoal=null }) { Text("Agregar fondos") }
                OutlinedButton(onClick={ operationGoal=goal.id; operation=Operation.RELEASE; detailGoal=null },enabled=saved>0) { Text("Retirar fondos") }
            }
        },confirmButton={ TextButton(onClick={ detailGoal=null }) { Text("Cerrar") } })
    }
    detailMovement?.let { movement ->
        AlertDialog(onDismissRequest={ detailMovement=null; error=null },title={ Text(movement.description.ifBlank { movement.type.label() }) },text={
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(movement.signedAmount(),fontSize=24.sp,fontWeight=FontWeight.SemiBold)
                Text(movement.type.label())
                Text(movement.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                Text("Cuenta: ${ledger.accounts.first { it.id==movement.account }.name}")
                movement.destination?.let { id -> Text("Destino: ${ledger.accounts.first { it.id==id }.name}") }
                if(movement.type==Operation.EXPENSE) Text("Categoría: ${movement.category}")
                movement.goal?.let { id -> Text("Apartado: ${ledger.goals.first { it.id==id }.name}") }
                error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            }
        },confirmButton={ TextButton(onClick={ detailMovement=null; error=null }) { Text("Cerrar") } },dismissButton={ TextButton(onClick={ runCatching { ledger.delete(movement.id) }.onSuccess { ledger=it; detailMovement=null; error=null }.onFailure { error=it.message } }) { Text("Eliminar") } })
    }
    if(analysis) AlertDialog(onDismissRequest={ analysis=false },title={ Text("Gastos del mes") },text={ CategoryChart(ledger.monthlySummary()) },confirmButton={ TextButton(onClick={ analysis=false }) { Text("Cerrar") } })
}
