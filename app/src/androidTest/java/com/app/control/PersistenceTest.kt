package com.app.control

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class PersistenceTest {
    private fun fixture(): Ledger {
        val account=Account(name="Prueba")
        val goal=SavingsGoal(name="Meta",target=50000)
        val month=YearMonth.now().plusMonths(1)
        val ledger=Ledger(accounts=listOf(account),goals=listOf(goal))
            .post(Movement(type=Operation.INCOME,cents=100000,account=account.id))
            .addSource(IncomeSource(name="Semanal",cents=12000,account=account.id,cadence=Cadence.WEEKLY,anchor=month.atDay(1)))
            .savePlan(PlannedOperation(type=Operation.RESERVE,cents=10000,description="Ahorro previsto",date=month.atDay(2),account=account.id,goal=goal.id))
            .savePlan(PlannedOperation(type=Operation.EXPENSE,cents=5000,description="Internet",date=month.atDay(3),category="Servicios",subcategory="Internet"))
        return ledger.confirmPlan(ledger.plansFor(month).first { it.source!=null },account.id)
    }
    @Test fun backupRestoresHistoryPlansAndConfirmationLinksExactly() {
        val ledger=fixture()
        val restored=decodeLedger(encodeLedger(ledger))
        val archived=ledger.updateGoal(ledger.goals.single().copy(archived=true))
        assertEquals(archived,decodeLedger(encodeLedger(archived)))
        assertEquals(ledger,restored)
        assertEquals(ledger.projection(YearMonth.now().plusMonths(2)),restored.projection(YearMonth.now().plusMonths(2)))
        val executed=restored.plans.first { it.status==PlanStatus.EXECUTED }
        assertEquals(restored,restored.confirmPlan(executed,restored.accounts.first().id))
        val csv=exportCsv(restored)
        assertTrue(csv.contains("Importe real MXN"));assertTrue(csv.contains("Importe previsto MXN"));assertTrue(csv.contains("Internet"));assertTrue(csv.contains("Ejecutado"))
        val invalid=org.json.JSONObject(encodeLedger(ledger))
        invalid.getJSONArray("movements").getJSONObject(0).put("cents",1.25)
        assertThrows(NumberFormatException::class.java) { decodeLedger(invalid.toString()) }
    }
    @Test fun databaseReopenPreservesHistoricalAndFutureData()=runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val name="persistence-${java.util.UUID.randomUUID()}.db"
        val ledger=fixture()
        val first=FinanceRepository(context,name)
        try { first.save(ledger) } finally { first.close() }
        val reopened=FinanceRepository(context,name)
        try { assertEquals(ledger,reopened.load()) } finally { reopened.close(); context.deleteDatabase(name) }
    }
    @Test fun failedRoomTransactionCannotLeavePartialLedger()=runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val db=Room.inMemoryDatabaseBuilder(context,FinanceDatabase::class.java).build()
        val original=encodeLedger(fixture())
        try {
            db.financeDao().write(FinancialState(json=original))
            try { db.withTransaction { db.financeDao().write(FinancialState(json=encodeLedger(Ledger()))); throw IllegalStateException("Interrupción simulada") } } catch(_: IllegalStateException) { }
            assertEquals(original,db.financeDao().read()!!.json)
        } finally { db.close() }
    }
}
