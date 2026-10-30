package com.app.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun PlanningScreen(ledger: Ledger, save: (Ledger)->Unit, back: ()->Unit, onProfile: ()->Unit = back) {
    var month by remember { mutableStateOf(YearMonth.now().plusMonths(1)) }
    var editing by remember { mutableStateOf<PlannedOperation?>(null) }
    var newPlan by remember { mutableStateOf(false) }
    var newSource by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf<PlannedOperation?>(null) }
    var selectedAccount by remember { mutableStateOf(ledger.accounts.firstOrNull()?.id) }
    var error by remember { mutableStateOf<String?>(null) }
    fun attempt(action: ()->Ledger) { runCatching(action).onSuccess(save).onFailure { error=it.message } }
    var type by remember { mutableStateOf<Operation?>(null) }
    var details by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf<String?>(null) }
    val locale=Locale.forLanguageTag("es-MX")
    val monthFormat=DateTimeFormatter.ofPattern("MMMM yyyy",locale)
    val projection=ledger.projection(month)
    val plans=ledger.plansFor(month)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        ScreenHeader("Planificación","Organiza tus ingresos, gastos y metas futuras",onProfile)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={ month=month.minusMonths(1) },enabled=month>YearMonth.now(),modifier=Modifier.semantics { contentDescription="Mes anterior" }) { Text("‹",fontSize=24.sp,color=Muted) }
            Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                (-1L..2L).map { month.plusMonths(it) }.filter { it>=YearMonth.now() }.forEach { item ->
                    Surface(onClick={ month=item },shape=RoundedCornerShape(10.dp),color=if(item==month) Green else MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(horizontal=12.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(item.format(DateTimeFormatter.ofPattern("MMM",locale)).replaceFirstChar { it.titlecase(locale) },fontSize=11.sp,color=if(item==month) Color.White else Muted)
                            Text(item.year.toString(),fontSize=9.sp,color=if(item==month) Color.White else Muted)
                        }
                    }
                }
            }
            }
            IconButton(onClick={ month=month.plusMonths(1) },modifier=Modifier.semantics { contentDescription="Mes siguiente" }) { Text("›",fontSize=24.sp,color=Muted) }
        }
        Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row { Text("Resumen del mes",Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Ink); Text(month.format(monthFormat).replaceFirstChar { it.titlecase(locale) },fontSize=10.sp,color=Muted) }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    PlanningMetric("Ingresos\nprevistos",projection.income,"down",Green,Modifier.weight(1f))
                    PlanningMetric("Gastos\nprevistos",projection.expenses,"up",Color(0xFFFF6383),Modifier.weight(1f))
                    PlanningMetric("Ahorro\nplanificado",projection.contributions,"pig",Color(0xFF438CFF),Modifier.weight(1f))
                    PlanningMetric("Saldo\nproyectado",projection.closing,"list",if(projection.closing<0) MaterialTheme.colorScheme.error else Green,Modifier.weight(1f))
                }
            }
        }
        Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) { Text("Proyección de saldo",Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Ink); TextButton(onClick={ details=true },contentPadding=PaddingValues(0.dp)) { Text("Ver detalle ›",fontSize=10.sp) } }
                ProjectionChart(ledger,month)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("Todos" to null,"Ingresos" to Operation.INCOME,"Gastos" to Operation.EXPENSE,"Ahorros" to Operation.RESERVE).forEach { (label,value) ->
                Surface(onClick={ type=value },modifier=Modifier.weight(1f),shape=RoundedCornerShape(10.dp),color=if(type==value) Green else MaterialTheme.colorScheme.surfaceVariant) {
                    Box(Modifier.padding(vertical=10.dp),contentAlignment=Alignment.Center) { Text(label,fontSize=10.sp,color=if(type==value) Color.White else Muted) }
                }
            }
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("Planificación del mes",Modifier.weight(1f),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Ink)
            Button(onClick={ editing=null; newPlan=true },contentPadding=PaddingValues(horizontal=14.dp,vertical=4.dp)) { Glyph("plus",MaterialTheme.colorScheme.onPrimary,Modifier.size(16.dp)); Text(" Agregar",fontSize=12.sp) }
        }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        val visible=plans.filter { type==null || it.type==type }
        if(visible.isEmpty()) Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface) { Text("Sin operaciones previstas para este mes.",Modifier.fillMaxWidth().padding(20.dp),fontSize=13.sp,color=Muted) }
        visible.groupBy { it.date }.forEach { (date,entries) ->
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",locale)).replaceFirstChar { it.titlecase(locale) },fontSize=10.sp,color=Muted)
            entries.forEach { p ->
                Surface(onClick={ expanded=if(expanded==p.id) null else p.id },shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            val color=p.type.iconStyle().color
                            Box(Modifier.size(36.dp).background(color.copy(alpha=.14f),CircleShape),contentAlignment=Alignment.Center) { Glyph(p.type.iconStyle().icon,color) }
                            Column(Modifier.weight(1f)) {
                                Text(p.description,fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Ink)
                                Text("${if(p.type==Operation.RESERVE) "Ahorro" else p.type.label()} · ${ledger.accounts.firstOrNull { it.id==p.account }?.name ?: "Cuenta por definir"}",fontSize=10.sp,color=Muted)
                                if(p.type==Operation.EXPENSE) Text(p.category,fontSize=10.sp,color=Muted)
                                if(p.type==Operation.RESERVE) Text("Apartado: ${ledger.goals.firstOrNull { it.id==p.goal }?.name ?: "Sin definir"}",fontSize=10.sp,color=Muted)
                            }
                            Column(horizontalAlignment=Alignment.End) {
                                Text("${if(p.type==Operation.INCOME) "+" else "−"}${money(p.cents)}",color=color,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
                                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) { Glyph(p.status.iconName(),Muted,Modifier.size(14.dp)); Text(p.status.title,fontSize=10.sp,color=Muted) }
                            }
                            Text("›",color=Muted,fontSize=20.sp)
                        }
                        if(p.status==PlanStatus.PENDING && expanded==p.id) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                            TextButton(onClick={ editing=p; newPlan=true },contentPadding=PaddingValues(6.dp)) { Glyph("edit",MaterialTheme.colorScheme.primary,Modifier.size(14.dp)); Text("Editar",fontSize=11.sp) }
                            TextButton(onClick={ selectedAccount=p.account ?: ledger.accounts.firstOrNull()?.id; confirming=p },contentPadding=PaddingValues(6.dp)) { Glyph("check",MaterialTheme.colorScheme.primary,Modifier.size(14.dp)); Text("Confirmar",fontSize=11.sp) }
                            TextButton(onClick={ attempt { ledger.cancelPlan(p) } },contentPadding=PaddingValues(6.dp)) { Glyph("cancel",MaterialTheme.colorScheme.primary,Modifier.size(14.dp)); Text("Cancelar",fontSize=11.sp) }
                            TextButton(onClick={ attempt { ledger.removePlan(p) } },contentPadding=PaddingValues(6.dp)) { Glyph("delete",MaterialTheme.colorScheme.primary,Modifier.size(14.dp)); Text("Eliminar",fontSize=11.sp) }
                        }
                    }
                }
            }
        }
        Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Saldo estimado al final de ${month.month.getDisplayName(java.time.format.TextStyle.FULL,locale)}",fontSize=11.sp,color=Ink); Text("Estimación basada en tus operaciones pendientes.",fontSize=9.sp,color=Muted) }
                Text(money(projection.closing),fontWeight=FontWeight.Bold,fontSize=14.sp,color=if(projection.closing<0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
        OutlinedButton(onClick={ newSource=true },modifier=Modifier.fillMaxWidth()) { Text("Agregar fuente de ingreso recurrente",fontSize=12.sp) }
        ledger.sources.forEach { source ->
            Text("${source.name} · ${source.cadence.title} · ${money(source.cents)}",fontSize=12.sp,color=Ink)
            TextButton(onClick={ attempt { ledger.copy(sources=ledger.sources.filterNot { it.id==source.id }) } }) { Text("Eliminar fuente (conserva historial)",fontSize=11.sp) }
        }
    }
    if(details) AlertDialog(onDismissRequest={ details=false },title={ Text("Detalle de la proyección") },text={ Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(month.format(monthFormat))
        Text("Saldo libre inicial proyectado: ${money(projection.opening)}")
        Text("Ingresos previstos: ${money(projection.income)}")
        Text("Gastos previstos: ${money(projection.expenses)}")
        Text("Aportaciones previstas: ${money(projection.contributions)}")
        Text("Saldo libre final proyectado: ${money(projection.closing)}")
        Text("Saldo patrimonial proyectado: ${money(projection.assets)}")
        Text("Las estimaciones no representan dinero recibido ni reservado.",fontSize=12.sp)
    } },confirmButton={ TextButton(onClick={ details=false }) { Text("Cerrar") } })
    if(newPlan) PlanDialog(ledger,month,editing,{ newPlan=false }) { save(ledger.savePlan(it)); newPlan=false }
    if(newSource) SourceDialog(ledger,{ newSource=false }) { save(ledger.addSource(it)); newSource=false }
    confirming?.let { p -> AlertDialog(onDismissRequest={ confirming=null },title={ Text("Confirmar operación real") },text={ Column {
        Text("Se registrará hoy por ${money(p.cents)}.")
        Choice("Cuenta",ledger.accounts.firstOrNull { it.id==selectedAccount }?.name ?: "Seleccionar",ledger.accounts.map { it.name }) { selectedAccount=ledger.accounts[it].id }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
    } },confirmButton={ TextButton(onClick={ runCatching { ledger.confirmPlan(p,selectedAccount ?: error("Crea una cuenta en Ajustes.")) }.onSuccess { save(it); confirming=null; error=null }.onFailure { error=it.message } }) { Text("Confirmar") } },dismissButton={ TextButton(onClick={ confirming=null }) { Text("Volver") } }) }
}
@Composable
private fun PlanningMetric(label: String, value: Long, icon: String, tint: Color, modifier: Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(26.dp).background(tint.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center) { Glyph(icon,tint,Modifier.size(17.dp)) }
        Text(label,fontSize=9.sp,lineHeight=12.sp,color=Muted)
        Text(money(value),fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=if(value<0) MaterialTheme.colorScheme.error else if(icon=="list") MaterialTheme.colorScheme.primary else Ink,maxLines=1)
    }
}

