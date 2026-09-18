package yadetbashe.app.alisa.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import yadetbashe.app.alisa.data.model.Reminder
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.databinding.ItemReminderBinding
import yadetbashe.app.alisa.utils.PersianDate
import java.text.DecimalFormat

class ReminderAdapter(
    private val onDeleteClicked: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    /** نگاشت transactionId به تراکنش برای نمایش عنوان */
    var transactions: Map<Long, Transaction> = emptyMap()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(
        private val binding: ItemReminderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnDeleteReminder.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onDeleteClicked(getItem(position))
            }
        }

        fun bind(reminder: Reminder) {
            val t = transactions[reminder.transactionId]
            binding.tvReminderTitle.text = t?.description?.ifEmpty { "تراکنش" } ?: "تراکنش"
            t?.let {
                val amount = DecimalFormat("#,###").format(it.amount)
                binding.tvReminderTitle.text =
                    binding.tvReminderTitle.text.toString() + " — " +
                        PersianDate.toPersianDigits(amount) + " تومان"
            }
            binding.tvReminderDate.text = PersianDate.formatNumeric(reminder.reminderDate)
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
            oldItem == newItem
    }
}
