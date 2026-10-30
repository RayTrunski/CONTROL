package com.app.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormDialog(
    onDismissRequest: () -> Unit, title: @Composable () -> Unit,
    subtitle: String, icon: String = "document", text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit, dismissButton: @Composable () -> Unit
) {
    ModalBottomSheet(onDismissRequest=onDismissRequest,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),
        containerColor=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(topStart=28.dp,topEnd=28.dp)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal=20.dp).padding(bottom=16.dp)) {
            Row(Modifier.padding(bottom=20.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.Top) {
                Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.primaryContainer) {
                    Box(Modifier.size(52.dp),contentAlignment=Alignment.Center) { Glyph(icon,MaterialTheme.colorScheme.primary,Modifier.size(27.dp)) }
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontWeight=FontWeight.Bold)) { title() }
                    Text(subtitle,fontSize=13.sp,lineHeight=18.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState()).padding(bottom=12.dp)) { text() }
            HorizontalDivider(color=MaterialTheme.colorScheme.outline.copy(alpha=.15f))
            Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { dismissButton() }
                Box(Modifier.weight(1f)) { confirmButton() }
            }
        }
    }
}

@Composable
internal fun FormTextField(
    value: String, onValueChange: (String) -> Unit, label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null, singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null, icon: String = "document"
) {
    OutlinedTextField(value,onValueChange,modifier=Modifier.fillMaxWidth(),label=label,placeholder=placeholder,
        singleLine=singleLine,keyboardOptions=keyboardOptions,isError=isError,leadingIcon={ Glyph(icon,MaterialTheme.colorScheme.onSurfaceVariant) },
        supportingText=if(isError) supportingText else null,shape=RoundedCornerShape(13.dp),
        colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=MaterialTheme.colorScheme.outline.copy(alpha=.6f)))
}

@Composable
internal fun OperationPicker(types: List<Operation>,value: Operation,onSelect: (Operation) -> Unit) {
    Surface(shape=RoundedCornerShape(18.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline.copy(alpha=.25f))) {
        Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Tipo de operación",fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    val selected=value==type
                    val accent=type.iconStyle().color
                    Surface(onClick={ onSelect(type) },modifier=Modifier.weight(1f).semantics { this.selected=selected },shape=RoundedCornerShape(14.dp),
                        color=if(selected) MaterialTheme.colorScheme.primaryContainer else accent.copy(alpha=.07f),
                        border=if(selected) BorderStroke(1.dp,MaterialTheme.colorScheme.primary) else null) {
                        Column(Modifier.padding(vertical=12.dp,horizontal=3.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                            Glyph(type.iconStyle().icon,accent,Modifier.size(27.dp))
                            Text(type.label(),fontSize=12.sp,fontWeight=if(selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}
