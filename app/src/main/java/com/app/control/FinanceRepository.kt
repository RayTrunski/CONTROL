package com.app.control

import android.app.Application
import android.content.Context
import androidx.room.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Entity(tableName="financial_state")
data class FinancialState(@PrimaryKey val id: Int=1, val json: String)
@Dao
interface FinanceDao {
    @Query("SELECT * FROM financial_state WHERE id=1") suspend fun read(): FinancialState?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun write(state: FinancialState)
}
@Database(entities=[FinancialState::class],version=1,exportSchema=false)
abstract class FinanceDatabase: RoomDatabase() { abstract fun financeDao(): FinanceDao }
private val Context.profileStore by preferencesDataStore("profile")
class FinanceRepository(context: Context, databaseName: String = "control.db") {
    private val db=Room.databaseBuilder(context.applicationContext,FinanceDatabase::class.java,databaseName).build()
    private val profile=context.applicationContext.profileStore
    private val aliasKey=stringPreferencesKey("alias")
    private val themeKey=stringPreferencesKey("theme")
    suspend fun darkTheme(): Boolean? = when(profile.data.first()[themeKey]) { "dark" -> true; "system" -> null; else -> false }
    suspend fun darkTheme(value: Boolean?) { profile.edit { it[themeKey]=when(value) { true -> "dark"; false -> "light"; null -> "system" } } }
    private val filterKey=stringPreferencesKey("analysis_filter")
    fun close() = db.close()
    suspend fun load()=db.financeDao().read()?.let { decodeLedger(it.json) } ?: Ledger()
    suspend fun save(ledger: Ledger)=db.withTransaction { db.financeDao().write(FinancialState(json=encodeLedger(ledger))) }
    suspend fun filter(): AnalysisFilter {
        val text=profile.data.first()[filterKey] ?: return AnalysisFilter()
        return runCatching { val j=org.json.JSONObject(text); AnalysisFilter(account=if(j.isNull("account")) null else j.getString("account"),category=if(j.isNull("category")) null else j.getString("category"),cadence=Cadence.valueOf(j.getString("cadence")),anchor=java.time.LocalDate.parse(j.getString("anchor")),firstDay=j.getInt("first"),secondDay=j.getInt("second"),customStart=java.time.LocalDate.parse(j.getString("start")),customEnd=java.time.LocalDate.parse(j.getString("end")),viewDate=if(j.isNull("viewDate")) null else java.time.LocalDate.parse(j.getString("viewDate"))) }.getOrDefault(AnalysisFilter())
    }
    suspend fun filter(value: AnalysisFilter) { profile.edit { it[filterKey]=org.json.JSONObject().put("account",value.account).put("category",value.category).put("cadence",value.cadence.name).put("anchor",value.anchor.toString()).put("first",value.firstDay).put("second",value.secondDay).put("start",value.customStart.toString()).put("end",value.customEnd.toString()).put("viewDate",value.viewDate?.toString()).toString() } }
    suspend fun alias()=profile.data.first()[aliasKey].orEmpty()
    suspend fun alias(value: String) { profile.edit { it[aliasKey]=value } }
}
class FinanceViewModel(app: Application): AndroidViewModel(app) {
    private val repository=FinanceRepository(app)
    private val mutex=Mutex()
    private val _ledger=MutableStateFlow<Ledger?>(null)
    val ledger=_ledger.asStateFlow()
    private val _alias=MutableStateFlow("")
    val alias=_alias.asStateFlow()
    val error=MutableStateFlow<String?>(null)
    val filter=MutableStateFlow(AnalysisFilter())
    val saving=MutableStateFlow(false)
    val darkTheme=MutableStateFlow<Boolean?>(false)
    init { viewModelScope.launch { runCatching { repository.darkTheme() }.onSuccess { darkTheme.value=it } } }
    fun setDarkTheme(value: Boolean?) { darkTheme.value=value; viewModelScope.launch { runCatching { repository.darkTheme(value) }.onFailure { error.value=it.message } } }
    init { viewModelScope.launch { runCatching { withContext(Dispatchers.IO) { Triple(repository.load(),repository.alias(),repository.filter()) } }.onSuccess { _ledger.value=it.first; _alias.value=it.second; filter.value=it.third }.onFailure { error.value="No se pudieron cargar los datos: ${it.message}" } } }
    fun save(value: Ledger) {
        if(saving.value) { error.value="Espera a que termine de guardarse el cambio anterior."; return }
        saving.value=true
        viewModelScope.launch { mutex.withLock { try { withContext(Dispatchers.IO) { repository.save(value) }; _ledger.value=value } catch(e: Exception) { error.value="No se guardaron los cambios: ${e.message}" } finally { saving.value=false } } }
    }
    fun setFilter(value: AnalysisFilter) { filter.value=value; viewModelScope.launch { runCatching { repository.filter(value) }.onFailure { error.value=it.message } } }
    fun setAlias(value: String) { viewModelScope.launch { runCatching { repository.alias(value); _alias.value=value }.onFailure { error.value=it.message } } }
    fun restore(text: String) {
        if(saving.value) return
        saving.value=true
        viewModelScope.launch { mutex.withLock { try { val restored=withContext(Dispatchers.IO) { decodeLedger(text).also { repository.save(it) } }; _ledger.value=restored; setFilter(filter.value.copy(account=null,category=null)) } catch(e: Exception) { error.value="Respaldo rechazado: ${e.message}" } finally { saving.value=false } } }
    }
}


