package yadetbashe.app.alisa.ui.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.Reminder
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionCategory
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.data.repository.AppRepository
import yadetbashe.app.alisa.databinding.DialogTransactionBinding
import yadetbashe.app.alisa.utils.PersianDate
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel کوچک مخصوص دیالوگ تراکنش.
 */
@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val persons = repository.getAllPersons()

    fun getTransaction(id: Long, onLoaded: (Transaction?) -> Unit) {
        viewModelScope.launch { onLoaded(repository.getTransactionById(id)) }
    }

    /**
     * ذخیره تراکنش؛ اگر فرد موجود نباشد اول ساخته می‌شود.
     * برای بدهی‌های پرداخت‌نشده با سررسید، یادآوری ساخت/به‌روزرسانی می‌شود.
     */
    fun saveTransaction(
        editingId: Long?,
        type: TransactionType,
        amount: Double,
        personName: String,
        knownPersonId: Long?,
        description: String,
        category: TransactionCategory,
        date: Long,
        dueDate: Long?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val personId = knownPersonId ?: repository.savePerson(Person(name = personName))
            val base = editingId?.let { repository.getTransactionById(it) }
                ?: Transaction(type = type, amount = amount, date = date, personId = personId)
            val updated = base.copy(
                type = type,
                amount = amount,
                date = date,
                personId = personId,
                description = description,
                category = category,
                dueDate = dueDate
            )
            val savedId = repository.saveTransaction(updated)

            if (type == TransactionType.DEBT && !updated.isPaid && dueDate != null) {
                val existing = repository.getReminderForTransaction(savedId)
                val reminder = existing?.copy(reminderDate = dueDate, isActive = true)
                    ?: Reminder(transactionId = savedId, reminderDate = dueDate)
                repository.saveReminder(reminder)
            }
            onDone()
        }
    }
}

@AndroidEntryPoint
class TransactionDialogFragment : DialogFragment() {

    private var _binding: DialogTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionFormViewModel by viewModels()

    private var editId: Long = -1L
    private var selectedDate: Long = System.currentTimeMillis()
    private var selectedDueDate: Long? = null
    private var personsList: List<Person> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogTransactionBinding.inflate(LayoutInflater.from(requireContext()))

        editId = arguments?.getLong(ARG_EDIT_ID, -1L) ?: -1L
        val isEditing = editId > 0

        // دسته‌بندی‌ها
        val categoryNames = listOf("قرض", "خدمات", "کالا", "سایر")
        binding.actvCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryNames)
        )
        binding.actvCategory.setText(categoryNames.last(), false)

        // افراد
        viewModel.persons.observe(this) { persons ->
            personsList = persons
            binding.actvPerson.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, persons.map { it.name })
            )
        }

        if (isEditing) {
            viewModel.getTransaction(editId) { t -> t?.let { populate(it, categoryNames) } }
        } else {
            binding.etDate.setText(PersianDate.formatNumeric(selectedDate))
        }

        binding.etDate.setOnClickListener { showDatePicker(isDue = false) }
        binding.etDueDate.setOnClickListener { showDatePicker(isDue = true) }

        return AlertDialog.Builder(requireContext())
            .setTitle(
                if (isEditing) getString(R.string.edit_transaction)
                else getString(R.string.add_transaction)
            )
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (validateAndSave()) dismiss()
                    }
                }
            }
    }

    private fun populate(t: Transaction, categoryNames: List<String>) {
        binding.rbDebt.isChecked = t.type == TransactionType.DEBT
        binding.rbCredit.isChecked = t.type == TransactionType.CREDIT
        binding.etAmount.setText(t.amount.toLong().toString())
        binding.etDescription.setText(t.description)
        val catIndex = TransactionCategory.entries.indexOf(t.category)
        binding.actvCategory.setText(categoryNames.getOrElse(catIndex) { categoryNames.last() }, false)
        selectedDate = t.date
        selectedDueDate = t.dueDate
        binding.etDate.setText(PersianDate.formatNumeric(t.date))
        binding.etDueDate.setText(t.dueDate?.let { PersianDate.formatNumeric(it) } ?: "")
        viewModel.persons.value?.find { it.id == t.personId }?.let {
            binding.actvPerson.setText(it.name, false)
        }
    }

    private fun showDatePicker(isDue: Boolean) {
        val current = if (isDue) selectedDueDate ?: selectedDate else selectedDate
        val g0 = PersianDate.toJalali(current).let { PersianDate.jalaliToGregorian(it.year, it.month, it.day) }
        val listener = DatePickerDialog.OnDateSetListener { _, year, month0, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month0, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val picked = cal.timeInMillis
            if (isDue) {
                selectedDueDate = picked
                binding.etDueDate.setText(PersianDate.formatNumeric(picked))
            } else {
                selectedDate = picked
                binding.etDate.setText(PersianDate.formatNumeric(picked))
            }
        }
        DatePickerDialog(requireContext(), listener, g0.year, g0.month - 1, g0.day).show()
    }

    private fun validateAndSave(): Boolean {
        val amountStr = PersianDate.fromPersianDigits(binding.etAmount.text.toString()).trim()
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = getString(R.string.amount_required)
            return false
        }
        val personName = binding.actvPerson.text.toString().trim()
        val knownPerson = personsList.find { it.name == personName }
        if (knownPerson == null && personName.isEmpty()) {
            binding.actvPerson.error = getString(R.string.person_required)
            return false
        }

        val type = if (binding.rbCredit.isChecked) TransactionType.CREDIT else TransactionType.DEBT
        val category = when (binding.actvCategory.text.toString()) {
            "قرض" -> TransactionCategory.LOAN
            "خدمات" -> TransactionCategory.SERVICE
            "کالا" -> TransactionCategory.PRODUCT
            else -> TransactionCategory.OTHER
        }

        viewModel.saveTransaction(
            editingId = if (editId > 0) editId else null,
            type = type,
            amount = amount,
            personName = personName,
            knownPersonId = knownPerson?.id,
            description = binding.etDescription.text.toString().trim(),
            category = category,
            date = selectedDate,
            dueDate = selectedDueDate
        ) {
            Toast.makeText(requireContext(), R.string.transaction_saved, Toast.LENGTH_SHORT).show()
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EDIT_ID = "edit_id"

        fun newInstance(editId: Long = -1L): TransactionDialogFragment {
            return TransactionDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_EDIT_ID, editId) }
            }
        }
    }
}
