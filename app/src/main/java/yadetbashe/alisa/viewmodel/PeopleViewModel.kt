package yadetbashe.app.alisa.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import yadetbashe.app.alisa.data.dao.PersonBalanceRow
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.repository.AppRepository
import javax.inject.Inject

data class PersonWithBalance(val person: Person, val balance: Double)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val persons: LiveData<List<Person>> = repository.getAllPersons()
    val balances: LiveData<List<PersonBalanceRow>> = repository.getPersonBalances()

    val peopleWithBalance: MediatorLiveData<List<PersonWithBalance>> = MediatorLiveData()

    init {
        peopleWithBalance.addSource(persons) { combine() }
        peopleWithBalance.addSource(balances) { combine() }
    }

    private fun combine() {
        val p = persons.value ?: emptyList()
        val b = balances.value.orEmpty().associateBy({ it.personId }, { it.balance })
        peopleWithBalance.value = p.map { person ->
            PersonWithBalance(person, b[person.id] ?: 0.0)
        }
    }

    fun savePerson(person: Person) {
        viewModelScope.launch {
            if (person.id == 0L) repository.savePerson(person)
            else repository.updatePerson(person)
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch { repository.deletePerson(person) }
    }
}
