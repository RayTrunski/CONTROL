package com.app.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.control.ui.theme.ControlTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The dashboard currently uses a light theme even when Android is in dark mode.
        // Match the system bar icons to the actual app surface rather than the OS theme.
        val lightBars = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent { ControlTheme(darkTheme = false) { ControlApp() } }
    }
}

@Composable
internal fun TextEntryDialog(title: String,label: String,initial: String,dismiss: () -> Unit,save: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest=dismiss,title={ Text(title) },text={ Column {
        OutlinedTextField(value,{ value=it },label={ Text(label) },singleLine=true)
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
    } },confirmButton={ TextButton(onClick={ runCatching { save(value) }.onFailure { error=it.message } }) { Text("Guardar") } },dismissButton={ TextButton(onClick=dismiss) { Text("Cancelar") } })
}

@Composable
internal fun MovementDialog(ledger: Ledger,initial: Operation,goal: String?,dismiss: () -> Unit,save: (Ledger) -> Unit) {
    var type by remember { mutableStateOf(initial) }
    var account by remember { mutableStateOf((if(initial==Operation.RELEASE && goal!=null) ledger.accounts.firstOrNull { ledger.goalBalance(goal,it.id)>0 } else ledger.accounts.firstOrNull())?.id) }
    var destination by remember { mutableStateOf(ledger.accounts.firstOrNull { it.id!=account }?.id) }
    var accountName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest=dismiss,title={ Text("Registrar ${type.label().lowercase()}") },text={
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            val types=if(goal==null) listOf(Operation.INCOME,Operation.EXPENSE,Operation.TRANSFER) else listOf(Operation.RESERVE,Operation.RELEASE)
            Choice("Tipo",type.label(),types.map { it.label() }) { type=types[it] }
            goal?.let { id -> Text("Apartado: ${ledger.goals.first { it.id==id }.name}",fontSize=13.sp,color=Green) }
            if(ledger.accounts.isEmpty()) {
                Text("Crea tu primera cuenta para registrar el movimiento.",fontSize=13.sp)
                OutlinedTextField(accountName,{ accountName=it },label={ Text("Nombre de la cuenta") },singleLine=true)
            } else Choice("Cuenta",ledger.accounts.firstOrNull { it.id==account }?.name ?: "Seleccionar",ledger.accounts.map { it.name }) { account=ledger.accounts[it].id }
            if(type==Operation.TRANSFER) {
                Choice("Destino",ledger.accounts.firstOrNull { it.id==destination }?.name ?: "Seleccionar",ledger.accounts.map { it.name }) { destination=ledger.accounts[it].id }
                if(ledger.accounts.size<2) Text("Crea una segunda cuenta desde Ajustes para transferir.",fontSize=12.sp,color=Muted)
            }
            OutlinedTextField(amount,{ amount=it },label={ Text("Importe MXN") },placeholder={ Text("0.00") },singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
            if(type==Operation.EXPENSE) Choice("Categoría",category,expenseCategories) { category=expenseCategories[it] }
            OutlinedTextField(date,{ date=it },label={ Text("Fecha (dd/mm/aaaa)") },singleLine=true)
            OutlinedTextField(description,{ description=it },label={ Text("Descripción opcional") })
            error?.let { Text(it,color=MaterialTheme.colorScheme.error,fontSize=12.sp) }
        }
    },confirmButton={ TextButton(onClick={
        runCatching {
            val updated=if(ledger.accounts.isEmpty()) ledger.addAccount(accountName) else ledger
            val formatter=DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)
            updated.post(Movement(type=type,cents=parseCents(amount),account=account ?: updated.accounts.first().id,destination=if(type==Operation.TRANSFER) destination else null,
                description=description.trim(),date=LocalDate.parse(date,formatter),category=if(type==Operation.EXPENSE) category else "Otros",goal=goal))
        }.onSuccess(save).onFailure { error=if(it is java.time.format.DateTimeParseException) "Revisa la fecha; usa dd/mm/aaaa." else it.message ?: "No se pudo registrar el movimiento." }
    }) { Text("Registrar") } },dismissButton={ TextButton(onClick=dismiss) { Text("Cancelar") } })
}
@Composable
internal fun Choice(label: String,selected: String,options: List<String>,choose: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick={ open=true },modifier=Modifier.fillMaxWidth()) { Text("$label: $selected") }
        DropdownMenu(open,onDismissRequest={ open=false }) {
            options.forEachIndexed { index,option -> DropdownMenuItem(text={ Text(option) },onClick={ choose(index); open=false }) }
        }
    }
}
