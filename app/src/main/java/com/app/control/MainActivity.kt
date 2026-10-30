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
        val lightBars = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            val vm: FinanceViewModel=androidx.lifecycle.viewmodel.compose.viewModel()
            val preference by vm.darkTheme.collectAsState()
            val dark=preference ?: androidx.compose.foundation.isSystemInDarkTheme()
            SideEffect { val bars=if(dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else lightBars; enableEdgeToEdge(statusBarStyle=bars,navigationBarStyle=bars) }
            ControlTheme(darkTheme=dark) { ControlApp(vm) }
        }
    }
}

@Composable
internal fun TextEntryDialog(title: String,label: String,initial: String,dismiss: () -> Unit,save: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<String?>(null) }
    FormDialog(onDismissRequest=dismiss,subtitle="Personaliza la información de tu app.",title={ Text(title) },text={ Column {
        FormTextField(value,{ value=it },label={ Text(label) },singleLine=true)
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
    } },confirmButton={ Button(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick={ runCatching { save(value) }.onFailure { error=it.message } }) { Text("Guardar") } },dismissButton={ OutlinedButton(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick=dismiss) { Text("Cancelar") } })
}

@Composable
internal fun MovementDialog(ledger: Ledger,initial: Operation,goal: String?,dismiss: () -> Unit,initialMovement: Movement? = null,initialAccount: String? = null,save: (Ledger) -> Unit) {
    var type by remember { mutableStateOf(initialMovement?.type ?: initial) }
    var account by remember { mutableStateOf(initialMovement?.account ?: ledger.accounts.firstOrNull { it.id==initialAccount && (initial!=Operation.RELEASE || goal==null || ledger.goalBalance(goal,it.id)>0) }?.id ?: (if(initial==Operation.RELEASE && goal!=null) ledger.accounts.firstOrNull { ledger.goalBalance(goal,it.id)>0 } else ledger.accounts.firstOrNull())?.id) }
    var destination by remember { mutableStateOf(initialMovement?.destination ?: ledger.accounts.firstOrNull { it.id!=account }?.id) }
    var accountName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(initialMovement?.cents?.let { java.math.BigDecimal.valueOf(it,2).toPlainString() }.orEmpty()) }
    var description by remember { mutableStateOf(initialMovement?.description.orEmpty()) }
    var category by remember { mutableStateOf(initialMovement?.category ?: expenseCategories.first()) }
    var subcategory by remember { mutableStateOf(initialMovement?.subcategory.orEmpty()) }
    var date by remember { mutableStateOf((initialMovement?.date ?: LocalDate.now()).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))) }
    var error by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    FormDialog(onDismissRequest=dismiss,icon=type.iconStyle().icon,subtitle=if(initialMovement==null) "Registra un movimiento y actualiza tus saldos." else "Actualiza los datos de este movimiento.",title={ Text(if(initialMovement==null) "Registrar ${type.label().lowercase()}" else "Editar ${type.label().lowercase()}") },text={
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            val types=if(goal==null) listOf(Operation.INCOME,Operation.EXPENSE,Operation.TRANSFER) else listOf(Operation.RESERVE,Operation.RELEASE)
            OperationPicker(types,type) { type=it }
            goal?.let { id -> Text("Apartado: ${ledger.goals.first { it.id==id }.name}",fontSize=13.sp,color=Green) }
            if(ledger.accounts.isEmpty()) {
                Text("Crea tu primera cuenta para registrar el movimiento.",fontSize=13.sp)
                FormTextField(accountName,{ accountName=it },icon=fieldIcon("Nombre de la cuenta"),label={ Text("Nombre de la cuenta") },singleLine=true)
            } else Choice("Cuenta",ledger.accounts.firstOrNull { it.id==account }?.name ?: "Seleccionar",ledger.accounts.map { it.name }) { account=ledger.accounts[it].id }
            if(type==Operation.TRANSFER) {
                Choice("Destino",ledger.accounts.firstOrNull { it.id==destination }?.name ?: "Seleccionar",ledger.accounts.map { it.name }) { destination=ledger.accounts[it].id }
                if(ledger.accounts.size<2) Text("Crea una segunda cuenta desde Ajustes para transferir.",fontSize=12.sp,color=Muted)
            }
            FormTextField(amount,{ amount=it; amountError=null; error=null },icon=fieldIcon("Importe MXN"),label={ Text("Importe MXN") },placeholder={ Text("0.00") },singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),isError=amountError!=null,supportingText={ amountError?.let { Text(it) } })
            if(type==Operation.EXPENSE) Choice("Categoría",category,expenseCategories) { category=expenseCategories[it] }
            if(type==Operation.EXPENSE) FormTextField(subcategory,{ subcategory=it },icon=fieldIcon("Subcategoría"),label={ Text("Subcategoría") })
            FormTextField(date,{ date=it },icon=fieldIcon("Fecha (dd/mm/aaaa)"),label={ Text("Fecha (dd/mm/aaaa)") },singleLine=true)
            FormTextField(description,{ description=it },icon=fieldIcon("Descripción opcional"),label={ Text("Descripción opcional") })
            error?.let { Text(it,color=MaterialTheme.colorScheme.error,fontSize=12.sp) }
        }
    },confirmButton={ Button(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick={
        val cents=runCatching { parseCents(amount) }.getOrElse {
            amountError=if(amount.isBlank()) "Escribe el importe aquí para agregar dinero a la cuenta." else "Escribe un importe mayor a cero, por ejemplo 1900 o 1900.50."
            return@Button
        }
        runCatching {
            val updated=if(ledger.accounts.isEmpty()) ledger.addAccount(accountName) else ledger
            val formatter=DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)
            val movement=Movement(id=initialMovement?.id ?: java.util.UUID.randomUUID().toString(),type=type,cents=cents,account=account ?: updated.accounts.first().id,destination=if(type==Operation.TRANSFER) destination else null,
                description=description.trim(),date=LocalDate.parse(date,formatter),category=if(type==Operation.EXPENSE) category else "Otros",goal=goal,subcategory=subcategory)
            if(initialMovement==null) updated.post(movement) else updated.edit(movement)
        }.onSuccess(save).onFailure { error=if(it is java.time.format.DateTimeParseException) "Revisa la fecha; usa dd/mm/aaaa." else it.message ?: "No se pudo registrar el movimiento." }
    }) { Text("Registrar") } },dismissButton={ OutlinedButton(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick=dismiss) { Text("Cancelar") } })
}
@Composable
internal fun Choice(label: String,selected: String,options: List<String>,choose: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick={ open=true },modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),shape=androidx.compose.foundation.shape.RoundedCornerShape(13.dp)) {
            Glyph(if(label=="Categoría") categoryIcon(selected).icon else fieldIcon(label),MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("$label: $selected",Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Start)
            Glyph("chevronDown",MaterialTheme.colorScheme.onSurfaceVariant,Modifier.size(16.dp))
        }
        DropdownMenu(open,onDismissRequest={ open=false }) {
            options.forEachIndexed { index,option -> DropdownMenuItem(leadingIcon={ Glyph(if(label=="Categoría") categoryIcon(option).icon else fieldIcon(label),if(label=="Categoría") categoryIcon(option).color else MaterialTheme.colorScheme.onSurfaceVariant) },text={ Text(option) },onClick={ choose(index); open=false }) }
        }
    }
}
