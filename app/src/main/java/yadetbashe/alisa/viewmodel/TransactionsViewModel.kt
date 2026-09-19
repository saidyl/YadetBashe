package yadetbashe.app.alisa.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.data.repository.AppRepository
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.getAllTransactions()
    val persons: LiveData<List<Person>> = repository.getAllPersons()
    val totalUnpaidDebts: LiveData<Double?> = repository.getTotalUnpaidDebts()
    val totalUnpaidCredits: LiveData<Double?> = repository.getTotalUnpaidCredits()

    /** فیلتر نوع: null یعنی همه */
    private val typeFilter = MediatorLiveData<TransactionType?>().apply { value = null }

    /** عبارت جستجو در توضیحات یا نام فرد */
    private val searchQuery = MediatorLiveData<String>().apply { value = "" }

    val filteredTransactions: MediatorLiveData<List<Transaction>> = MediatorLiveData()

    init {
        filteredTransactions.addSource(allTransactions) { applyFilters() }
        filteredTransactions.addSource(typeFilter) { applyFilters() }
        filteredTransactions.addSource(searchQuery) { applyFilters() }
    }

    fun setTypeFilter(type: TransactionType?) {
        typeFilter.value = type
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    private fun applyFilters() {
        val list = allTransactions.value ?: emptyList()
        val type = typeFilter.value
        val q = searchQuery.value?.trim() ?: ""
        if (q.isEmpty()) {
            filteredTransactions.value = list.filter { type == null || it.type == type }
            return
        }
        // جستجو در توضیحات و نام فرد
        val names = persons.value.orEmpty().associateBy({ it.id }, { it.name })
        filteredTransactions.value = list.filter { t ->
            (type == null || t.type == type) &&
            (t.description.contains(q, ignoreCase = true) ||
                    (names[t.personId]?.contains(q, ignoreCase = true) == true))    
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    fun togglePaid(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(isPaid = !transaction.isPaid))
        }
    }
}
