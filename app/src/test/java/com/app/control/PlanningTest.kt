package com.app.control

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class PlanningTest {
    @Test fun browsingHistoricalMonthFiltersMovementsInThatMonth() {
        val filter=AnalysisFilter(viewDate=LocalDate.of(2024,2,20))
        assertEquals(LocalDate.of(2024,2,1)..LocalDate.of(2024,2,29),filter.range())
        assertTrue(filter.matches(Movement(type=Operation.EXPENSE,cents=100,account="a",date=LocalDate.of(2024,2,29))))
        assertFalse(filter.matches(Movement(type=Operation.EXPENSE,cents=100,account="a",date=LocalDate.of(2024,3,1))))
    }
    @Test fun browsingQuincenaAcrossYearBoundaryPreservesCycle() {
        val filter=AnalysisFilter(cadence=Cadence.SEMIMONTHLY,viewDate=LocalDate.of(2026,12,30))
        val current=filter.range()
        val next=filter.copy(viewDate=current.endInclusive.plusDays(1))
        assertEquals(LocalDate.of(2026,12,31)..LocalDate.of(2027,1,14),next.range())
        assertEquals(current,next.copy(viewDate=next.range().start.minusDays(1)).range())
    }
    private val today=LocalDate.now()
    private val month=YearMonth.from(today).plusMonths(1)
    private val a=Account(name="Nómina")
    private val goal=SavingsGoal(name="Emergencias",target=50000)
    private fun funded()=Ledger(accounts=listOf(a),goals=listOf(goal)).post(Movement(type=Operation.INCOME,cents=100000,account=a.id))
    private fun plan(type: Operation=Operation.EXPENSE,cents: Long=25000)=PlannedOperation(type=type,cents=cents,description="Previsto",date=month.atDay(1),account=a.id,goal=if(type==Operation.RESERVE) goal.id else null)
    @Test fun weeklyIncludesFivePaydaysAndBiweeklyDoesNotBecomeSemimonthly() {
        val weekly=IncomeSource(name="Salario",cents=10000,account=a.id,cadence=Cadence.WEEKLY,anchor=LocalDate.of(2027,1,1))
        assertEquals(listOf(1,8,15,22,29),weekly.dates(YearMonth.of(2027,1)).map { it.dayOfMonth })
        assertEquals(listOf(1,15,29),weekly.copy(cadence=Cadence.BIWEEKLY).dates(YearMonth.of(2027,1)).map { it.dayOfMonth })
        assertEquals(listOf(15,28),weekly.copy(cadence=Cadence.SEMIMONTHLY).dates(YearMonth.of(2027,2)).map { it.dayOfMonth })
        assertEquals(listOf(28),weekly.copy(cadence=Cadence.MONTHLY,anchor=LocalDate.of(2027,1,31)).dates(YearMonth.of(2027,2)).map { it.dayOfMonth })
    }
    @Test fun plansDoNotChangeRealBalancesAndEditingRecalculatesFutureMonths() {
        val p=plan();val ledger=funded().savePlan(p)
        assertEquals(100000L,ledger.total)
        assertEquals(75000L,ledger.projection(month).closing)
        assertEquals(50000L,ledger.savePlan(p.copy(cents=50000)).projection(month.plusMonths(1)).opening)
        assertEquals(100000L,ledger.cancelPlan(p).projection(month).closing)
    }
    @Test fun contributionReducesFreeMoneyOnceButPreservesAssets() {
        val p=plan(Operation.RESERVE)
        val ledger=funded().savePlan(p)
        val projected=ledger.projection(month)
        assertEquals(100000L,projected.assets);assertEquals(75000L,projected.closing);assertEquals(0L,ledger.held)
        val executed=ledger.confirmPlan(p,a.id)
        assertEquals(100000L,executed.total);assertEquals(25000L,executed.held)
        assertEquals(75000L,executed.projection(month).closing)
    }
    @Test fun repeatedConfirmationCreatesOnlyOneRealIncome() {
        val p=plan(Operation.INCOME);val ledger=funded().savePlan(p)
        val executed=ledger.confirmPlan(p,a.id).confirmPlan(p,a.id)
        assertEquals(125000L,executed.total);assertEquals(2,executed.movements.size)
        assertEquals(125000L,executed.projection(month).closing)
        assertEquals(PlanStatus.EXECUTED,executed.plans.single().status)
    }
    @Test fun generatedIncomeConfirmationOverridesOccurrenceAndRecalculates() {
        val ledger=funded().addSource(IncomeSource(name="Salario",cents=10000,account=a.id,cadence=Cadence.WEEKLY,anchor=month.atDay(1)))
        val occurrence=ledger.plansFor(month).first()
        val before=ledger.projection(month).closing
        val executed=ledger.confirmPlan(occurrence,a.id)
        assertEquals(before,executed.projection(month).closing)
        assertEquals(executed,executed.confirmPlan(occurrence,a.id))
    }
    @Test fun negativeProjectionNeverChangesRealMoney() {
        val ledger=funded().savePlan(plan(cents=200000))
        assertEquals(-100000L,ledger.projection(month).closing);assertEquals(100000L,ledger.total)
        assertThrows(IllegalArgumentException::class.java) { ledger.confirmPlan(ledger.plans.single(),a.id) }
    }
    @Test fun completedGoalCanReopenAndInternalOperationsDoNotCountAsIncome() {
        val ledger=funded().post(Movement(type=Operation.RESERVE,cents=50000,account=a.id,goal=goal.id)).updateGoal(goal.copy(target=80000))
        assertTrue(ledger.goalBalance(goal.id)<ledger.goals.single().target)
        val b=Account(name="Efectivo")
        val transferred=ledger.copy(accounts=ledger.accounts+b).post(Movement(type=Operation.TRANSFER,cents=10000,account=a.id,destination=b.id))
        val summary=transferred.analysisSummary(AnalysisFilter())
        assertEquals(100000L,summary.income);assertEquals(0L,summary.expenses);assertEquals(100000L,transferred.total)
    }
    @Test fun periodAnchorsAreConsistentAcrossBoundaries() {
        val reference=LocalDate.of(2027,1,4)
        val filter=AnalysisFilter(cadence=Cadence.BIWEEKLY,anchor=reference)
        assertEquals(reference..reference.plusDays(13),filter.range(reference.plusDays(12)))
        assertEquals(reference.minusDays(14)..reference.minusDays(1),filter.range(reference.minusDays(1)))
        assertEquals(LocalDate.of(2027,2,15)..LocalDate.of(2027,2,27),filter.copy(cadence=Cadence.SEMIMONTHLY).range(LocalDate.of(2027,2,20)))
    }
    @Test fun sharedFiltersMatchDashboardTotalsWithoutChangingCurrentBalances() {
        val b=Account(name="Efectivo")
        val expense=Movement(type=Operation.EXPENSE,cents=1000,account=a.id,category="Servicios",description="Internet")
        val transfer=Movement(type=Operation.TRANSFER,cents=10000,account=a.id,destination=b.id)
        val ledger=funded().copy(accounts=listOf(a,b)).post(expense).post(transfer)
        val filter=AnalysisFilter(account=a.id,category="Servicios")
        val summary=ledger.analysisSummary(filter)
        assertEquals(listOf(expense),ledger.movements.filter(filter::matches))
        assertEquals(1000L,summary.expenses);assertEquals(0L,summary.income)
        assertEquals(99000L,ledger.total)
        assertEquals(listOf(transfer),ledger.movements.filter(AnalysisFilter(account=b.id)::matches))
        Cadence.entries.forEach { cadence ->
            val period=filter.copy(cadence=cadence,customStart=today,customEnd=today)
            assertEquals(1000L,ledger.analysisSummary(period).expenses)
            assertEquals(99000L,ledger.total)
        }
    }
}
