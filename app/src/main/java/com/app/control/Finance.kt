package com.app.control

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class Account(val id: String = UUID.randomUUID().toString(), val name: String)
data class SavingsGoal(val id: String = UUID.randomUUID().toString(), val name: String, val target: Long, val deadline: LocalDate? = null, val icon: String = "bag")
enum class Operation { INCOME, EXPENSE, TRANSFER, RESERVE, RELEASE }
data class Movement(val id: String = UUID.randomUUID().toString(), val type: Operation, val cents: Long, val account: String, val destination: String? = null, val description: String = "", val date: LocalDate = LocalDate.now(), val category: String = "Otros", val goal: String? = null)
/** Immutable ledger. Money is represented as integer cents. */
data class Ledger(val accounts: List<Account> = emptyList(), val movements: List<Movement> = emptyList(), val goals: List<SavingsGoal> = emptyList()) {
    fun balance(id: String): Long = movements.fold(0L) { sum, m -> Math.addExact(sum, when {
        m.type == Operation.TRANSFER && m.destination == id -> m.cents
        m.account != id -> 0L
        m.type == Operation.INCOME -> m.cents
        m.type == Operation.EXPENSE || m.type == Operation.TRANSFER -> -m.cents
        else -> 0L
    }) }
    private fun List<Movement>.reservedTotal(): Long = fold(0L) { sum, m -> Math.addExact(sum, when(m.type) {
        Operation.RESERVE -> m.cents
        Operation.RELEASE -> -m.cents
        else -> 0L
    }) }
    fun reserved(id: String): Long = movements.filter { it.account == id }.reservedTotal()
    fun goalBalance(goal: String, account: String? = null): Long = movements.filter { it.goal == goal && (account == null || it.account == account) }.reservedTotal()
    fun available(id: String) = Math.subtractExact(balance(id), reserved(id))
    val total get() = accounts.fold(0L) { sum, a -> Math.addExact(sum, balance(a.id)) }
    val held get() = accounts.fold(0L) { sum, a -> Math.addExact(sum, reserved(a.id)) }
    fun addAccount(name: String): Ledger {
        require(name.isNotBlank()) { "Escribe el nombre de la cuenta." }
        require(accounts.none { it.name.equals(name.trim(), true) }) { "La cuenta ya existe." }
        return copy(accounts = accounts + Account(name = name.trim()))
    }
    fun addGoal(goal: SavingsGoal): Ledger {
        require(goal.name.isNotBlank()) { "Escribe un nombre para el apartado." }
        require(goal.target > 0) { "La meta debe ser mayor a cero." }
        require(goals.none { it.id == goal.id || it.name.equals(goal.name.trim(), true) }) { "El apartado ya existe." }
        return copy(goals = goals + goal.copy(name = goal.name.trim()))
    }
    fun post(m: Movement): Ledger {
        require(movements.none { it.id == m.id }) { "Movimiento ya registrado." }
        return copy(movements = movements + m).validated()
    }
    fun delete(id: String) = copy(movements = movements.filterNot { it.id == id }).validated()
    fun edit(m: Movement): Ledger {
        require(movements.any { it.id == m.id })
        return copy(movements = movements.map { if(it.id == m.id) m else it }).validated()
    }
    private fun validated(): Ledger {
        movements.forEach { m ->
            require(m.cents > 0) { "El importe debe ser mayor a cero." }
            require(accounts.any { it.id == m.account }) { "Cuenta inválida." }
            if(m.type == Operation.TRANSFER) require(m.destination != m.account && accounts.any { it.id == m.destination }) { "Selecciona otra cuenta de destino." }
            else require(m.destination == null)
            if(m.goal != null) require((m.type == Operation.RESERVE || m.type == Operation.RELEASE) && goals.any { it.id == m.goal }) { "Apartado inválido." }
        }
        movements.filter { it.type == Operation.RESERVE || it.type == Operation.RELEASE }.groupBy { it.account to it.goal }.values.forEach {
            require(it.reservedTotal() >= 0) { "No puedes liberar más de lo reservado en este apartado y cuenta." }
        }
        accounts.forEach {
            require(reserved(it.id) >= 0) { "No puedes liberar más de lo apartado." }
            require(available(it.id) >= 0) { "Saldo disponible insuficiente en ${it.name}." }
        }
        total
        held
        return this
    }
}
fun parseCents(text: String): Long {
    require(Regex("[0-9]+(\\.[0-9]{1,2})?").matches(text.trim())) { "Usa un importe como 250.50, con máximo dos decimales." }
    return BigDecimal(text.trim()).movePointRight(2).longValueExact().also { require(it > 0) { "El importe debe ser positivo." } }
}
