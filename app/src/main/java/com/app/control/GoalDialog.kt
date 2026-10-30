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
internal fun NewGoalDialog(dismiss: () -> Unit,initial: SavingsGoal? = null,save: (SavingsGoal) -> Unit) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var target by remember { mutableStateOf(initial?.target?.let { java.math.BigDecimal.valueOf(it,2).toPlainString() }.orEmpty()) }
    var date by remember { mutableStateOf(initial?.deadline?.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")).orEmpty()) }
    var icon by remember { mutableStateOf(listOf("pig","plane","laptop","home","gift").indexOf(initial?.icon).coerceAtLeast(0)) }
    var error by remember { mutableStateOf<String?>(null) }
    val icons=listOf("pig","plane","laptop","home","gift")
    FormDialog(onDismissRequest=dismiss,icon="pig",subtitle="Define tu meta y organiza el dinero para alcanzarla.",title={ Text(if(initial==null) "Nuevo apartado" else "Editar apartado") },text={
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            FormTextField(name,{ name=it },icon=fieldIcon("Nombre del apartado"),label={ Text("Nombre del apartado") },singleLine=true)
            FormTextField(target,{ target=it },icon=fieldIcon("Monto objetivo MXN"),label={ Text("Monto objetivo MXN") },singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
            FormTextField(date,{ date=it },icon=fieldIcon("Fecha límite (opcional)"),label={ Text("Fecha límite (opcional)") },placeholder={ Text("dd/mm/aaaa") },singleLine=true)
            Choice("Icono",listOf("Ahorro","Viaje","Laptop","Casa","Regalo")[icon],listOf("Ahorro","Viaje","Laptop","Casa","Regalo")) { icon=it }
            Text("Después podrás agregar fondos desde cualquiera de tus cuentas.")
            error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        }
    },confirmButton={ Button(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick={
        runCatching {
            val deadline=if(date.isBlank()) null else LocalDate.parse(date,DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT))
            save(SavingsGoal(id=initial?.id ?: java.util.UUID.randomUUID().toString(),name=name.trim(),target=parseCents(target),deadline=deadline,icon=icons[icon]))
        }.onFailure { error=if(it is java.time.format.DateTimeParseException) "Revisa la fecha; usa dd/mm/aaaa." else it.message ?: "No se pudo crear el apartado." }
    }) { Text(if(initial==null) "Crear apartado" else "Guardar cambios") } },dismissButton={ OutlinedButton(modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),onClick=dismiss) { Text("Cancelar") } })
}


