package com.app.control

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class DashboardDataTest {
    @Test fun monthlySummaryExcludesTransfersAndReservationsAndOtherMonths() {
        val a=Account(name="Efectivo")
        val b=Account(name="Débito")
        val date=LocalDate.of(2026,10,10)
        val ledger=Ledger(listOf(a,b))
            .post(Movement(type=Operation.INCOME,cents=200000,account=a.id,date=date.minusMonths(1)))
            .post(Movement(type=Operation.INCOME,cents=100000,account=a.id,date=date))
            .post(Movement(type=Operation.EXPENSE,cents=25000,account=a.id,date=date,category="Alimentación"))
            .post(Movement(type=Operation.EXPENSE,cents=10000,account=a.id,date=date,category="Transporte"))
            .post(Movement(type=Operation.TRANSFER,cents=50000,account=a.id,destination=b.id,date=date))
            .post(Movement(type=Operation.RESERVE,cents=5000,account=a.id,date=date))
        val summary=ledger.monthlySummary(date)
        assertEquals(100000L,summary.income)
        assertEquals(35000L,summary.expenses)
        assertEquals(200000L,summary.previousIncome)
        assertEquals(0L,summary.previousExpenses)
        assertEquals(listOf("Alimentación" to 25000L,"Transporte" to 10000L),summary.categories)
        assertEquals(summary.income,summary.incomeBars.sum())
        assertEquals(summary.expenses,summary.expenseBars.sum())
    }
    @Test fun emptyMonthHasZeroTotalsAndEmptyCategories() {
        val summary=Ledger().monthlySummary(LocalDate.of(2026,1,1))
        assertEquals(0L,summary.income)
        assertEquals(0L,summary.expenses)
        assertTrue(summary.categories.isEmpty())
        assertEquals(List(8) { 0L },summary.incomeBars)
    }
    @Test fun previousMonthCrossesYearBoundaryAndLastDayIsIncluded() {
        val a=Account(name="Efectivo")
        val ledger=Ledger(listOf(a))
            .post(Movement(type=Operation.INCOME,cents=10000,account=a.id,date=LocalDate.of(2025,12,31)))
            .post(Movement(type=Operation.INCOME,cents=12345,account=a.id,date=LocalDate.of(2026,1,31)))
        val summary=ledger.monthlySummary(LocalDate.of(2026,1,31))
        assertEquals(10000L,summary.previousIncome)
        assertEquals(12345L,summary.incomeBars.last())
    }
}
