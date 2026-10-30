package com.app.control

import org.junit.Assert.*
import org.junit.Test

class FinanceTest {
    private val a = Account(name = "Efectivo")
    private val b = Account(name = "Débito")
    private fun funded() = Ledger(listOf(a, b)).post(Movement(type = Operation.INCOME, cents = 200000, account = a.id))
    private fun movement(type: Operation, amount: Long) = Movement(type = type, cents = amount, account = a.id)
    @Test fun incomeAndExpense() {
        val ledger = funded().post(movement(Operation.EXPENSE, 50000))
        assertEquals(150000L, ledger.balance(a.id))
    }
    @Test fun transferPreservesTotal() {
        val ledger = funded().post(Movement(type = Operation.TRANSFER, cents = 100000, account = a.id, destination = b.id))
        assertEquals(200000L, ledger.total)
        assertEquals(100000L, ledger.balance(b.id))
    }
    @Test fun reserveAndReleaseDoNotChangeTotal() {
        val ledger = funded().post(movement(Operation.RESERVE, 80000)).post(movement(Operation.RELEASE, 30000))
        assertEquals(200000L, ledger.total)
        assertEquals(150000L, ledger.available(a.id))
    }
    @Test fun editAndDeleteReverseEffects() {
        val expense = movement(Operation.EXPENSE, 20000)
        val ledger = funded().post(expense).edit(expense.copy(cents = 25000))
        assertEquals(175000L, ledger.total)
        assertEquals(200000L, ledger.delete(expense.id).total)
    }
    @Test fun reservedMoneyCannotBeSpent() {
        val ledger = funded().post(movement(Operation.RESERVE, 180000))
        assertThrows(IllegalArgumentException::class.java) { ledger.post(movement(Operation.EXPENSE, 25000)) }
        assertEquals(200000L, ledger.total)
    }
    @Test fun duplicateIdIsRejected() {
        val m = movement(Operation.EXPENSE, 100)
        val ledger = funded().post(m)
        assertThrows(IllegalArgumentException::class.java) { ledger.post(m) }
    }
    @Test fun deletingFundingCannotInvalidateReservation() {
        val ledger = funded().post(movement(Operation.RESERVE, 1000))
        assertThrows(IllegalArgumentException::class.java) { ledger.delete(ledger.movements.first().id) }
    }
    @Test fun invalidTransfersAndReleaseAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { funded().post(Movement(type = Operation.TRANSFER, cents = 1, account = a.id, destination = a.id)) }
        assertThrows(IllegalArgumentException::class.java) { funded().post(movement(Operation.RELEASE, 1)) }
    }
    @Test fun moneyParsingIsExact() {
        assertEquals(25050L, parseCents("250.50"))
        assertEquals(190000L, parseCents("1900."))
        assertEquals(190050L, parseCents("1900,50"))
        assertEquals(10L, parseCents("0.10"))
        listOf("0", "-1", "1.001", "NaN", "1,000").forEach { input ->
            assertThrows(IllegalArgumentException::class.java) { parseCents(input) }
        }
    }
}
