package com.app.control

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class PlanningScreenTest {
    @get:Rule val compose=createComposeRule()
    @Test fun confirmingFutureExpenseChangesRealBalanceOnlyOnce() {
        val account=Account(name="Efectivo")
        val plan=PlannedOperation(type=Operation.EXPENSE,cents=25000,description="Pago de internet",date=YearMonth.now().plusMonths(1).atDay(2),account=account.id,category="Servicios")
        val state=mutableStateOf(Ledger(accounts=listOf(account)).post(Movement(type=Operation.INCOME,cents=100000,account=account.id)).savePlan(plan))
        compose.setContent { MaterialTheme { PlanningScreen(state.value,{ state.value=it },{}) } }
        compose.onNodeWithText("Planificación").assertIsDisplayed()
        compose.onNodeWithText("Pago de internet").performScrollTo().performClick()
        compose.onNodeWithText("Confirmar").performScrollTo().performClick()
        compose.onNode(hasText("Confirmar") and hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle {
            assertEquals(75000L,state.value.total)
            assertEquals(PlanStatus.EXECUTED,state.value.plans.single().status)
            assertEquals(2,state.value.movements.size)
            assertEquals(state.value,state.value.confirmPlan(plan,account.id))
        }
        compose.onNodeWithText("Ejecutado").performScrollTo().assertIsDisplayed()
    }
}

