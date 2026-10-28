package com.app.control

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MexicanLocale=Locale.forLanguageTag("es-MX")
internal fun percent(value: Long,total: Long): String = if(total<=0) "0%" else BigDecimal.valueOf(value).multiply(BigDecimal(100)).divide(BigDecimal.valueOf(total),0,RoundingMode.HALF_UP).toPlainString()+"%"
private fun ratio(value: Long,total: Long): Float = if(total<=0) 0f else BigDecimal.valueOf(value).divide(BigDecimal.valueOf(total),6,RoundingMode.HALF_UP).toFloat().coerceIn(0f,1f)
@Composable
internal fun ScreenHeader(title: String,subtitle: String,onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title,fontSize=24.sp,fontWeight=FontWeight.Bold,color=Ink)
            Text(subtitle,fontSize=12.sp,color=Muted)
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFECEEF3)).clickable(onClick=onSettings).semantics { contentDescription="Abrir ajustes y perfil" },contentAlignment=Alignment.Center) {
            Glyph("user",Ink,Modifier.size(27.dp))
            Box(Modifier.align(Alignment.BottomEnd).padding(4.dp).size(8.dp).background(Green,CircleShape))
        }
    }
}
private fun Movement.visual(): Pair<String,Color> = when(type) {
    Operation.INCOME -> "bag" to Green
    Operation.TRANSFER -> "transfer" to Color(0xFF3478FF)
    Operation.RESERVE,Operation.RELEASE -> "pig" to Color(0xFFFF931C)
    Operation.EXPENSE -> when(category) {
        "Alimentación" -> "cart" to Green
        "Transporte" -> "car" to Color(0xFF3478FF)
        "Servicios" -> "bulb" to Color(0xFFA34FFF)
        "Vivienda" -> "home" to Color(0xFFFF931C)
        "Salud" -> "plus" to Color(0xFFFF5276)
        else -> "minus" to Color(0xFFFF931C)
    }
}
internal fun Movement.signedAmount(): String = when(type) {
    Operation.INCOME -> "+ ${money(cents)}"
    Operation.EXPENSE -> "- ${money(cents)}"
    else -> money(cents)
}
@Composable
internal fun MovementRow(ledger: Ledger,m: Movement,hidden: Boolean=false,compact: Boolean=false,onClick: () -> Unit) {
    val (icon,color)=m.visual()
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick=onClick).padding(vertical=if(compact) 7.dp else 12.dp,horizontal=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(if(compact) 36.dp else 44.dp).background(color.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center) { Glyph(icon,color,Modifier.size(24.dp)) }
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(m.description.ifBlank { m.type.label() },fontSize=if(compact) 12.sp else 13.sp,fontWeight=FontWeight.SemiBold,color=Ink,maxLines=1,overflow=TextOverflow.Ellipsis)
            val account=ledger.accounts.firstOrNull { it.id==m.account }?.name.orEmpty()
            val label=if(m.type==Operation.EXPENSE) m.category else m.type.label()
            Text("$label · $account",fontSize=10.sp,color=Muted,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment=Alignment.End,verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(if(hidden) "••••" else m.signedAmount(),fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=if(m.type==Operation.INCOME) Green else Ink)
            Text(m.date.format(DateTimeFormatter.ofPattern("dd MMM",MexicanLocale)),fontSize=10.sp,color=Muted)
        }
    }
}

internal fun Ledger.filteredMovements(query: String,type: Operation?,month: YearMonth?,account: String?,category: String?): List<Movement> = movements.asReversed().filter { m ->
    (type==null || m.type==type) && (month==null || YearMonth.from(m.date)==month) && (account==null || m.account==account || m.destination==account) &&
        (category==null || (m.type==Operation.EXPENSE && m.category==category)) &&
        (query.isBlank() || m.description.contains(query.trim(),true) || m.category.contains(query.trim(),true) || m.type.label().contains(query.trim(),true))
}.sortedByDescending { it.date }

