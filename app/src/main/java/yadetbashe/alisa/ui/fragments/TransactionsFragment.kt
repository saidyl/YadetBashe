package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.databinding.FragmentTransactionsBinding
import yadetbashe.app.alisa.ui.adapter.TransactionAdapter
import yadetbashe.app.alisa.ui.dialogs.TransactionDialogFragment
import yadetbashe.app.alisa.utils.PersianDate
import java.text.DecimalFormat

@AndroidEntryPoint
class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: yadetbashe.app.alisa.viewmodel.TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionAdapter(
            onItemClicked = { transaction ->
                // ویرایش تراکنش
                TransactionDialogFragment.newInstance(transaction.id)
                    .show(parentFragmentManager, "TransactionDialog")
            },
            onItemLongClicked = { transaction ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_confirmation)
                    .setMessage(R.string.delete_transaction_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.deleteTransaction(transaction)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        // فیلترها
        binding.chipAll.setOnClickListener { viewModel.setTypeFilter(null) }
        binding.chipDebt.setOnClickListener { viewModel.setTypeFilter(TransactionType.DEBT) }
        binding.chipCredit.setOnClickListener { viewModel.setTypeFilter(TransactionType.CREDIT) }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // مشاهده داده‌ها
        viewModel.filteredTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.layoutEmpty.isVisible = list.isEmpty()
        }

        viewModel.persons.observe(viewLifecycleOwner) { persons ->
            adapter.personNames = persons.associateBy({ it.id }, { it.name })
        }

        viewModel.totalUnpaidDebts.observe(viewLifecycleOwner) { d ->
            binding.tvTotalDebts.text = PersianDate.toPersianDigits(
                DecimalFormat("#,###").format(d ?: 0.0)
            )
        }

        viewModel.totalUnpaidCredits.observe(viewLifecycleOwner) { c ->
            binding.tvTotalCredits.text = PersianDate.toPersianDigits(
                DecimalFormat("#,###").format(c ?: 0.0)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
