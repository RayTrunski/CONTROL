package com.app.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
internal val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
internal val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
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
                "income", "expense" -> { path(3f,15f,3f,21f,21f,21f,21f,15f); line(12f,2f,12f,16f); if(kind=="income") path(7f,11f,12f,16f,17f,11f) else path(7f,7f,12f,2f,17f,7f) }
                "withdraw" -> { drawOval(color,Offset(3f,10f),Size(17f,10f)); path(6f,11f,5f,7f,10f,10f); line(7f,19f,7f,23f); line(17f,19f,17f,23f); path(19f,12f,23f,12f,23f,17f,20f,17f); line(13f,11f,13f,1f); path(9f,5f,13f,1f,17f,5f) }
                "food" -> { line(4f,3f,4f,10f); line(7f,3f,7f,21f); line(10f,3f,10f,10f); path(4f,10f,7f,12f,10f,10f); path(19f,3f,15f,9f,15f,13f,19f,13f,19f,3f,19f,21f) }
                "heart" -> { val p=Path().apply { moveTo(12f,21f); cubicTo(-10f,6f,8f,-3f,12f,7f); cubicTo(18f,-3f,33f,6f,12f,21f) }; drawPath(p,color,style=Stroke(1.7f)); path(3f,12f,8f,12f,10f,8f,13f,16f,16f,11f,21f,11f) }
                "education" -> { path(2f,8f,12f,3f,22f,8f,12f,13f,2f,8f); path(6f,11f,6f,17f,12f,21f,18f,17f,18f,11f); line(22f,8f,22f,17f) }
                "fitness" -> { line(7f,12f,17f,12f); for(x in listOf(3f,7f,17f,21f)) line(x,if(x==3f || x==21f) 8f else 5f,x,if(x==3f || x==21f) 16f else 19f) }
                "ticket" -> { path(3f,4f,21f,4f,21f,9f,18f,12f,21f,15f,21f,20f,3f,20f,3f,15f,6f,12f,3f,9f,3f,4f); path(12f,7f,13f,10f,16f,10f,14f,12f,15f,16f,12f,14f,9f,16f,10f,12f,8f,10f,11f,10f,12f,7f) }
                "coins" -> { drawOval(color,Offset(3f,11f),Size(10f,5f),style=Stroke(1.7f)); path(3f,13f,3f,20f,8f,22f,13f,20f,13f,13f); drawOval(color,Offset(12f,2f),Size(10f,5f),style=Stroke(1.7f)); path(12f,4f,12f,10f); path(22f,4f,22f,16f,17f,18f); line(4f,18f,12f,18f) }
                "cash" -> { path(2f,7f,20f,7f,20f,21f,2f,21f,2f,7f); path(5f,4f,22f,2f,23f,17f); drawCircle(color,3f,Offset(11f,14f),style=Stroke(1.7f)); line(5f,11f,6f,11f); line(16f,17f,17f,17f) }
                "bank" -> { path(2f,8f,12f,2f,22f,8f,2f,8f); for(x in listOf(5f,12f,19f)) line(x,10f,x,19f); path(2f,22f,22f,22f); line(3f,19f,21f,19f) }
                "wallet" -> { path(3f,7f,20f,7f,20f,21f,3f,21f,3f,7f,17f,3f,19f,7f); path(20f,11f,14f,11f,14f,17f,20f,17f); drawCircle(color,1f,Offset(17f,14f)) }
                "briefcase", "archive" -> { path(3f,7f,21f,7f,21f,21f,3f,21f,3f,7f); path(8f,7f,8f,3f,16f,3f,16f,7f); line(3f,12f,21f,12f); line(11f,12f,13f,12f) }
                "shirt" -> path(8f,3f,3f,5f,1f,10f,5f,12f,7f,9f,7f,22f,17f,22f,17f,9f,19f,12f,23f,10f,21f,5f,16f,3f,12f,6f,8f,3f)
                "family" -> { drawCircle(color,3f,Offset(12f,6f)); drawCircle(color,2.5f,Offset(4f,9f)); drawCircle(color,2.5f,Offset(20f,9f)); drawArc(color,180f,180f,false,Offset(6f,12f),Size(12f,16f),style=Stroke(2f)); path(1f,20f,1f,16f,4f,14f); path(23f,20f,23f,16f,20f,14f) }
                "paw" -> { drawOval(color,Offset(6f,12f),Size(12f,9f)); for((x,y) in listOf(3f to 9f,8f to 5f,16f to 5f,21f to 9f)) drawOval(color,Offset(x-2f,y-3f),Size(4f,6f)) }
                "umbrella" -> { drawArc(color,180f,180f,false,Offset(2f,4f),Size(20f,20f),style=Stroke(1.7f)); line(2f,14f,22f,14f); path(12f,2f,12f,20f,15f,22f,18f,20f) }
                "more" -> { for(x in listOf(5f,12f,19f)) drawCircle(color,2f,Offset(x,12f)) }
                "check", "cancel", "clock", "pause", "info", "target" -> { drawCircle(color,9f,Offset(12f,12f),style=Stroke(1.7f)); when(kind) { "check" -> path(7f,12f,11f,16f,17f,8f); "cancel" -> { line(8f,8f,16f,16f); line(16f,8f,8f,16f) }; "clock" -> path(12f,6f,12f,12f,17f,15f); "pause" -> { line(9f,8f,9f,16f); line(15f,8f,15f,16f) }; "info" -> { drawCircle(color,1f,Offset(12f,7f)); line(12f,11f,12f,17f) }; else -> { drawCircle(color,5f,Offset(12f,12f),style=Stroke(1.7f)); path(12f,12f,22f,2f) } } }
                "edit" -> { path(3f,21f,5f,14f,17f,2f,22f,7f,10f,19f,3f,21f); line(14f,5f,19f,10f) }
                "delete" -> { path(5f,7f,6f,22f,18f,22f,19f,7f); line(3f,5f,21f,5f); path(9f,5f,9f,2f,15f,2f,15f,5f); line(10f,10f,10f,18f); line(14f,10f,14f,18f) }
                "copy" -> { path(7f,7f,21f,7f,21f,22f,7f,22f,7f,7f); path(17f,4f,17f,2f,3f,2f,3f,17f,5f,17f) }
                "tag" -> { path(2f,3f,12f,3f,22f,13f,13f,22f,2f,11f,2f,3f); drawCircle(color,1.5f,Offset(7f,7f)) }
                "stats" -> { for((x,h) in listOf(4f to 8f,10f to 14f,16f to 20f)) drawRoundRect(color,Offset(x,23f-h),Size(4f,h),androidx.compose.ui.geometry.CornerRadius(1f)) }
                "chevronRight" -> path(9f,5f,16f,12f,9f,19f)
                "sun" -> { drawCircle(color,4f,Offset(12f,12f),style=Stroke(1.7f)); for(i in 0..7) { val a=i*Math.PI/4; line((12+7*kotlin.math.cos(a)).toFloat(),(12+7*kotlin.math.sin(a)).toFloat(),(12+10*kotlin.math.cos(a)).toFloat(),(12+10*kotlin.math.sin(a)).toFloat()) } }
                "moon" -> { val p=Path().apply { moveTo(15f,3f); cubicTo(2f,0f,0f,20f,14f,21f); cubicTo(19f,21f,22f,17f,21f,14f); cubicTo(13f,18f,8f,9f,15f,3f) }; drawPath(p,color,style=Stroke(1.7f)) }
                "monitor" -> { path(3f,4f,21f,4f,21f,17f,3f,17f,3f,4f); line(12f,17f,12f,21f); line(8f,21f,16f,21f) }
                "card" -> { path(3f,5f,21f,5f,21f,20f,3f,20f,3f,5f); line(3f,10f,21f,10f) }
                "document" -> { path(5f,2f,14f,2f,20f,8f,20f,22f,5f,22f,5f,2f); path(14f,2f,14f,8f,20f,8f); line(9f,13f,16f,13f); line(9f,17f,16f,17f) }
                "globe" -> { drawCircle(color,9f,Offset(12f,12f),style=Stroke(1.7f)); drawOval(color,Offset(8f,3f),Size(8f,18f),style=Stroke(1.5f)); line(3f,9f,21f,9f); line(3f,15f,21f,15f) }
                "cloud", "restore" -> { val p=Path().apply { moveTo(6f,19f); cubicTo(-1f,19f,0f,9f,6f,10f); cubicTo(6f,0f,19f,1f,19f,10f); cubicTo(26f,10f,25f,19f,18f,19f); close() }; drawPath(p,color,style=Stroke(1.7f)); line(12f,9f,12f,16f); if(kind=="cloud") path(9f,13f,12f,16f,15f,13f) else path(9f,12f,12f,9f,15f,12f) }
                "chevronDown" -> path(7f,10f,12f,15f,17f,10f)
                "calendar" -> { path(4f,5f,20f,5f,20f,21f,4f,21f,4f,5f); line(4f,10f,20f,10f); line(8f,2f,8f,7f); line(16f,2f,16f,7f); line(8f,14f,10f,14f); line(14f,14f,16f,14f) }
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
internal fun HomeScreen(ledger: Ledger, alias: String, onProfile: () -> Unit, onAction: (Operation) -> Unit, onAnalysis: () -> Unit, onMovements: () -> Unit = {}, onMovement: (Movement) -> Unit = {}, onPlan: () -> Unit = {}, filter: AnalysisFilter = AnalysisFilter(), onFilter: (AnalysisFilter)->Unit = {}) {
    val summary = ledger.analysisSummary(filter)
    val hidden=false
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=18.dp,bottom=24.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        ScreenHeader(if(alias.isBlank()) "Hola" else "Hola, $alias", "Saldos actuales · análisis del periodo", onProfile)
        val accountIds=listOf<String?>(null)+ledger.accounts.map { it.id }
        val pager=rememberPagerState(initialPage=accountIds.indexOf(filter.account).coerceAtLeast(0),pageCount={ accountIds.size })
        LaunchedEffect(filter.account) { pager.scrollToPage(accountIds.indexOf(filter.account).coerceAtLeast(0)) }
        LaunchedEffect(pager.settledPage) { val id=accountIds[pager.settledPage]; if(id!=filter.account) onFilter(filter.copy(account=id)) }
        HorizontalPager(state=pager,pageSpacing=12.dp,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.fillMaxWidth()) { page ->
        val account=accountIds[page]
        val total=account?.let(ledger::balance) ?: ledger.total
        val held=account?.let(ledger::reserved) ?: ledger.held
        val tint=when(page%3) { 1 -> Color(0xFF7561BD); 2 -> Color(0xFF3988BB); else -> Green }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(tint,tint.copy(red=tint.red*.8f,green=tint.green*.85f)))) .padding(18.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(Color.White.copy(alpha=.16f),CircleShape),contentAlignment=Alignment.Center) { Glyph("bag",Color.White,Modifier.size(21.dp)) }
                Column(Modifier.padding(start=10.dp)) {
                    Text(ledger.accounts.firstOrNull { it.id==account }?.name ?: "Todas las cuentas",color=Color.White,fontSize=14.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(if(account==null) "Saldo total" else "Saldo actual",color=Color.White.copy(alpha=.85f),fontSize=11.sp)
                }
            }
            Row(verticalAlignment=Alignment.Bottom, horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                Text(if(hidden) "••••••" else money(total),fontSize=27.sp,fontWeight=FontWeight.SemiBold,color=Color.White,modifier=Modifier.weight(1f,false),maxLines=1)
                Text("MXN",color=Color.White.copy(alpha=.85f),fontSize=14.sp,modifier=Modifier.padding(bottom=4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) { Text("Disponible",color=Color.White,fontSize=12.sp); Text(if(hidden) "••••" else money(total-held),color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Medium) }
                Box(Modifier.padding(horizontal=16.dp).height(36.dp).width(1.dp).background(Color.White.copy(alpha=.2f)))
                Column(Modifier.weight(1f)) { Text("Apartado",color=Color.White,fontSize=12.sp); Text(if(hidden) "••••" else money(held),color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Medium) }
            }
            Spacer(Modifier.height(3.dp))
            val fraction = if(total>0) (BigDecimal.valueOf(total-held).divide(BigDecimal.valueOf(total),6,RoundingMode.HALF_UP)).toFloat() else 0f
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White.copy(alpha=.28f))) {
                if(fraction>0) Box(Modifier.fillMaxWidth(fraction.coerceIn(0f,1f)).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFCAFFDD),Color(0xFF79E7AD))),CircleShape))
            }
        }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
            accountIds.forEachIndexed { index,id -> Box(Modifier.padding(horizontal=3.dp).size(7.dp).clip(CircleShape).background(if(index==pager.currentPage) Green else Color(0xFFCBD2DC)).clickable { onFilter(filter.copy(account=id)) }.semantics { contentDescription=if(id==null) "Todas las cuentas" else ledger.accounts.first { it.id==id }.name }) }
        }
        SharedFilters(ledger,filter,onFilter,showAccount=false)
        Surface(onClick=onPlan,shape=RoundedCornerShape(18.dp),color=Green.copy(alpha=.08f)) {
            Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(38.dp).background(MaterialTheme.colorScheme.surface,CircleShape),contentAlignment=Alignment.Center) { Glyph("calendar",Green) }
                Column(Modifier.weight(1f)) { Text("Planificar",fontWeight=FontWeight.SemiBold,fontSize=14.sp,color=Ink); Text("Revisa y organiza tus próximos meses",color=Muted,fontSize=11.sp) }
                Text("›",color=Green,fontSize=25.sp)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            MetricCard("Ingresos del periodo",summary.income,summary.previousIncome,summary.incomeBars,Green,"down",hidden,Modifier.weight(1f))
            MetricCard("Gastos del periodo",summary.expenses,summary.previousExpenses,summary.expenseBars,Red,"up",hidden,Modifier.weight(1f))
        }
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("Gastos por categoría",Modifier.weight(1f),fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink)
                TextButton(onClick=onAnalysis,contentPadding=PaddingValues(0.dp)) { Text("Ver más ›",color=Color(0xFF0098B5),fontSize=12.sp) }
            }
            Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=1.dp) { Box(Modifier.padding(14.dp)) { CategoryChart(summary,hidden) } }
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("Últimos movimientos",Modifier.weight(1f),fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink)
            TextButton(onClick=onMovements,contentPadding=PaddingValues(0.dp)) { Text("Ver todos",fontSize=12.sp,color=Color(0xFF0098B5)) }
        }
        Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal=10.dp,vertical=4.dp)) {
                val recent=ledger.movements.filter(filter::matches).sortedByDescending { it.date }.take(3)
                if(recent.isEmpty()) Text("Tus próximos movimientos aparecerán aquí.",Modifier.padding(12.dp),fontSize=12.sp,color=Muted)
                recent.forEach { movement -> MovementRow(ledger,movement,hidden,true) { onMovement(movement) } }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            QuickAction("Ingresar dinero","income",Green,Modifier.weight(1f)) { onAction(Operation.INCOME) }
            QuickAction("Registrar gasto","expense",Red,Modifier.weight(1f)) { onAction(Operation.EXPENSE) }
            QuickAction("Transferencia","transfer",Color(0xFF3478FF),Modifier.weight(1f)) { onAction(Operation.TRANSFER) }
            QuickAction("Nuevo apartado","pig",Color(0xFF9146F5),Modifier.weight(1f)) { onAction(Operation.RESERVE) }
        }
    }
}