@Composable
internal fun MovementsScreen(ledger: Ledger,onSettings: () -> Unit,onAdd: () -> Unit,onMovement: (Movement) -> Unit) {
    var type by rememberSaveable { mutableStateOf<Operation?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var account by rememberSaveable { mutableStateOf<String?>(null) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var filters by remember { mutableStateOf(false) }
    val month=monthText.takeIf { it.isNotBlank() }?.let { YearMonth.parse(it) }
    val entries=ledger.filteredMovements(query,type,month,account,category)
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=18.dp,bottom=88.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            ScreenHeader("Movimientos","Todos tus ingresos, gastos y transferencias",onSettings)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                listOf("Todos" to null,"Ingresos" to Operation.INCOME,"Gastos" to Operation.EXPENSE,"Transferencias" to Operation.TRANSFER).forEach { (label,value) ->
                    Surface(onClick={ type=value },shape=RoundedCornerShape(10.dp),color=if(type==value) Green else Color(0xFFEBEEF3),modifier=Modifier.semantics { selected=type==value }) {
                        Text(label,Modifier.padding(horizontal=14.dp,vertical=11.dp),fontSize=11.sp,color=if(type==value) Color.White else Muted)
                    }
                }
            }
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(query,{ query=it },Modifier.weight(1f),placeholder={ Text("Buscar movimientos…",fontSize=12.sp) },singleLine=true,shape=RoundedCornerShape(14.dp),leadingIcon={ Glyph("search") },colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=Color.Transparent,focusedBorderColor=Green,unfocusedContainerColor=Color(0xFFEBEEF3)))
                IconButton(onClick={ filters=true },modifier=Modifier.background(if(category==null) Color(0xFFEBEEF3) else Green.copy(alpha=.15f),RoundedCornerShape(12.dp)).semantics { contentDescription="Filtrar movimientos por categoría" }) { Glyph("list",if(category==null) Muted else Green) }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                val months=(listOf(YearMonth.now())+ledger.movements.map { YearMonth.from(it.date) }).distinct().sortedDescending()
                FilterMenu(month?.format(DateTimeFormatter.ofPattern("MMMM yyyy",MexicanLocale)) ?: "Todas las fechas",listOf("Todas las fechas")+months.map { it.format(DateTimeFormatter.ofPattern("MMMM yyyy",MexicanLocale)) },Modifier.weight(1f)) { monthText=if(it==0) "" else months[it-1].toString() }
                FilterMenu(ledger.accounts.firstOrNull { it.id==account }?.name ?: "Todas las cuentas",listOf("Todas las cuentas")+ledger.accounts.map { it.name },Modifier.weight(1f)) { account=if(it==0) null else ledger.accounts[it-1].id }
            }
            if(category!=null) TextButton(onClick={ category=null }) { Text("${category} ×",fontSize=12.sp) }
            if(entries.isEmpty()) Surface(shape=RoundedCornerShape(18.dp),color=Color.White) {
                Column(Modifier.fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("Sin movimientos",fontWeight=FontWeight.SemiBold)
                    Text("Registra una operación o cambia los filtros para consultar tu historial.",color=Muted,fontSize=13.sp)
                }
            }
            entries.groupBy { it.date }.forEach { (date,movements) ->
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",MexicanLocale)).replaceFirstChar { it.titlecase(MexicanLocale) },Modifier.weight(1f),fontSize=12.sp,color=Muted)
                    val net=movements.fold(0L) { sum,m -> Math.addExact(sum,when(m.type) { Operation.INCOME -> m.cents; Operation.EXPENSE -> -m.cents; else -> 0L }) }
                    Text("${if(net>0) "+ " else if(net<0) "- " else ""}${money(if(net<0) Math.negateExact(net) else net)}",fontSize=12.sp,fontWeight=FontWeight.Medium,color=if(net>0) Green else Muted)
                }
                Surface(shape=RoundedCornerShape(16.dp),color=Color.White) {
                    Column(Modifier.padding(horizontal=8.dp)) { movements.forEach { m -> MovementRow(ledger,m) { onMovement(m) } } }
                }
            }
        }
        FloatingActionButton(onClick=onAdd,containerColor=Green,contentColor=Color.White,shape=CircleShape,modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp).semantics { contentDescription="Registrar movimiento" }) { Glyph("plus",Color.White,Modifier.size(28.dp)) }
    }
    if(filters) AlertDialog(onDismissRequest={ filters=false },title={ Text("Filtrar por categoría") },text={
        Column(Modifier.verticalScroll(rememberScrollState())) {
            (listOf("Todas las categorías")+expenseCategories).forEachIndexed { i,label ->
                Row(Modifier.fillMaxWidth().clickable { category=if(i==0) null else label; filters=false },verticalAlignment=Alignment.CenterVertically) {
                    RadioButton(selected=if(i==0) category==null else category==label,onClick={ category=if(i==0) null else label; filters=false })
                    Text(label,fontSize=14.sp)
                }
            }
        }
    },confirmButton={ TextButton(onClick={ filters=false }) { Text("Cerrar") } })
}
@Composable
private fun FilterMenu(label: String,options: List<String>,modifier: Modifier,choose: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(onClick={ expanded=true },shape=RoundedCornerShape(12.dp),color=Color.White,shadowElevation=1.dp) {
            Row(Modifier.fillMaxWidth().padding(11.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(label.replaceFirstChar { it.titlecase(MexicanLocale) },Modifier.weight(1f),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis,color=Ink)
                Text("⌄",color=Muted)
            }
        }
        DropdownMenu(expanded,onDismissRequest={ expanded=false }) { options.forEachIndexed { index,value -> DropdownMenuItem(text={ Text(value) },onClick={ choose(index); expanded=false }) } }
    }
}

