package com.app.control

import androidx.compose.ui.graphics.Color

internal data class IconStyle(val icon: String,val color: Color)
internal fun Operation.iconStyle() = when(this) {
    Operation.INCOME -> IconStyle("income",Color(0xFF008B50))
    Operation.EXPENSE -> IconStyle("expense",Color(0xFFF12A58))
    Operation.TRANSFER -> IconStyle("transfer",Color(0xFF1670F8))
    Operation.RESERVE -> IconStyle("pig",Color(0xFF9146F5))
    Operation.RELEASE -> IconStyle("withdraw",Color(0xFFF49321))
}
internal fun categoryIcon(name: String) = when(name) {
    "Alimentación" -> IconStyle("food",Color(0xFF008B50))
    "Vivienda" -> IconStyle("home",Color(0xFF2475F8))
    "Transporte" -> IconStyle("car",Color(0xFFEF3659))
    "Servicios" -> IconStyle("bulb",Color(0xFFF49300))
    "Compras" -> IconStyle("cart",Color(0xFF8040F3))
    "Salud" -> IconStyle("heart",Color(0xFFF32973))
    "Educación" -> IconStyle("education",Color(0xFF894AF4))
    "Fitness" -> IconStyle("fitness",Color(0xFFFF841D))
    "Viajes" -> IconStyle("plane",Color(0xFF00ADD1))
    "Entretenimiento" -> IconStyle("ticket",Color(0xFFEE3C85))
    "Ingresos" -> IconStyle("coins",Color(0xFF008B50))
    "Trabajo" -> IconStyle("briefcase",Color(0xFFB97A3F))
    "Personal" -> IconStyle("shirt",Color(0xFF159CE5))
    "Familia" -> IconStyle("family",Color(0xFF8544F5))
    "Mascotas" -> IconStyle("paw",Color(0xFF078334))
    "Regalos" -> IconStyle("gift",Color(0xFFFF8426))
    "Seguros" -> IconStyle("umbrella",Color(0xFFE93477))
    "Deuda" -> IconStyle("card",Color(0xFF5A687D))
    else -> IconStyle("more",Color(0xFF845133))
}
internal fun fieldIcon(label: String) = when {
    label.contains("Categoría",true) || label.contains("Subcategoría",true) -> "tag"
    label.contains("fecha",true) || label.contains("cobro",true) || label.contains("día",true) -> "calendar"
    label.contains("importe",true) || label.contains("monto",true) -> "coins"
    label.contains("cuenta",true) || label=="Destino" -> "bank"
    label.contains("apartado",true) -> "target"
    label.contains("Periodicidad",true) -> "clock"
    label.contains("Icono",true) -> "pig"
    else -> "document"
}
internal fun PlanStatus.iconName() = when(this) {
    PlanStatus.PENDING -> "calendar"
    PlanStatus.EXECUTED -> "check"
    PlanStatus.CANCELLED -> "cancel"
}
