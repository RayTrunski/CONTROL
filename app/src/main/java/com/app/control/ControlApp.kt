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
import kotlinx.coroutines.launch

@Composable
fun ControlApp(vm: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val loaded by vm.ledger.collectAsState()
    val storageError by vm.error.collectAsState()
    val context=androidx.compose.ui.platform.LocalContext.current
    val scope=rememberCoroutineScope()
    if(loaded==null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) { Text(storageError ?: "Cargando tus finanzas…") }
        return
    }
    var ledger by object : MutableState<Ledger> { override var value: Ledger get()=loaded!!; set(value) { vm.save(value) }; override fun component1()=value; override fun component2(): (Ledger)->Unit = { value=it } }
    var section by rememberSaveable { mutableStateOf(Section.HOME) }
    val alias by vm.alias.collectAsState()
    var operation by remember { mutableStateOf<Operation?>(null) }
    var operationGoal by remember { mutableStateOf<String?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var analysis by remember { mutableStateOf(false) }
    var planning by remember { mutableStateOf(false) }
    val filter by vm.filter.collectAsState()
    val saving by vm.saving.collectAsState()
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var editingMovement by remember { mutableStateOf<Movement?>(null) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    val csv=androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if(uri!=null) scope.launch { runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(exportCsv(ledger)) } ?: error("No se pudo abrir el archivo.") } }.onFailure { vm.error.value=it.message } }
    }
    val backup=androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri!=null) scope.launch { runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(encodeLedger(ledger)) } ?: error("No se pudo abrir el archivo.") } }.onFailure { vm.error.value=it.message } }
    }
    val restore=androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) scope.launch { runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("No se pudo abrir el respaldo.") } }.onSuccess { pendingRestore=it }.onFailure { vm.error.value=it.message } }
    }
    var profileOpen by remember { mutableStateOf(false) }
    var accountOpen by remember { mutableStateOf(false) }
    var newGoal by remember { mutableStateOf(false) }
    var detailGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var detailMovement by remember { mutableStateOf<Movement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    BackHandler(planning) { planning=false }
    BackHandler(settings) { settings=false }
    Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={ if(!settings) BottomBar(if(planning) Section.HOME else section) { planning=false; section=it; error=null } }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if(settings) {
                val dark by vm.darkTheme.collectAsState()
                SettingsScreen(ledger,alias,dark,{ vm.setDarkTheme(it) },{ settings=false },
                    { profileOpen=true },{ accountOpen=true },{ csv.launch("control.csv") },
                    { backup.launch("control-respaldo.json") },{ restore.launch(arrayOf("application/json","text/plain")) },
                    { goal -> ledger=ledger.updateGoal(goal.copy(archived=false)) })
            } else if(planning) PlanningScreen(ledger,{ ledger=it },{ planning=false },{ settings=true }) else when(section) {
                Section.HOME -> HomeScreen(ledger,alias,{ settings=true },{ if(it==Operation.RESERVE) newGoal=true else { operationGoal=null; operation=it } },{ analysis=true },{ section=Section.MOVEMENTS },{ detailMovement=it },{ planning=true },filter,{ vm.setFilter(it) })
                Section.MOVEMENTS -> MovementsScreen(ledger,{ settings=true },{ operationGoal=null; operation=Operation.EXPENSE },{ detailMovement=it },filter,{ vm.setFilter(it) })
                Section.SAVINGS -> SavingsScreen(ledger,{ settings=true },{ newGoal=true },{ detailGoal=it; error=null })
            }
        }
    }
    operation?.let { selected -> MovementDialog(ledger,selected,operationGoal,{ operation=null; operationGoal=null },initialAccount=filter.account) { updated ->
        val added=updated.movements.last()
        ledger=updated
        // Keep the new record visible, even after browsing a historical or future period.
        val visibleFilter=filter.copy(account=added.account,category=if(added.type==Operation.EXPENSE) filter.category?.takeIf { it==added.category } else null,viewDate=added.date)
        vm.setFilter(if(visibleFilter.cadence==Cadence.CUSTOM && added.date !in visibleFilter.range()) visibleFilter.copy(customStart=added.date,customEnd=added.date) else visibleFilter)
        operation=null; operationGoal=null
    } }
    if(profileOpen) TextEntryDialog("Tu perfil","Nombre o alias (opcional)",alias,{ profileOpen=false }) { vm.setAlias(it.trim()); profileOpen=false }
    if(accountOpen) TextEntryDialog("Nueva cuenta","Nombre de la cuenta","",{ accountOpen=false }) { ledger=ledger.addAccount(it); accountOpen=false }
    if(newGoal) NewGoalDialog({ newGoal=false }) { goal -> ledger=ledger.addGoal(goal); newGoal=false; detailGoal=goal }
    detailGoal?.let { goal ->
        GoalDetailSheet(ledger,goal,{ detailGoal=null },
            { editingGoal=goal; detailGoal=null },
            { ledger=ledger.updateGoal(goal.copy(archived=true)); detailGoal=null },
            { operationGoal=goal.id; operation=Operation.RESERVE; detailGoal=null },
            { operationGoal=goal.id; operation=Operation.RELEASE; detailGoal=null })
    }
    detailMovement?.let { movement ->
        AlertDialog(onDismissRequest={ detailMovement=null; error=null },title={ Text(movement.description.ifBlank { movement.type.label() }) },text={
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(movement.signedAmount(),fontSize=24.sp,fontWeight=FontWeight.SemiBold)
                Text(movement.type.label())
                TextButton(onClick={ editingMovement=movement; detailMovement=null }) { Text("Editar movimiento") }
                Text(movement.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                Text("Cuenta: ${ledger.accounts.first { it.id==movement.account }.name}")
                movement.destination?.let { id -> Text("Destino: ${ledger.accounts.first { it.id==id }.name}") }
                if(movement.type==Operation.EXPENSE) Text("Categoría: ${movement.category}${if(movement.subcategory.isBlank()) "" else " / ${movement.subcategory}"}")
                movement.goal?.let { id -> Text("Apartado: ${ledger.goals.first { it.id==id }.name}") }
                error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            }
        },confirmButton={ TextButton(onClick={ detailMovement=null; error=null }) { Text("Cerrar") } },dismissButton={ TextButton(onClick={ runCatching { ledger.delete(movement.id) }.onSuccess { ledger=it; detailMovement=null; error=null }.onFailure { error=it.message } }) { Text("Eliminar") } })
    }
    editingGoal?.let { goal -> NewGoalDialog({ editingGoal=null },goal) { ledger=ledger.updateGoal(it); editingGoal=null } }
    editingMovement?.let { movement -> MovementDialog(ledger,movement.type,movement.goal,{ editingMovement=null },movement) { ledger=it; editingMovement=null } }
    if(storageError!=null) AlertDialog(onDismissRequest={ vm.error.value=null },title={ Text("No se completó la operación") },text={ Text(storageError.orEmpty()) },confirmButton={ TextButton(onClick={ vm.error.value=null }) { Text("Cerrar") } })
    pendingRestore?.let { text -> AlertDialog(onDismissRequest={ pendingRestore=null },title={ Text("Restaurar respaldo") },text={ Text("El respaldo reemplazará los datos financieros actuales. Guarda un respaldo antes de continuar.") },confirmButton={ TextButton(onClick={ vm.restore(text); pendingRestore=null }) { Text("Restaurar") } },dismissButton={ TextButton(onClick={ pendingRestore=null }) { Text("Cancelar") } }) }
    if(saving) androidx.compose.ui.window.Dialog(onDismissRequest={}) { Surface { Row(Modifier.padding(24.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) { CircularProgressIndicator(); Text("Guardando…") } } }
    if(analysis) AlertDialog(onDismissRequest={ analysis=false },title={ Text("Gastos del periodo") },text={ CategoryChart(ledger.analysisSummary(filter)) },confirmButton={ TextButton(onClick={ analysis=false }) { Text("Cerrar") } })
}



