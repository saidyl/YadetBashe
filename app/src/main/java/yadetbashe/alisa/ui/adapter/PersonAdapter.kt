package yadetbashe.app.alisa.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.databinding.ItemPersonBinding
import yadetbashe.app.alisa.utils.PersianDate
import yadetbashe.app.alisa.viewmodel.PersonWithBalance
import java.text.DecimalFormat

class PersonAdapter(
    private val onItemClicked: (PersonWithBalance) -> Unit,
    private val onItemLongClicked: (PersonWithBalance) -> Unit = {}
) : ListAdapter<PersonWithBalance, PersonAdapter.PersonViewHolder>(PersonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonViewHolder(
        private val binding: ItemPersonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onItemClicked(getItem(position))
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClicked(getItem(position))
                    true
                } else false
            }
        }

        fun bind(item: PersonWithBalance) {
            val context = binding.root.context
            binding.tvPersonName.text = item.person.name
            binding.tvPersonAvatar.text = item.person.name.firstOrNull()?.toString() ?: "؟"
            binding.tvPersonPhone.text = item.person.phone

            val color = when {
                item.balance > 0 -> R.color.credit_color  // طلبکاریم
                item.balance < 0 -> R.color.debt_color    // بدهکاریم
                else -> R.color.unpaid_color
            }
            binding.tvPersonBalance.setTextColor(ContextCompat.getColor(context, color))
            val sign = when {
                item.balance > 0 -> "+"
                item.balance < 0 -> "−"
                else -> ""
            }
            val formatted = DecimalFormat("#,###").format(kotlin.math.abs(item.balance))
            binding.tvPersonBalance.text =
                PersianDate.toPersianDigits(sign + formatted)
        }
    }

    class PersonDiffCallback : DiffUtil.ItemCallback<PersonWithBalance>() {
        override fun areItemsTheSame(oldItem: PersonWithBalance, newItem: PersonWithBalance): Boolean =
            oldItem.person.id == newItem.person.id

        override fun areContentsTheSame(oldItem: PersonWithBalance, newItem: PersonWithBalance): Boolean =
            oldItem == newItem
    }
}
