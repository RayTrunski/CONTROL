package com.app.control

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private fun JSONObject.optional(key: String)=if(isNull(key)) null else getString(key)
private fun <T> JSONArray.items(read: (JSONObject)->T)=(0 until length()).map { read(getJSONObject(it)) }
private fun array(items: List<JSONObject>)=JSONArray(items)
fun encodeLedger(ledger: Ledger): String = JSONObject().apply {
    put("version",1)
    put("accounts",array(ledger.accounts.map { JSONObject().put("id",it.id).put("name",it.name) }))
    put("goals",array(ledger.goals.map { JSONObject().put("id",it.id).put("name",it.name).put("target",it.target).put("deadline",it.deadline?.toString()).put("icon",it.icon).put("archived",it.archived) }))
    put("movements",array(ledger.movements.map { JSONObject().put("id",it.id).put("type",it.type.name).put("cents",it.cents).put("account",it.account).put("destination",it.destination).put("description",it.description).put("date",it.date.toString()).put("category",it.category).put("goal",it.goal).put("subcategory",it.subcategory) }))
    put("plans",array(ledger.plans.map { JSONObject().put("id",it.id).put("type",it.type.name).put("cents",it.cents).put("description",it.description).put("date",it.date.toString()).put("account",it.account).put("category",it.category).put("subcategory",it.subcategory).put("goal",it.goal).put("status",it.status.name).put("movementId",it.movementId).put("executedDate",it.executedDate?.toString()).put("source",it.source) }))
    put("sources",array(ledger.sources.map { JSONObject().put("id",it.id).put("name",it.name).put("cents",it.cents).put("account",it.account).put("cadence",it.cadence.name).put("anchor",it.anchor.toString()).put("firstDay",it.firstDay).put("secondDay",it.secondDay).put("end",it.end?.toString()) }))
}.toString()
fun decodeLedger(text: String): Ledger {
    val json=JSONObject(text)
    require(json.getInt("version")==1) { "Versión de respaldo no compatible." }
    val ledger=Ledger(
        accounts=json.getJSONArray("accounts").items { Account(it.getString("id"),it.getString("name")) },
        movements=json.getJSONArray("movements").items { Movement(it.getString("id"),Operation.valueOf(it.getString("type")),it.get("cents").toString().toLong(),it.getString("account"),it.optional("destination"),it.getString("description"),LocalDate.parse(it.getString("date")),it.getString("category"),it.optional("goal"),it.optString("subcategory","")) },
        goals=json.getJSONArray("goals").items { SavingsGoal(it.getString("id"),it.getString("name"),it.get("target").toString().toLong(),it.optional("deadline")?.let(LocalDate::parse),it.getString("icon"),it.optBoolean("archived")) },
        plans=json.getJSONArray("plans").items { PlannedOperation(it.getString("id"),Operation.valueOf(it.getString("type")),it.get("cents").toString().toLong(),it.getString("description"),LocalDate.parse(it.getString("date")),it.optional("account"),it.getString("category"),it.optString("subcategory",""),it.optional("goal"),PlanStatus.valueOf(it.getString("status")),it.optional("movementId"),it.optional("executedDate")?.let(LocalDate::parse),it.optional("source")) },
        sources=json.getJSONArray("sources").items { IncomeSource(it.getString("id"),it.getString("name"),it.get("cents").toString().toLong(),it.getString("account"),Cadence.valueOf(it.getString("cadence")),LocalDate.parse(it.getString("anchor")),it.getInt("firstDay"),it.getInt("secondDay"),it.optional("end")?.let(LocalDate::parse)) }
    )
    listOf(ledger.accounts.map { it.id },ledger.goals.map { it.id },ledger.movements.map { it.id },ledger.plans.map { it.id },ledger.sources.map { it.id }).forEach { require(it.size==it.distinct().size) { "Identificadores duplicados." } }
    ledger.goals.forEach { require(it.target>0 && it.name.isNotBlank()) }
    ledger.sources.forEach { ledger.addSource(it) }
    ledger.plans.forEach { p ->
        ledger.copy(plans=emptyList()).savePlan(p.copy(status=PlanStatus.PENDING))
        if(p.status==PlanStatus.EXECUTED) require(ledger.movements.any { it.id==p.movementId }) { "Falta el movimiento ejecutado." }
    }
    return ledger.validated()
}
fun exportCsv(ledger: Ledger): String {
    fun row(vararg values: Any?)=values.joinToString(",") { "\""+it.toString().replace("\"","\"\"")+"\"" }
    val accountNames=ledger.accounts.associate { it.id to it.name }
    return buildList {
        add(row("Origen","Concepto","Tipo","Importe real MXN","Importe previsto MXN","Fecha","Estado","Categoría","Subcategoría","Cuenta","Destino","Apartado","Movimiento vinculado"))
        ledger.movements.forEach { add(row("Real",it.description,it.type.label(),java.math.BigDecimal.valueOf(it.cents,2),"",it.date,"Confirmado",it.category,it.subcategory,accountNames[it.account],accountNames[it.destination].orEmpty(),it.goal.orEmpty(),it.id)) }
        // Include stored plans and the next twelve months of generated salary occurrences.
        val now=java.time.YearMonth.now()
        val plans=(ledger.plans+(0L..12L).flatMap { ledger.plansFor(now.plusMonths(it)) }).distinctBy { it.id }
        plans.forEach { add(row("Planificado",it.description,it.type.label(),"",java.math.BigDecimal.valueOf(it.cents,2),it.date,it.status.title,it.category,it.subcategory,accountNames[it.account].orEmpty(),"",it.goal.orEmpty(),it.movementId.orEmpty())) }
    }.joinToString("\r\n")
}