private val GoalColors=listOf(Color(0xFF1681FF),Color(0xFFA04EFF),Color(0xFFFF941E),Color(0xFFFF6088))
@Composable
internal fun SavingsScreen(ledger: Ledger,onSettings: () -> Unit,onNew: () -> Unit,onGoal: (SavingsGoal) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=18.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        ScreenHeader("Apartados","Tus objetivos de ahorro",onSettings)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF00964F),Color(0xFF00843E)))).padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Total en apartados",fontSize=13.sp,color=Color.White)
                    Text("${money(ledger.held)} MXN",fontSize=28.sp,fontWeight=FontWeight.SemiBold,color=Color.White)
                }
                Glyph("pig",Color(0xFF77D99A),Modifier.size(48.dp))
            }
            Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("${ledger.goals.count { ledger.goalBalance(it.id)<it.target }} apartados activos",fontSize=12.sp,color=Color.White)
                    ProgressTrack(ratio(ledger.held,ledger.total),Color(0xFFAEF5C6),Color.White.copy(alpha=.25f))
                }
                Column(horizontalAlignment=Alignment.End) {
                    Text(percent(ledger.held,ledger.total),fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Color.White)
                    Text("de tus activos",fontSize=11.sp,color=Color.White)
                }
            }
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("Mis apartados",Modifier.weight(1f),fontSize=19.sp,fontWeight=FontWeight.Bold,color=Ink)
            Button(onClick=onNew,contentPadding=PaddingValues(horizontal=16.dp,vertical=7.dp)) { Glyph("plus",Color.White,Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Nuevo",fontSize=12.sp) }
        }
        if(ledger.goals.isEmpty()) Surface(shape=RoundedCornerShape(20.dp),color=Color.White) {
            Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("Tu próxima meta empieza aquí",fontWeight=FontWeight.SemiBold)
                Text("Crea un apartado, define cuánto quieres ahorrar y agrega fondos desde tus cuentas.",color=Muted,fontSize=13.sp)
            }
        }
        ledger.goals.forEachIndexed { index,goal ->
            val color=GoalColors[index%GoalColors.size]
            val saved=ledger.goalBalance(goal.id)
            Surface(onClick={ onGoal(goal) },shape=RoundedCornerShape(20.dp),color=Color.White) {
                Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(48.dp).background(color.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center) { Glyph(goal.icon,color,Modifier.size(28.dp)) }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text(goal.name,fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=Ink)
                        Row {
                            Text(money(saved),fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Ink)
                            Text(" / ${money(goal.target)}",Modifier.weight(1f),fontSize=12.sp,color=Muted)
                            Text(percent(saved,goal.target),fontSize=12.sp,color=Muted)
                        }
                        ProgressTrack(ratio(saved,goal.target),color,Color(0xFFE8ECF1))
                        Text(if(saved>=goal.target) "¡Meta completada!" else "Meta: ${goal.deadline?.format(DateTimeFormatter.ofPattern("dd MMM yyyy",MexicanLocale)) ?: "Sin fecha"}",fontSize=11.sp,color=Muted)
                    }
                    Text("›",Modifier.align(Alignment.CenterVertically),fontSize=25.sp,color=Muted)
                }
            }
        }
        val unassigned=ledger.held-ledger.goals.fold(0L) { sum,goal -> Math.addExact(sum,ledger.goalBalance(goal.id)) }
        if(unassigned>0) Text("Reservas anteriores sin objetivo: ${money(unassigned)}",fontSize=12.sp,color=Muted)
        Surface(color=Green.copy(alpha=.09f),shape=RoundedCornerShape(16.dp)) {
            Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Glyph("bulb",Green)
                Text("Los apartados te ayudan a reservar dinero para tus metas sin perder el control de tus finanzas.",fontSize=11.sp,color=Muted)
            }
        }
    }
}
@Composable
private fun ProgressTrack(fraction: Float,color: Color,track: Color) {
    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(track)) {
        if(fraction>0) Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(color,CircleShape))
    }
}
