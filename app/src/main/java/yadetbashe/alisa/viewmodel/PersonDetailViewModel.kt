package yadetbashe.app.alisa.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.Reminder
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.repository.AppRepository
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
) : ViewModel() {

    private val personId: Long = savedStateHandle["personId"] ?: -1L

    private val _person = MutableLiveData<Person?>()
    val person: LiveData<Person?> = _person

    val transactions: LiveData<List<Transaction>> =
        repository.getTransactionsByPerson(personId)

    val reminders: LiveData<List<Reminder>> = repository.getActiveReminders()

    init {
        viewModelScope.launch {
            _person.value = repository.getPersonById(personId)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
