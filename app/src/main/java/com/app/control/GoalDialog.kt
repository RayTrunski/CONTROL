package com.app.control

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@Composable
internal fun NewGoalDialog(dismiss: () -> Unit,save: (SavingsGoal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val icons=listOf("pig","plane","laptop","home","gift")
    AlertDialog(onDismissRequest=dismiss,title={ Text("Nuevo apartado") },text={
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name,{ name=it },label={ Text("Nombre del apartado") },singleLine=true)
            OutlinedTextField(target,{ target=it },label={ Text("Monto objetivo MXN") },singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
            OutlinedTextField(date,{ date=it },label={ Text("Fecha límite (opcional)") },placeholder={ Text("dd/mm/aaaa") },singleLine=true)
            Choice("Icono",listOf("Ahorro","Viaje","Laptop","Casa","Regalo")[icon],listOf("Ahorro","Viaje","Laptop","Casa","Regalo")) { icon=it }
            Text("Después podrás agregar fondos desde cualquiera de tus cuentas.")
            error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        }
    },confirmButton={ TextButton(onClick={
        runCatching {
            val deadline=if(date.isBlank()) null else LocalDate.parse(date,DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT))
            save(SavingsGoal(name=name.trim(),target=parseCents(target),deadline=deadline,icon=icons[icon]))
        }.onFailure { error=if(it is java.time.format.DateTimeParseException) "Revisa la fecha; usa dd/mm/aaaa." else it.message ?: "No se pudo crear el apartado." }
    }) { Text("Crear apartado") } },dismissButton={ TextButton(onClick=dismiss) { Text("Cancelar") } })
}