@Composable
private fun ProjectionChart(ledger: Ledger, selected: YearMonth) {
    val start=maxOf(YearMonth.now(),selected.minusMonths(2))
    val months=(0L..5L).map { start.plusMonths(it) }
    val values=months.map { ledger.projection(it) }
    val colors=listOf(Color(0xFF36BD8B),Color(0xFFFF6383),Color(0xFFA75CF5),Color(0xFF3284FF))
    val grid=MaterialTheme.colorScheme.outline.copy(alpha=.25f)
    val upper=values.maxOf { maxOf(it.income,it.expenses,it.contributions,it.closing) }.coerceAtLeast(100L).toDouble()
    val lower=values.minOf { it.closing }.coerceAtMost(0L).toDouble()
    val locale=Locale.forLanguageTag("es-MX")
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row {
            Column(Modifier.width(42.dp).height(112.dp),verticalArrangement=Arrangement.SpaceBetween) {
                Text(money(upper.toLong()),fontSize=8.sp,color=Muted,maxLines=1)
                Text(money(((upper+lower)/2).toLong()),fontSize=8.sp,color=Muted,maxLines=1)
                Text(money(lower.toLong()),fontSize=8.sp,color=Muted,maxLines=1)
            }
            Canvas(Modifier.weight(1f).height(112.dp).semantics { contentDescription=months.zip(values).joinToString("; ") { (month,p) -> "$month: ingresos ${money(p.income)}, gastos ${money(p.expenses)}, ahorro ${money(p.contributions)}, saldo proyectado ${money(p.closing)}" } }) {
                fun y(value: Double)=((upper-value)/(upper-lower)*size.height).toFloat()
                val zero=y(0.0)
                for(i in 0..4) { val position=size.height*i/4; drawLine(grid,Offset(0f,position),Offset(size.width,position),1f) }
                drawLine(grid,Offset(0f,zero),Offset(size.width,zero),2f)
                val step=size.width/6
                val line=Path()
                values.forEachIndexed { index,p ->
                    val center=step*(index+.5f)
                    listOf(p.income,p.expenses,p.contributions).forEachIndexed { bar,value ->
                        val top=y(value.toDouble())
                        drawRoundRect(colors[bar].copy(alpha=.55f),Offset(center+(bar-1)*step*.2f-step*.08f,top),Size(step*.16f,(zero-top).coerceAtLeast(1f)),androidx.compose.ui.geometry.CornerRadius(3f))
                    }
                    val point=y(p.closing.toDouble())
                    if(index==0) line.moveTo(center,point) else line.lineTo(center,point)
                }
                drawPath(line,colors[3],style=Stroke(2.dp.toPx()))
                values.forEachIndexed { index,p -> drawCircle(colors[3],3.dp.toPx(),Offset(step*(index+.5f),y(p.closing.toDouble()))) }
            }
        }
        Row(Modifier.padding(start=42.dp)) { months.forEach { Text(it.format(DateTimeFormatter.ofPattern("MMM",locale)),Modifier.weight(1f),fontSize=9.sp,color=Muted,textAlign=androidx.compose.ui.text.style.TextAlign.Center) } }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            listOf("Ingresos","Gastos","Ahorro","Saldo proyectado").forEachIndexed { i,label ->
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(3.dp)) { Box(Modifier.size(6.dp).background(colors[i],CircleShape)); Text(label,fontSize=8.sp,color=Muted) }
            }
        }
    }
}

