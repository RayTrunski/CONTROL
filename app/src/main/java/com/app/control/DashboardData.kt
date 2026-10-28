package com.app.control

import java.time.LocalDate
import java.time.YearMonth

val expenseCategories = listOf("Alimentación", "Vivienda", "Transporte", "Servicios", "Salud", "Entretenimiento", "Educación", "Otros")
data class MonthlySummary(val income: Long, val expenses: Long, val previousIncome: Long, val previousExpenses: Long, val incomeBars: List<Long>, val expenseBars: List<Long>, val categories: List<Pair<String, Long>>)
fun Ledger.monthlySummary(today: LocalDate = LocalDate.now()): MonthlySummary {
    val month = YearMonth.from(today)
    val current = movements.filter { YearMonth.from(it.date) == month }
    val previous = movements.filter { YearMonth.from(it.date) == month.minusMonths(1) }
    fun List<Movement>.total(type: Operation) = filter { it.type == type }.fold(0L) { sum, m -> Math.addExact(sum, m.cents) }
    fun bars(type: Operation) = (0..7).map { index ->
        current.filter { ((it.date.dayOfMonth - 1) * 8 / month.lengthOfMonth()) == index }.total(type)
    }
    return MonthlySummary(current.total(Operation.INCOME), current.total(Operation.EXPENSE), previous.total(Operation.INCOME), previous.total(Operation.EXPENSE), bars(Operation.INCOME), bars(Operation.EXPENSE),
        current.filter { it.type == Operation.EXPENSE }.groupBy { it.category }.map { (name, entries) -> name to entries.total(Operation.EXPENSE) }.sortedByDescending { it.second })
}
