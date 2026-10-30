package com.app.control

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalDate

@Composable
internal fun SharedFilters(ledger: Ledger, filter: AnalysisFilter, change: (AnalysisFilter)->Unit, showAccount: Boolean = true) {
    var configure by remember { mutableStateOf(false) }
    if(showAccount) Choice("Cuenta",ledger.accounts.firstOrNull { it.id==filter.account }?.name ?: "Todas las cuentas",listOf("Todas las cuentas")+ledger.accounts.map { it.name }) { change(filter.copy(account=if(it==0) null else ledger.accounts[it-1].id)) }
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            CompactFilter(filter.category ?: "Todas las categorías",listOf("Todas las categorías")+expenseCategories,Modifier.weight(1.2f)) { change(filter.copy(category=if(it==0) null else expenseCategories[it-1])) }
            CompactFilter(filter.cadence.title,Cadence.entries.map { it.title },Modifier.weight(1f)) { change(filter.copy(cadence=Cadence.entries[it],viewDate=null)); if(Cadence.entries[it]!=Cadence.MONTHLY) configure=true }
        }
        val range=filter.range()
        fun navigate(forward: Boolean) {
            if(filter.cadence==Cadence.CUSTOM) {
                val days=java.time.temporal.ChronoUnit.DAYS.between(range.start,range.endInclusive)+1
                val shift=if(forward) days else -days
                change(filter.copy(customStart=range.start.plusDays(shift),customEnd=range.endInclusive.plusDays(shift)))
            } else change(filter.copy(viewDate=if(forward) range.endInclusive.plusDays(1) else range.start.minusDays(1)))
        }
        Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=1.dp) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={ navigate(false) }) { Text("‹",color=Muted,fontSize=24.sp,modifier=Modifier.semantics { contentDescription="Periodo anterior" }) }
                TextButton(onClick={ configure=true },modifier=Modifier.weight(1f),contentPadding=PaddingValues(0.dp)) {
                    val format=DateTimeFormatter.ofPattern("d MMM yyyy",Locale.forLanguageTag("es-MX"))
                    Text("${range.start.format(format)} – ${range.endInclusive.format(format)}",color=Ink,fontSize=11.sp,maxLines=1)
                }
                IconButton(onClick={ navigate(true) }) { Text("›",color=Muted,fontSize=24.sp,modifier=Modifier.semantics { contentDescription="Periodo siguiente" }) }
            }
        }
    }
    if(configure) {
        var anchor by remember { mutableStateOf(filter.anchor.toString()) }; var start by remember { mutableStateOf(filter.customStart.toString()) }; var end by remember { mutableStateOf(filter.customEnd.toString()) }
        var first by remember { mutableStateOf(filter.firstDay.toString()) }; var second by remember { mutableStateOf(filter.secondDay.toString()) }; var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(onDismissRequest={ configure=false },title={ Text("Preferencias del periodo") },text={ Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Fechas aaaa-mm-dd. La referencia inicia el ciclo semanal o catorcenal.")
            OutlinedTextField(anchor,{ anchor=it },label={ Text("Fecha de referencia") })
            OutlinedTextField(first,{ first=it },label={ Text("Inicio primera quincena (1–28)") }); OutlinedTextField(second,{ second=it },label={ Text("Inicio segunda quincena (hasta 31)") })
            OutlinedTextField(start,{ start=it },label={ Text("Desde (personalizado)") }); OutlinedTextField(end,{ end=it },label={ Text("Hasta (personalizado)") })
            error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        } },confirmButton={ TextButton(onClick={ runCatching { val a=first.toInt(); val b=second.toInt(); require(a in 1..28 && b in (a+1)..31); val s=LocalDate.parse(start); val e=LocalDate.parse(end); require(s<=e); change(filter.copy(anchor=LocalDate.parse(anchor),firstDay=a,secondDay=b,customStart=s,customEnd=e)); configure=false }.onFailure { error="Revisa las fechas y los días del periodo." } }) { Text("Aplicar") } },dismissButton={ TextButton(onClick={ configure=false }) { Text("Cancelar") } })
    }
}

@Composable
private fun CompactFilter(label: String, options: List<String>, modifier: Modifier, select: (Int)->Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(onClick={ open=true },shape=RoundedCornerShape(15.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=1.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(label,modifier=Modifier.weight(1f),fontSize=11.sp,color=Ink,maxLines=1,overflow=TextOverflow.Ellipsis)
                Glyph("chevronDown",Ink,Modifier.padding(start=6.dp).size(16.dp))
            }
        }
        DropdownMenu(expanded=open,onDismissRequest={ open=false }) {
            options.forEachIndexed { index,value -> DropdownMenuItem(text={ Text(value) },onClick={ select(index); open=false }) }
        }
    }
}
fun Ledger.analysisSummary(filter: AnalysisFilter): MonthlySummary {
    val range=filter.range(); val days=java.time.temporal.ChronoUnit.DAYS.between(range.start,range.endInclusive)+1
    val current=movements.filter(filter::matches)
    val previous=movements.filter { it.date>=range.start.minusDays(days) && it.date<range.start && (filter.account==null || it.account==filter.account || it.destination==filter.account) && (filter.category==null || it.category==filter.category) }
    fun List<Movement>.sum(type: Operation)=filter { it.type==type }.fold(0L) { sum,m -> Math.addExact(sum,m.cents) }
    fun bars(type: Operation)=(0..7).map { index -> current.filter { (java.time.temporal.ChronoUnit.DAYS.between(range.start,it.date)*8/days).toInt()==index }.sum(type) }
    return MonthlySummary(current.sum(Operation.INCOME),current.sum(Operation.EXPENSE),previous.sum(Operation.INCOME),previous.sum(Operation.EXPENSE),bars(Operation.INCOME),bars(Operation.EXPENSE),current.filter { it.type==Operation.EXPENSE }.groupBy { it.category }.map { it.key to it.value.sum(Operation.EXPENSE) }.sortedByDescending { it.second })
}

@Composable
internal fun AccountSelector(ledger: Ledger, filter: AnalysisFilter, change: (AnalysisFilter)->Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick={ open=true }) { Text(ledger.accounts.firstOrNull { it.id==filter.account }?.name ?: "Todas las cuentas",color=androidx.compose.ui.graphics.Color.White); Glyph("chevronDown",androidx.compose.ui.graphics.Color.White,Modifier.size(16.dp)) }
        DropdownMenu(expanded=open,onDismissRequest={ open=false }) {
            DropdownMenuItem(text={ Text("Todas las cuentas") },onClick={ change(filter.copy(account=null)); open=false })
            ledger.accounts.forEach { a -> DropdownMenuItem(text={ Text(a.name) },onClick={ change(filter.copy(account=a.id)); open=false }) }
        }
    }
}
