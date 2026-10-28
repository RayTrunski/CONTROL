package com.app.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

internal val Green = Color(0xFF00964F)
internal val Ink = Color(0xFF111827)
internal val Muted = Color(0xFF687286)
private val Red = Color(0xFFFF536D)
private val CategoryColors = listOf(Color(0xFF42BE79), Color(0xFF4777FF), Color(0xFFFFA23C), Color(0xFFA35BF5), Color(0xFFA9AFBD), Color(0xFF1AB8B0), Color(0xFFED79B4), Color(0xFFCFB34F))
internal fun money(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(BigDecimal.valueOf(cents, 2))
internal fun Operation.label() = when(this) {
    Operation.INCOME -> "Ingreso"
    Operation.EXPENSE -> "Gasto"
    Operation.TRANSFER -> "Transferencia"
    Operation.RESERVE -> "Apartado"
    Operation.RELEASE -> "Liberar apartado"
}

@Composable
internal fun Glyph(kind: String, color: Color = Muted, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 24f, Offset.Zero) {
            fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(color, Offset(x,y), Offset(x2,y2), 1.7f, StrokeCap.Round)
            fun path(vararg points: Float) {
                val p = Path().apply { moveTo(points[0], points[1]); for(i in 2 until points.size step 2) lineTo(points[i], points[i+1]) }
                drawPath(p, color, style = Stroke(1.7f, cap = StrokeCap.Round))
            }
            when(kind) {
                "home" -> { path(3f,11f,12f,3f,21f,11f); drawRoundRect(color, Offset(6f,10f), Size(12f,11f), androidx.compose.ui.geometry.CornerRadius(1f)); drawRect(Color.White, Offset(10f,15f), Size(4f,6f)) }
                "plus", "minus" -> { line(6f,12f,18f,12f); if(kind == "plus") line(12f,6f,12f,18f) }
                "up", "down" -> { line(12f,5f,12f,19f); if(kind == "up") path(7f,10f,12f,5f,17f,10f) else path(7f,14f,12f,19f,17f,14f) }
                "transfer" -> { path(4f,8f,20f,8f,16f,4f); path(20f,16f,4f,16f,8f,20f) }
                "search" -> { drawCircle(color,7f,Offset(10f,10f),style=Stroke(1.7f)); line(15f,15f,21f,21f) }
                "cart" -> { path(2f,3f,5f,3f,8f,16f,19f,16f,22f,7f,6f,7f); drawCircle(color,1.7f,Offset(9f,21f)); drawCircle(color,1.7f,Offset(18f,21f)) }
                "car" -> { path(3f,12f,6f,5f,18f,5f,21f,12f,21f,19f,3f,19f,3f,12f,21f,12f); line(6f,19f,6f,22f); line(18f,19f,18f,22f); line(6f,15f,8f,15f); line(16f,15f,18f,15f) }
                "laptop" -> { path(5f,17f,5f,4f,19f,4f,19f,17f); path(2f,20f,22f,20f,19f,17f,5f,17f,2f,20f) }
                "gift" -> { path(4f,11f,4f,21f,20f,21f,20f,11f); path(2f,7f,22f,7f,22f,11f,2f,11f,2f,7f); line(12f,7f,12f,21f); drawCircle(color,3f,Offset(9f,4f),style=Stroke(1.5f)); drawCircle(color,3f,Offset(15f,4f),style=Stroke(1.5f)) }
                "plane" -> { path(3f,10f,10f,12f,18f,3f,21f,3f,15f,13f,20f,18f,18f,20f,12f,16f,7f,22f,5f,21f,8f,15f,2f,12f,3f,10f) }
                "bulb" -> { drawCircle(color,7f,Offset(12f,9f),style=Stroke(1.7f)); line(8f,15f,9f,20f); line(16f,15f,15f,20f); line(9f,20f,15f,20f); line(10f,23f,14f,23f) }
                "user" -> { drawCircle(color,4f,Offset(12f,7f),style=Stroke(1.6f)); drawArc(color,180f,180f,false,Offset(4f,14f),Size(16f,13f),style=Stroke(1.6f)) }
                "chart" -> { drawCircle(color,9f,Offset(12f,12f),style=Stroke(1.6f)); path(12f,3f,12f,12f,20f,17f); line(12f,12f,6f,19f) }
                "bag" -> { path(9f,7f,7f,3f,11f,4f,15f,3f,17f,7f); val p=Path().apply { moveTo(8f,8f); cubicTo(-2f,22f,26f,25f,17f,8f); close() }; drawPath(p,color,style=Stroke(1.7f)); line(9f,8f,16f,8f) }
                "pig" -> { drawOval(color,Offset(3f,8f),Size(17f,12f)); drawRoundRect(color,Offset(18f,11f),Size(5f,6f),androidx.compose.ui.geometry.CornerRadius(1f)); path(6f,9f,5f,5f,10f,8f); line(7f,18f,7f,23f); line(17f,18f,17f,23f); path(3f,12f,1f,10f,1f,14f,3f,14f); drawCircle(Color.White,1f,Offset(16f,11f)); drawCircle(color,3f,Offset(13f,3f)); line(12f,3f,14f,3f) }
                "settings" -> { drawCircle(color,7f,Offset(12f,12f),style=Stroke(1.7f)); drawCircle(color,2.5f,Offset(12f,12f),style=Stroke(1.7f)); for(i in 0..7) { val a=i*Math.PI/4; line((12+8*kotlin.math.cos(a)).toFloat(),(12+8*kotlin.math.sin(a)).toFloat(),(12+10*kotlin.math.cos(a)).toFloat(),(12+10*kotlin.math.sin(a)).toFloat()) } }
                "list" -> { for(y in listOf(6f,12f,18f)) line(3f,y,21f,y); drawCircle(color,2f,Offset(8f,6f)); drawCircle(color,2f,Offset(16f,12f)); drawCircle(color,2f,Offset(10f,18f)) }
                "eye", "hidden" -> { val p=Path().apply { moveTo(2f,12f); quadraticTo(12f,-1f,22f,12f); quadraticTo(12f,25f,2f,12f) }; drawPath(p,color,style=Stroke(1.5f)); drawCircle(color,3f,Offset(12f,12f),style=Stroke(1.5f)); if(kind=="hidden") line(3f,3f,21f,21f) }
            }
        }
    }
}

