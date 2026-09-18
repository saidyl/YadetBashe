package yadetbashe.app.alisa.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import yadetbashe.app.alisa.data.repository.AppRepository
import yadetbashe.app.alisa.utils.PersianDate
import javax.inject.Inject

data class MonthlySummary(
    val monthStart: Long,
    val debts: Double,
    val credits: Double
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    /** ابتدای ماه جلالیِ ماه انتخاب‌شده */
    private val monthStart = MutableLiveData(PersianDate.startOfMonth(System.currentTimeMillis()))

    private val _summary = MutableLiveData<MonthlySummary>()
    val summary: LiveData<MonthlySummary> = _summary

    init {
        loadMonth()
    }

    fun previousMonth() {
        monthStart.value = PersianDate.startOfMonthPlusMonths(monthStart.value!!, -1)
        loadMonth()
    }

    fun nextMonth() {
        monthStart.value = PersianDate.startOfMonthPlusMonths(monthStart.value!!, +1)
        loadMonth()
    }

    private fun loadMonth() {
        viewModelScope.launch {
            val start = monthStart.value ?: return@launch
            val end = PersianDate.startOfMonthPlusMonths(start, 1)
            val debts = repository.getDebtsBetween(start, end) ?: 0.0
            val credits = repository.getCreditsBetween(start, end) ?: 0.0
            _summary.value = MonthlySummary(start, debts, credits)
        }
    }
}
