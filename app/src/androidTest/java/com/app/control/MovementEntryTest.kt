package com.app.control

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MovementEntryTest {
    @get:Rule val compose=createComposeRule()

    @Test fun successiveIncomeUsesSelectedAccountAndAccumulates() {
        val first=Account(name="NU")
        val second=Account(name="BBVA")
        val state=mutableStateOf(Ledger(accounts=listOf(first,second)))
        val open=mutableStateOf(true)
        compose.setContent {
            MaterialTheme {
                if(open.value) MovementDialog(state.value,Operation.INCOME,null,{ open.value=false },initialAccount=second.id) { state.value=it; open.value=false }
            }
        }
        compose.onNodeWithText("Cuenta: BBVA").assertIsDisplayed()
        compose.onNodeWithText("Importe MXN").performTextInput("2000")
        compose.onNodeWithText("Registrar",substring=false).performClick()
        compose.runOnIdle { open.value=true }
        compose.onNodeWithText("Cuenta: BBVA").assertIsDisplayed()
        compose.onNodeWithText("Importe MXN").performTextInput("1900.")
        compose.onNodeWithText("Registrar",substring=false).performClick()
        compose.runOnIdle {
            assertFalse(open.value)
            assertEquals(390000L,state.value.balance(second.id))
            assertEquals(0L,state.value.balance(first.id))
            assertEquals(2,state.value.movements.size)
            assertEquals(2,state.value.movements.map { it.id }.distinct().size)
        }
    }

    @Test fun accountCanBeChangedAndMissingAmountStaysOnAmountField() {
        val first=Account(name="NU")
        val second=Account(name="BBVA")
        var saved: Ledger?=null
        compose.setContent { MaterialTheme { MovementDialog(Ledger(accounts=listOf(first,second)),Operation.INCOME,null,{},initialAccount=second.id) { saved=it } } }
        compose.onNodeWithText("Cuenta: BBVA").performClick()
        compose.onNodeWithText("NU",substring=false).performClick()
        compose.onNodeWithText("Descripción opcional").performTextInput("1900")
        compose.onNodeWithText("Registrar",substring=false).performClick()
        compose.onNodeWithText("Escribe el importe aquí para agregar dinero a la cuenta.").assertExists()
        compose.runOnIdle { assertNull(saved) }
        compose.onNodeWithText("Importe MXN").performTextInput("1900,50")
        compose.onNodeWithText("Registrar",substring=false).performClick()
        compose.runOnIdle {
            assertEquals(190050L,saved!!.balance(first.id))
            assertEquals(0L,saved!!.balance(second.id))
        }
    }

    @Test fun movementsShowSelectedAccountAndPeriod() {
        val first=Account(name="NU")
        val second=Account(name="BBVA")
        val ledger=Ledger(accounts=listOf(first,second))
            .post(Movement(type=Operation.INCOME,cents=200000,account=first.id,description="Ingreso NU"))
            .post(Movement(type=Operation.INCOME,cents=190000,account=second.id,description="Ingreso BBVA"))
        val filter=mutableStateOf(AnalysisFilter(account=second.id))
        compose.setContent { MaterialTheme { MovementsScreen(ledger,{},{},{},filter.value,{ filter.value=it }) } }
        compose.onNodeWithText("Ingreso BBVA").assertExists()
        compose.onNodeWithText("Ingreso NU").assertDoesNotExist()
        compose.runOnIdle { filter.value=filter.value.copy(account=first.id) }
        compose.onNodeWithText("Ingreso NU").assertExists()
        compose.onNodeWithText("Ingreso BBVA").assertDoesNotExist()
    }
}