@Composable
internal fun HomeScreen(ledger: Ledger, alias: String, onProfile: () -> Unit, onAction: (Operation) -> Unit, onAnalysis: () -> Unit, onMovements: () -> Unit = {}, onMovement: (Movement) -> Unit = {}) {
    val summary = remember(ledger) { ledger.monthlySummary() }
    var hidden by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=18.dp,bottom=24.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        ScreenHeader(if(alias.isBlank()) "Hola" else "Hola, $alias", "Tu resumen financiero de hoy", onProfile)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF008D4C),Color(0xFF00A15A),Color(0xFF008341)))).padding(18.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("Saldo total",Modifier.weight(1f),color=Color.White,fontSize=13.sp)
                Box(Modifier.size(28.dp).clickable { hidden=!hidden }.semantics { contentDescription=if(hidden) "Mostrar saldos" else "Ocultar saldos" },contentAlignment=Alignment.Center) { Glyph(if(hidden) "hidden" else "eye",Color.White) }
            }
            Row(verticalAlignment=Alignment.Bottom, horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                Text(if(hidden) "••••••" else money(ledger.total),fontSize=31.sp,fontWeight=FontWeight.SemiBold,color=Color.White,modifier=Modifier.weight(1f,false))
                Text("MXN",color=Color.White.copy(alpha=.85f),fontSize=14.sp,modifier=Modifier.padding(bottom=4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) { Text("Disponible",color=Color.White,fontSize=12.sp); Text(if(hidden) "••••" else money(ledger.total-ledger.held),color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Medium) }
                Box(Modifier.padding(horizontal=16.dp).height(36.dp).width(1.dp).background(Color.White.copy(alpha=.2f)))
                Column(Modifier.weight(1f)) { Text("Apartado",color=Color.White,fontSize=12.sp); Text(if(hidden) "••••" else money(ledger.held),color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Medium) }
            }
            Spacer(Modifier.height(3.dp))
            val fraction = if(ledger.total>0) (BigDecimal.valueOf(ledger.total-ledger.held).divide(BigDecimal.valueOf(ledger.total),6,RoundingMode.HALF_UP)).toFloat() else 0f
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White.copy(alpha=.28f))) {
                if(fraction>0) Box(Modifier.fillMaxWidth(fraction.coerceIn(0f,1f)).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFCAFFDD),Color(0xFF79E7AD))),CircleShape))
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            MetricCard("Ingresos del mes",summary.income,summary.previousIncome,summary.incomeBars,Green,"down",hidden,Modifier.weight(1f))
            MetricCard("Gastos del mes",summary.expenses,summary.previousExpenses,summary.expenseBars,Red,"up",hidden,Modifier.weight(1f))
        }
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("Gastos por categoría",Modifier.weight(1f),fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink)
                TextButton(onClick=onAnalysis,contentPadding=PaddingValues(0.dp)) { Text("Ver más ›",color=Color(0xFF0098B5),fontSize=12.sp) }
            }
            CategoryChart(summary,hidden)
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("Últimos movimientos",Modifier.weight(1f),fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink)
            TextButton(onClick=onMovements,contentPadding=PaddingValues(0.dp)) { Text("Ver todos",fontSize=12.sp,color=Color(0xFF0098B5)) }
        }
        Surface(shape=RoundedCornerShape(18.dp),color=Color.White) {
            Column(Modifier.padding(horizontal=10.dp,vertical=4.dp)) {
                val recent=ledger.movements.sortedByDescending { it.date }.take(3)
                if(recent.isEmpty()) Text("Tus próximos movimientos aparecerán aquí.",Modifier.padding(12.dp),fontSize=12.sp,color=Muted)
                recent.forEach { movement -> MovementRow(ledger,movement,hidden,true) { onMovement(movement) } }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            QuickAction("Ingresar dinero","plus",Green,Modifier.weight(1f)) { onAction(Operation.INCOME) }
            QuickAction("Registrar gasto","minus",Red,Modifier.weight(1f)) { onAction(Operation.EXPENSE) }
            QuickAction("Transferencia","transfer",Color(0xFF3478FF),Modifier.weight(1f)) { onAction(Operation.TRANSFER) }
            QuickAction("Nuevo apartado","pig",Color(0xFFFF8B14),Modifier.weight(1f)) { onAction(Operation.RESERVE) }
        }
    }
}

