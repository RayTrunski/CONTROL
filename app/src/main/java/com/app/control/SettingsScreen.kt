package com.app.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Account order is persisted; appending an account keeps existing colors stable.
private val AccountPalette=listOf(
    Color(0xFFA020DB), Color(0xFF004780), Color(0xFF008BA3),
    Color(0xFFCF344A), Color(0xFFAD651D), Color(0xFF187B58),
    Color(0xFF5155BB), Color(0xFFBE397F), Color(0xFF527521),
    Color(0xFF376A9D), Color(0xFF8C5541), Color(0xFF625980)
)

@Composable
internal fun SettingsScreen(
    ledger: Ledger, alias: String, dark: Boolean?, onTheme: (Boolean?) -> Unit,
    onBack: () -> Unit, onProfile: () -> Unit, onCreateAccount: () -> Unit,
    onExport: () -> Unit, onBackup: () -> Unit, onRestore: () -> Unit,
    onReopen: (SavingsGoal) -> Unit
) {
    val colors=MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(bottom=24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        TextButton(onClick=onBack, contentPadding=PaddingValues(horizontal=0.dp)) { Text("‹  Volver",fontWeight=FontWeight.SemiBold) }
        Column(Modifier.padding(bottom=4.dp),verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Text("Ajustes y perfil",fontSize=25.sp,fontWeight=FontWeight.Bold)
            Text("Gestiona tu perfil y la configuración de la app.",fontSize=13.sp,color=colors.onSurfaceVariant)
        }
        SettingsCard {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(50.dp).background(colors.primaryContainer,CircleShape),contentAlignment=Alignment.Center) { Glyph("user",colors.primary,Modifier.size(30.dp)) }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                    Text(alias.ifBlank { "Tu perfil local" },fontSize=14.sp,fontWeight=FontWeight.Bold)
                    Text("Este nombre solo se muestra en tu dispositivo.",fontSize=11.sp,color=colors.onSurfaceVariant,lineHeight=16.sp)
                }
                OutlinedButton(onClick=onProfile,shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(horizontal=10.dp,vertical=6.dp)) { Glyph("edit",colors.primary,Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Editar nombre",fontSize=11.sp) }
            }
        }
        SettingsCard {
            SettingsHeading("sun","Apariencia","Elige el tema de la aplicación.")
            Surface(color=colors.background,shape=RoundedCornerShape(15.dp)) {
                Row(Modifier.fillMaxWidth().padding(3.dp)) {
                    listOf(Triple(false,"sun","Claro"),Triple(true,"moon","Oscuro"),Triple(null,"monitor","Sistema")).forEach { (value,icon,label) ->
                        val selected=dark==value
                        Surface(onClick={ onTheme(value) },modifier=Modifier.weight(1f).semantics { this.selected=selected },shape=RoundedCornerShape(12.dp),color=if(selected) colors.primaryContainer else Color.Transparent,contentColor=if(selected) colors.primary else colors.onSurfaceVariant) {
                            Row(Modifier.heightIn(min=44.dp).padding(horizontal=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp,Alignment.CenterHorizontally)) {
                                Glyph(icon,if(selected) colors.primary else colors.onSurfaceVariant,Modifier.size(19.dp))
                                Text(label,fontSize=12.sp)
                            }
                        }
                    }
                }
            }
        }
        SettingsCard {
            SettingsHeading("bank","Mis cuentas","Administra tus cuentas y revisa tus saldos.")
            if(ledger.accounts.isEmpty()) Text("Aún no tienes cuentas. Crea la primera para comenzar.",fontSize=13.sp,color=colors.onSurfaceVariant)
            ledger.accounts.forEachIndexed { index,account ->
                Surface(shape=RoundedCornerShape(15.dp),border=androidx.compose.foundation.BorderStroke(1.dp,colors.outline.copy(alpha=.45f)),color=colors.surface) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        val badge=AccountPalette[index % AccountPalette.size]
                        Box(Modifier.size(48.dp).background(badge,CircleShape),contentAlignment=Alignment.Center) {
                            Glyph("bank",Color.White,Modifier.size(27.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(account.name,fontSize=14.sp,fontWeight=FontWeight.Bold)
                            Text("Saldo: ${money(ledger.balance(account.id))}",fontSize=12.sp)
                            Text("Reservado: ${money(ledger.reserved(account.id))}",fontSize=12.sp,color=colors.onSurfaceVariant)
                            Text("Disponible: ${money(ledger.available(account.id))}",fontSize=12.sp,color=colors.onSurfaceVariant)
                        }
                    }
                }
            }
            FilledTonalButton(onClick=onCreateAccount,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.filledTonalButtonColors(containerColor=colors.primaryContainer,contentColor=colors.primary)) {
                Glyph("plus",colors.primary,Modifier.size(20.dp)); Spacer(Modifier.width(7.dp)); Text("Crear cuenta",fontSize=13.sp)
            }
        }
        SettingsCard {
            SettingsHeading("settings","Datos y respaldo","Configuración de la app y herramientas de gestión.")
            Surface(shape=RoundedCornerShape(14.dp),color=colors.background) {
                Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Glyph("globe",colors.onSurfaceVariant,Modifier.size(24.dp))
                    Column(verticalArrangement=Arrangement.spacedBy(3.dp)) {
                        Text("Moneda: MXN · Formato: México",fontSize=12.sp)
                        Text("Gestión y planificación financiera personal · Sin conexión",fontSize=10.sp,color=colors.onSurfaceVariant)
                    }
                }
            }
            SettingsAction("document","Exportar CSV (incluye planificación)",onExport)
            SettingsAction("cloud","Guardar respaldo local",onBackup)
            SettingsAction("restore","Restaurar respaldo",onRestore)
            ledger.goals.filter { it.archived }.forEach { goal -> TextButton(onClick={ onReopen(goal) }) { Text("Reabrir ${goal.name}") } }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}

@Composable
private fun SettingsHeading(icon: String,title: String,subtitle: String) {
    Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
        Glyph(icon,if(icon=="card") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(22.dp))
        Column(verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Text(title,fontSize=16.sp,fontWeight=FontWeight.Bold)
            Text(subtitle,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsAction(icon: String,label: String,onClick: () -> Unit) {
    Surface(onClick=onClick,shape=RoundedCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outline.copy(alpha=.4f)),modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.heightIn(min=48.dp).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Glyph(icon,MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(21.dp))
            Text(label,modifier=Modifier.weight(1f),fontSize=12.sp)
            Glyph("chevronRight",MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(17.dp))
        }
    }
}


