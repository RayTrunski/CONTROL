package com.app.control

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID

enum class Cadence(val title: String) { WEEKLY("Semanal"), BIWEEKLY("Catorcenal"), SEMIMONTHLY("Quincenal"), MONTHLY("Mensual"), CUSTOM("Personalizado") }
enum class PlanStatus(val title: String) { PENDING("Pendiente"), EXECUTED("Ejecutado"), CANCELLED("Cancelado") }
data class IncomeSource(val id: String = UUID.randomUUID().toString(), val name: String, val cents: Long, val account: String, val cadence: Cadence, val anchor: LocalDate, val firstDay: Int = 15, val secondDay: Int = 31, val end: LocalDate? = null)
data class PlannedOperation(val id: String = UUID.randomUUID().toString(), val type: Operation, val cents: Long, val description: String, val date: LocalDate, val account: String? = null, val category: String = "Otros", val subcategory: String = "", val goal: String? = null, val status: PlanStatus = PlanStatus.PENDING, val movementId: String? = null, val executedDate: LocalDate? = null, val source: String? = null)
data class Projection(val opening: Long, val income: Long, val expenses: Long, val contributions: Long, val closing: Long, val assets: Long)
data class AnalysisFilter(val account: String? = null, val category: String? = null, val cadence: Cadence = Cadence.MONTHLY, val anchor: LocalDate = LocalDate.now(), val firstDay: Int = 15, val secondDay: Int = 31, val customStart: LocalDate = LocalDate.now(), val customEnd: LocalDate = LocalDate.now(), val viewDate: LocalDate? = null) {
    fun range(today: LocalDate = viewDate ?: LocalDate.now()): ClosedRange<LocalDate> = when(cadence) {
        Cadence.WEEKLY, Cadence.BIWEEKLY -> {
            val days = if(cadence == Cadence.WEEKLY) 7L else 14L
            val start = anchor.plusDays(Math.floorDiv(ChronoUnit.DAYS.between(anchor, today), days) * days)
            start..start.plusDays(days - 1)
        }
        Cadence.MONTHLY -> YearMonth.from(today).atDay(1)..YearMonth.from(today).atEndOfMonth()
        Cadence.SEMIMONTHLY -> {
            val month = YearMonth.from(today)
            val boundaries = (-1L..1L).flatMap { offset -> val m=month.plusMonths(offset); listOf(m.atDay(firstDay.coerceAtMost(m.lengthOfMonth())),m.atDay(secondDay.coerceAtMost(m.lengthOfMonth()))) }.distinct().sorted()
            val start=boundaries.last { it<=today }
            start..boundaries.first { it>today }.minusDays(1)
        }
        Cadence.CUSTOM -> customStart..customEnd
    }
    fun matches(m: Movement): Boolean = m.date in range() && (account==null || m.account==account || m.destination==account) && (category==null || (m.type==Operation.EXPENSE && m.category==category))
}
fun IncomeSource.dates(month: YearMonth): List<LocalDate> {
    val dates=when(cadence) {
        Cadence.WEEKLY, Cadence.BIWEEKLY -> {
            val step=if(cadence==Cadence.WEEKLY) 7L else 14L
            var date=anchor.plusDays(Math.floorDiv(ChronoUnit.DAYS.between(anchor,month.atDay(1)),step)*step)
            if(date<month.atDay(1)) date=date.plusDays(step)
            buildList { while(date<=month.atEndOfMonth()) { add(date); date=date.plusDays(step) } }
        }
        Cadence.SEMIMONTHLY -> listOf(firstDay,secondDay).map { month.atDay(it.coerceAtMost(month.lengthOfMonth())) }.distinct()
        Cadence.MONTHLY -> listOf(month.atDay(anchor.dayOfMonth.coerceAtMost(month.lengthOfMonth())))
        Cadence.CUSTOM -> listOf(anchor).filter { YearMonth.from(it)==month }
    }
    return dates.filter { it>=anchor && (end==null || it<=end) }
}
fun Ledger.plansFor(month: YearMonth): List<PlannedOperation> {
    val generated=sources.flatMap { source -> source.dates(month).map { date -> PlannedOperation(id="${source.id}:$date",type=Operation.INCOME,cents=source.cents,description=source.name,date=date,account=source.account,source=source.id) } }
    return (plans.filter { YearMonth.from(it.date)==month }+generated.filter { g -> plans.none { it.id==g.id } }).sortedBy { it.date }
}
fun Ledger.savePlan(plan: PlannedOperation): Ledger {
    require(plan.status==PlanStatus.PENDING && plans.none { it.id==plan.id && it.status!=PlanStatus.PENDING }) { "La operación ya fue ejecutada o cancelada." }
    require(plan.type in listOf(Operation.INCOME,Operation.EXPENSE,Operation.RESERVE))
    require(plan.cents>0 && plan.description.isNotBlank()) { "Indica concepto e importe positivo." }
    require(plan.account==null || accounts.any { it.id==plan.account }) { "Cuenta inválida." }
    if(plan.type==Operation.RESERVE) require(goals.any { it.id==plan.goal }) { "Selecciona un apartado." }
    return copy(plans=plans.filterNot { it.id==plan.id }+plan)
}
fun Ledger.confirmPlan(plan: PlannedOperation, account: String, today: LocalDate = LocalDate.now()): Ledger {
    val current=plans.firstOrNull { it.id==plan.id } ?: plansFor(YearMonth.from(plan.date)).firstOrNull { it.id==plan.id } ?: error("Planificación inexistente.")
    if(current.status==PlanStatus.EXECUTED) return this
    require(current.status==PlanStatus.PENDING) { "La operación está cancelada." }
    val movement=Movement(id="plan:${current.id}",type=current.type,cents=current.cents,account=account,description=current.description,date=today,category=current.category,goal=current.goal,subcategory=current.subcategory)
    return post(movement).copy(plans=plans.filterNot { it.id==current.id }+current.copy(status=PlanStatus.EXECUTED,movementId=movement.id,executedDate=today,account=account))
}
fun Ledger.cancelPlan(plan: PlannedOperation): Ledger {
    require(plans.none { it.id==plan.id && it.status==PlanStatus.EXECUTED }) { "La operación ya se ejecutó." }
    return copy(plans=plans.filterNot { it.id==plan.id }+plan.copy(status=PlanStatus.CANCELLED))
}
fun Ledger.removePlan(plan: PlannedOperation): Ledger = if(plan.source!=null) cancelPlan(plan) else copy(plans=plans.filterNot { it.id==plan.id })
fun Ledger.projection(month: YearMonth, today: LocalDate = LocalDate.now()): Projection {
    require(month>=YearMonth.from(today))
    var free=Math.subtractExact(total,held)
    var assets=total
    var cursor=YearMonth.from(today)
    while(cursor<=month) {
        val pending=plansFor(cursor).filter { it.status==PlanStatus.PENDING && it.date>=today }
        fun sum(type: Operation)=pending.filter { it.type==type }.fold(0L) { a,p -> Math.addExact(a,p.cents) }
        val income=sum(Operation.INCOME); val expenses=sum(Operation.EXPENSE); val reserves=sum(Operation.RESERVE)
        val closing=Math.subtractExact(Math.addExact(free,income),Math.addExact(expenses,reserves))
        assets=Math.subtractExact(Math.addExact(assets,income),expenses)
        if(cursor==month) return Projection(free,income,expenses,reserves,closing,assets)
        free=closing; cursor=cursor.plusMonths(1)
    }
    error("Periodo inválido")
}
fun Ledger.updateGoal(goal: SavingsGoal): Ledger {
    require(goals.any { it.id==goal.id } && goal.name.isNotBlank() && goal.target>0)
    require(goals.none { it.id!=goal.id && it.name.equals(goal.name.trim(),true) }) { "El apartado ya existe." }
    return copy(goals=goals.map { if(it.id==goal.id) goal.copy(name=goal.name.trim()) else it })
}
fun Ledger.addSource(source: IncomeSource): Ledger {
    require(source.name.isNotBlank() && source.cents>0 && accounts.any { it.id==source.account }) { "Revisa el ingreso y la cuenta." }
    require(source.firstDay in 1..28 && source.secondDay in (source.firstDay+1)..31)
    return copy(sources=sources.filterNot { it.id==source.id }+source)
}