@Composable
private fun MetricCard(title: String, amount: Long, previous: Long, bars: List<Long>, color: Color, icon: String, hidden: Boolean, modifier: Modifier) {
    Surface(modifier,shape=RoundedCornerShape(22.dp),color=Color.White,shadowElevation=3.dp) {
        Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(31.dp).background(color.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center) {
                Box(Modifier.size(20.dp).background(color,CircleShape),contentAlignment=Alignment.Center) { Glyph(icon,Color.White,Modifier.size(16.dp)) }
            }
            Text(title,fontSize=11.sp,color=Muted)
            Text(if(hidden) "••••••" else money(amount),fontSize=19.sp,fontWeight=FontWeight.Bold,color=Ink,maxLines=1)
            val change = if(previous>0) BigDecimal.valueOf(amount).subtract(BigDecimal.valueOf(previous)).multiply(BigDecimal(100)).divide(BigDecimal.valueOf(previous),0,RoundingMode.HALF_UP) else null
            Text(if(hidden) "•••" else if(change==null) "Sin mes previo" else "${if(change.signum()>=0) "↑ +" else "↓ "}$change%",fontSize=11.sp,color=if(change==null) Muted else color)
            Canvas(Modifier.fillMaxWidth().height(30.dp).semantics { contentDescription="Distribución de $title durante el mes" }) {
                val maximum=bars.maxOrNull()?.coerceAtLeast(1) ?: 1L
                val step=size.width / bars.size
                bars.forEachIndexed { i,value ->
                    val height=if(hidden) 0f else (value.toDouble()/maximum*size.height).toFloat()
                    drawRoundRect(color.copy(alpha=if(value==0L || hidden) .10f else .3f+i*.07f),Offset(i*step+step*.15f,size.height-height.coerceAtLeast(2f)),Size(step*.65f,height.coerceAtLeast(2f)),androidx.compose.ui.geometry.CornerRadius(2f))
                }
            }
        }
    }
}

