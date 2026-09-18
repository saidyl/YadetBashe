package yadetbashe.app.alisa.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.databinding.ItemTransactionBinding
import yadetbashe.app.alisa.utils.PersianDate
import java.text.DecimalFormat

class TransactionAdapter(
    private val onItemClicked: (Transaction) -> Unit,
    private val onItemLongClicked: (Transaction) -> Unit = {}
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    /** نگاشت personId به نام فرد برای نمایش در لیست */
    var personNames: Map<Long, String> = emptyMap()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClicked(getItem(position))
                    true
                } else {
                    false
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                val context = root.context
                val (colorRes, typeText) = when (transaction.type) {
                    TransactionType.DEBT -> Pair(R.color.debt_color, context.getString(R.string.debt_label))
                    TransactionType.CREDIT -> Pair(R.color.credit_color, context.getString(R.string.credit_label))
                }

                tvTransactionType.text = typeText
                viewTypeIndicator.background.mutate().setTint(ContextCompat.getColor(context, colorRes))
                tvTransactionType.setTextColor(ContextCompat.getColor(context, colorRes))
                tvAmount.setTextColor(ContextCompat.getColor(context, colorRes))

                tvAmount.text = formatAmount(transaction.amount)
                tvDescription.text = transaction.description.ifEmpty { "—" }
                tvDate.text = PersianDate.formatNumeric(transaction.date)
                tvPersonName.text = personNames[transaction.personId] ?: ""

                if (transaction.isPaid) {
                    tvStatus.text = context.getString(R.string.paid_label)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.paid_color))
                } else {
                    tvStatus.text = context.getString(R.string.unpaid_label)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.unpaid_color))
                }
            }
        }

        private fun formatAmount(amount: Double): String {
            return PersianDate.toPersianDigits(DecimalFormat("#,###").format(amount))
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }
}