@Composable
private fun MetricCard(title: String, amount: Long, previous: Long, bars: List<Long>, color: Color, icon: String, hidden: Boolean, modifier: Modifier) {
    Surface(modifier,shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=3.dp) {
        Box(Modifier.padding(12.dp)) {
        Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(25.dp).background(color.copy(alpha=.15f),CircleShape),contentAlignment=Alignment.Center) {
                Glyph(icon,color,Modifier.size(17.dp))
            }
            Text(title,fontSize=11.sp,color=Muted)
            Text(if(hidden) "••••••" else money(amount),fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink,maxLines=1)
            val change = if(previous>0) BigDecimal.valueOf(amount).subtract(BigDecimal.valueOf(previous)).multiply(BigDecimal(100)).divide(BigDecimal.valueOf(previous),0,RoundingMode.HALF_UP) else null
            Text(if(hidden) "•••" else if(change==null) "Sin periodo previo" else "${if(change.signum()>=0) "↑ +" else "↓ "}$change%",fontSize=11.sp,color=if(change==null) Muted else color)
            Text("vs. periodo anterior",fontSize=9.sp,color=Muted)
        }
            Canvas(Modifier.align(Alignment.BottomEnd).width(45.dp).height(42.dp).padding(bottom=16.dp).semantics { contentDescription="Distribución de $title durante el periodo" }) {
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
    val track=MaterialTheme.colorScheme.surfaceVariant
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(136.dp),contentAlignment=Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(10.dp).semantics { contentDescription=if(hidden) "Gastos ocultos" else "Total de gastos del periodo: ${money(summary.expenses)}" }) {
                val stroke=Stroke(22.dp.toPx())
                drawArc(track,0f,360f,false,style=stroke)
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
                    Glyph(categoryIcon(entry.first).icon,categoryIcon(entry.first).color,Modifier.size(17.dp))
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
    Surface(color=MaterialTheme.colorScheme.surface,shadowElevation=8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(top=12.dp,bottom=10.dp)) {
            Section.entries.forEach { section ->
                val color=if(selected==section) MaterialTheme.colorScheme.primary else Muted
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