@Composable
internal fun CategoryChart(summary: MonthlySummary, hidden: Boolean = false) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(136.dp),contentAlignment=Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(10.dp).semantics { contentDescription=if(hidden) "Gastos ocultos" else "Total de gastos del mes: ${money(summary.expenses)}" }) {
                val stroke=Stroke(22.dp.toPx())
                drawArc(Color(0xFFE6E9EF),0f,360f,false,style=stroke)
                if(!hidden && summary.expenses>0) {
                    var start=-90f
                    summary.categories.forEachIndexed { index, entry ->
                        val sweep=(entry.second.toDouble()/summary.expenses*360).toFloat()
                        drawArc(CategoryColors[index%CategoryColors.size],start,sweep,false,style=stroke)
                        start+=sweep
                    }
                }
            }
            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                Text(if(hidden) "••••" else money(summary.expenses),fontWeight=FontWeight.Bold,fontSize=17.sp,color=Ink)
                Text("MXN",fontSize=11.sp,color=Muted)
            }
        }
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            if(summary.categories.isEmpty()) {
                Text("Aún no hay gastos",fontSize=13.sp,fontWeight=FontWeight.Medium,color=Ink)
                Text("Registra tu primer gasto para ver su distribución.",fontSize=12.sp,color=Muted)
            } else summary.categories.forEachIndexed { index, entry ->
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(7.dp).background(CategoryColors[index%CategoryColors.size],CircleShape))
                    Text(entry.first,Modifier.weight(1f),fontSize=11.sp,color=Muted)
                    val percent=BigDecimal.valueOf(entry.second).multiply(BigDecimal(100)).divide(BigDecimal.valueOf(summary.expenses),0,RoundingMode.HALF_UP)
                    Text(if(hidden) "••" else "$percent%",fontSize=11.sp,color=Muted)
                }
            }
        }
    }
}
@Composable
private fun QuickAction(label: String, icon: String, color: Color, modifier: Modifier, action: () -> Unit) {
    Surface(onClick=action,modifier=modifier,shape=RoundedCornerShape(16.dp),color=Color.Transparent) {
        Column(Modifier.padding(horizontal=1.dp,vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Box(Modifier.width(58.dp).height(40.dp).background(color.copy(alpha=.13f),RoundedCornerShape(22.dp)),contentAlignment=Alignment.Center) {
                if(icon=="plus" || icon=="minus") Box(Modifier.size(23.dp).background(color,CircleShape),contentAlignment=Alignment.Center) { Glyph(icon,Color.White,Modifier.size(17.dp)) }
                else Glyph(icon,color)
            }
            Text(label,fontSize=9.sp,lineHeight=12.sp,textAlign=TextAlign.Center,color=Muted)
        }
    }
}

internal enum class Section(val label: String,val icon: String) {
    HOME("Inicio","home"), MOVEMENTS("Movimientos","list"), SAVINGS("Apartados","pig")
}
@Composable
internal fun BottomBar(selected: Section, select: (Section) -> Unit) {
    Surface(color=Color.White,shadowElevation=8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(top=12.dp,bottom=10.dp)) {
            Section.entries.forEach { section ->
                val color=if(selected==section) Green else Muted
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { select(section) }.padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Glyph(section.icon,color)
                    Text(section.label,fontSize=10.sp,color=color,fontWeight=if(selected==section) FontWeight.SemiBold else FontWeight.Normal,maxLines=1)
                }
            }
        }
    }
}
@Preview(showBackground=true, widthDp=360, heightDp=780)
@Composable
private fun EmptyHomePreview() {
    com.app.control.ui.theme.ControlTheme(darkTheme=false) {
        Scaffold(bottomBar={ BottomBar(Section.HOME,{}) }) { padding -> Box(Modifier.padding(padding)) { HomeScreen(Ledger(),"",{},{},{}) } }
    }
}
