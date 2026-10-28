package com.app.control

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class SavingsAndFiltersTest {
    private val a=Account(name="Efectivo")
    private val b=Account(name="Débito")
    private val goal=SavingsGoal(name="Vacaciones",target=50000)
    private fun funded()=Ledger(listOf(a,b)).post(Movement(type=Operation.INCOME,cents=100000,account=a.id)).addGoal(goal)
    @Test fun goalDepositsAndWithdrawalsPreserveTotalAndUpdateAvailability() {
        val ledger=funded().post(Movement(type=Operation.RESERVE,cents=30000,account=a.id,goal=goal.id))
            .post(Movement(type=Operation.RELEASE,cents=5000,account=a.id,goal=goal.id))
        assertEquals(100000L,ledger.total)
        assertEquals(25000L,ledger.goalBalance(goal.id))
        assertEquals(75000L,ledger.available(a.id))
        assertEquals(0L,ledger.monthlySummary().expenses)
    }
    @Test fun cannotWithdrawFromAnotherGoalOrAccount() {
        val other=SavingsGoal(name="Laptop",target=100000)
        val ledger=funded().addGoal(other).post(Movement(type=Operation.RESERVE,cents=10000,account=a.id,goal=goal.id))
        assertThrows(IllegalArgumentException::class.java) { ledger.post(Movement(type=Operation.RELEASE,cents=1,account=a.id,goal=other.id)) }
        assertThrows(IllegalArgumentException::class.java) { ledger.post(Movement(type=Operation.RELEASE,cents=1,account=b.id,goal=goal.id)) }
        assertThrows(IllegalArgumentException::class.java) { ledger.post(Movement(type=Operation.RELEASE,cents=1,account=a.id)) }
    }
    @Test fun goalCannotBeAttachedToIncomeAndUnknownGoalIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { funded().post(Movement(type=Operation.INCOME,cents=1,account=a.id,goal=goal.id)) }
        assertThrows(IllegalArgumentException::class.java) { funded().post(Movement(type=Operation.RESERVE,cents=1,account=a.id,goal="missing")) }
    }
    @Test fun deletingReservationCannotConsumeOtherGoalFunds() {
        val other=SavingsGoal(name="Laptop",target=100000)
        val deposit=Movement(type=Operation.RESERVE,cents=10000,account=a.id,goal=goal.id)
        val ledger=funded().addGoal(other).post(deposit)
            .post(Movement(type=Operation.RESERVE,cents=10000,account=a.id,goal=other.id))
            .post(Movement(type=Operation.RELEASE,cents=1000,account=a.id,goal=goal.id))
        assertThrows(IllegalArgumentException::class.java) { ledger.delete(deposit.id) }
    }
    @Test fun goalsRequirePositiveTargetsAndUniqueNames() {
        assertThrows(IllegalArgumentException::class.java) { funded().addGoal(SavingsGoal(name=" vacaciones ",target=1)) }
        assertThrows(IllegalArgumentException::class.java) { funded().addGoal(SavingsGoal(name="",target=1)) }
        assertThrows(IllegalArgumentException::class.java) { funded().addGoal(SavingsGoal(name="Laptop",target=0)) }
    }
    @Test fun filtersCombineDateAccountCategoryAndDescription() {
        val date=LocalDate.of(2026,10,20)
        val expense=Movement(type=Operation.EXPENSE,cents=1000,account=a.id,description="Supermercado",date=date,category="Alimentación")
        val ledger=funded().post(expense).post(expense.copy(id="older",date=date.minusMonths(1)))
        assertEquals(listOf(expense),ledger.filteredMovements("SUPER",Operation.EXPENSE,YearMonth.from(date),a.id,"Alimentación"))
        assertTrue(ledger.filteredMovements("SUPER",Operation.EXPENSE,YearMonth.from(date),b.id,null).isEmpty())
        assertEquals(2,ledger.filteredMovements("Super",null,null,null,null).size)
    }
    @Test fun transferAppearsForBothSourceAndDestinationWithoutInventingIncome() {
        val transfer=Movement(type=Operation.TRANSFER,cents=100,account=a.id,destination=b.id)
        val ledger=funded().post(transfer)
        assertEquals(listOf(transfer),ledger.filteredMovements("",Operation.TRANSFER,null,b.id,null))
        assertTrue(ledger.filteredMovements("",Operation.INCOME,null,b.id,null).isEmpty())
    }
}
