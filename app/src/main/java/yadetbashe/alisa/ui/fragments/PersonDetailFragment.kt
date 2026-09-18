package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.databinding.FragmentPersonDetailBinding
import androidx.navigation.fragment.navArgs
import yadetbashe.app.alisa.ui.adapter.ReminderAdapter
import yadetbashe.app.alisa.ui.adapter.TransactionAdapter
import yadetbashe.app.alisa.ui.dialogs.TransactionDialogFragment
import yadetbashe.app.alisa.utils.PersianDate
import yadetbashe.app.alisa.viewmodel.PersonDetailViewModel
import java.text.DecimalFormat

@AndroidEntryPoint
class PersonDetailFragment : Fragment() {

    private var _binding: FragmentPersonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PersonDetailViewModel by viewModels()
    private lateinit var transactionsAdapter: TransactionAdapter
    private lateinit var remindersAdapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val personId = PersonDetailFragmentArgs.fromBundle(requireArguments()).personId

        transactionsAdapter = TransactionAdapter(
            onItemClicked = { transaction ->
                TransactionDialogFragment.newInstance(transaction.id)
                    .show(parentFragmentManager, "TransactionDialog")
            },
            onItemLongClicked = { transaction -> confirmDeleteTransaction(transaction) }
        )
        binding.rvPersonTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPersonTransactions.adapter = transactionsAdapter

        remindersAdapter = ReminderAdapter(onDeleteClicked = { reminder ->
            viewModel.deleteReminder(reminder)
        })
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = remindersAdapter

        binding.fabAddForPerson.setOnClickListener {
            // افزودن تراکنش برای همین فرد (فقط نمایش دیالوگ؛ فرد در دیالوگ انتخاب می‌شود)
            TransactionDialogFragment.newInstance(-1L)
                .show(parentFragmentManager, "TransactionDialog")
        }

        viewModel.person.observe(viewLifecycleOwner) { person ->
            person?.let {
                binding.tvDetailName.text = it.name
                binding.tvDetailAvatar.text = it.name.firstOrNull()?.toString() ?: "؟"
                binding.tvDetailPhone.text = it.phone
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { list ->
            transactionsAdapter.submitList(list)
            binding.tvNoTransactions.isVisible = list.isEmpty()

            // محاسبه مانده: طلب‌ها منهای بدهی‌ها (پرداخت‌نشده)
            val balance = list.filter { !it.isPaid }.sumOf {
                if (it.type == TransactionType.CREDIT) it.amount else -it.amount
            }
            val color = when {
                balance > 0 -> R.color.credit_color
                balance < 0 -> R.color.debt_color
                else -> R.color.unpaid_color
            }
            binding.tvDetailBalance.setTextColor(
                ContextCompat.getColor(requireContext(), color)
            )
            val sign = when {
                balance > 0 -> "+"
                balance < 0 -> "−"
                else -> ""
            }
            val formatted = DecimalFormat("#,###").format(kotlin.math.abs(balance))
            binding.tvDetailBalance.text =
                PersianDate.toPersianDigits(sign + formatted) + " تومان"
        }

        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            // فقط یادآوری‌های مربوط به تراکنش‌های همین فرد
            val personTxIds = transactionsAdapter.currentList.map { it.id }.toSet()
            val mine = reminders.filter { it.transactionId in personTxIds }
            remindersAdapter.transactions = transactionsAdapter.currentList.associateBy({ it.id }, { it })
            remindersAdapter.submitList(mine)
            binding.tvNoReminders.isVisible = mine.isEmpty()
            binding.rvReminders.isVisible = mine.isNotEmpty()
        }
    }

    private fun confirmDeleteTransaction(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirmation)
            .setMessage(R.string.delete_transaction_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