@Composable
private fun PlanDialog(ledger: Ledger, month: YearMonth, initial: PlannedOperation?, dismiss: ()->Unit, save: (PlannedOperation)->Unit) {
    var type by remember { mutableStateOf(initial?.type ?: Operation.EXPENSE) }
    var name by remember { mutableStateOf(initial?.description.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.cents?.let { java.math.BigDecimal.valueOf(it,2).toPlainString() }.orEmpty()) }
    var date by remember { mutableStateOf((initial?.date ?: month.atDay(1)).toString()) }
    var account by remember { mutableStateOf(initial?.account) }
    var category by remember { mutableStateOf(initial?.category ?: expenseCategories.first()) }
    var subcategory by remember { mutableStateOf(initial?.subcategory.orEmpty()) }
    var goal by remember { mutableStateOf(initial?.goal ?: ledger.goals.firstOrNull { !it.archived }?.id) }
    var error by remember { mutableStateOf<String?>(null) }
    FormDialog(onDismissRequest=dismiss,icon="calendar",subtitle="Programa una operación y confírmala cuando se realice.",title={ Text("Operación planificada") },text={ Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        val types=listOf(Operation.INCOME,Operation.EXPENSE,Operation.RESERVE)
        OperationPicker(types,type) { type=it }
        FormTextField(name,{ name=it },icon=fieldIcon("Concepto"),label={ Text("Concepto") })
        FormTextField(amount,{ amount=it },icon=fieldIcon("Importe MXN"),label={ Text("Importe MXN") })
        FormTextField(date,{ date=it },icon=fieldIcon("Fecha aaaa-mm-dd"),label={ Text("Fecha aaaa-mm-dd") })
        Choice("Cuenta prevista",ledger.accounts.firstOrNull { it.id==account }?.name ?: "Sin cuenta",listOf("Sin cuenta")+ledger.accounts.map { it.name }) { account=if(it==0) null else ledger.accounts[it-1].id }
        if(type==Operation.EXPENSE) { Choice("Categoría",category,expenseCategories) { category=expenseCategories[it] }; FormTextField(subcategory,{ subcategory=it },icon=fieldIcon("Subcategoría"),label={ Text("Subcategoría") }) }
        if(type==Operation.RESERVE) { val goals=ledger.goals.filterNot { it.archived }; Choice("Apartado",goals.firstOrNull { it.id==goal }?.name ?: "Seleccionar",goals.map { it.name }) { goal=goals[it].id } }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
    } },confirmButton={ Button(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick={ runCatching {
        val day=LocalDate.parse(date); require(day>=LocalDate.now()) { "Selecciona una fecha actual o futura." }
        save(PlannedOperation(id=initial?.id ?: java.util.UUID.randomUUID().toString(),type=type,cents=parseCents(amount),description=name,date=day,account=account,category=category,subcategory=subcategory,goal=if(type==Operation.RESERVE) goal else null,source=initial?.source))
    }.onFailure { error=it.message } }) { Text("Guardar") } },dismissButton={ OutlinedButton(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick=dismiss) { Text("Cancelar") } })
}
@Composable
private fun SourceDialog(ledger: Ledger,dismiss: ()->Unit,save: (IncomeSource)->Unit) {
    var name by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }; var cadence by remember { mutableStateOf(Cadence.MONTHLY) }
    var account by remember { mutableStateOf(ledger.accounts.firstOrNull()?.id) }; var first by remember { mutableStateOf("15") }; var second by remember { mutableStateOf("31") }
    var error by remember { mutableStateOf<String?>(null) }
    FormDialog(onDismissRequest=dismiss,icon="bag",subtitle="Organiza tus cobros previstos y su periodicidad.",title={ Text("Fuente de ingreso") },text={ Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        FormTextField(name,{ name=it },icon=fieldIcon("Nombre"),label={ Text("Nombre") }); FormTextField(amount,{ amount=it },icon=fieldIcon("Importe por cobro MXN"),label={ Text("Importe por cobro MXN") })
        Choice("Periodicidad",cadence.title,Cadence.entries.filter { it!=Cadence.CUSTOM }.map { it.title }) { cadence=Cadence.entries[it] }
        FormTextField(date,{ date=it },icon=fieldIcon("Primer cobro aaaa-mm-dd"),label={ Text("Primer cobro aaaa-mm-dd") })
        if(cadence==Cadence.SEMIMONTHLY) { FormTextField(first,{ first=it },icon=fieldIcon("Primer día de cobro (1–28)"),label={ Text("Primer día de cobro (1–28)") }); FormTextField(second,{ second=it },icon=fieldIcon("Segundo día (31 = fin de mes)"),label={ Text("Segundo día (31 = fin de mes)") }) }
        Choice("Cuenta",ledger.accounts.firstOrNull { it.id==account }?.name ?: "Crea una cuenta en Ajustes",ledger.accounts.map { it.name }) { account=ledger.accounts[it].id }
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
    } },confirmButton={ Button(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick={ runCatching { save(IncomeSource(name=name,cents=parseCents(amount),account=account ?: error("Crea una cuenta primero."),cadence=cadence,anchor=LocalDate.parse(date),firstDay=first.toInt(),secondDay=second.toInt())) }.onFailure { error=it.message } }) { Text("Guardar") } },dismissButton={ OutlinedButton(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick=dismiss) { Text("Cancelar") } })
}

