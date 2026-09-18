package yadetbashe.app.alisa.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.repository.AppRepository
import yadetbashe.app.alisa.utils.PersianDate.toPersianDigits
import java.util.concurrent.TimeUnit

/**
 * Worker دوره‌ای: سررسیدهای نزدیک (۲ روز آینده) و گذشته را پیدا کرده و اعلان می‌فرستد.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: AppRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!SettingsPrefs.isRemindersEnabled(context)) return Result.success()
        if (!NotificationHelper.canPostNotifications(context)) return Result.success()

        val now = System.currentTimeMillis()
        val soonWindow = TimeUnit.DAYS.toMillis(2)

        val activeReminders = repository.getRemindersDueBetween(now - soonWindow, now + soonWindow)

        activeReminders.forEach { reminder ->
            val transaction = repository.getTransactionById(reminder.transactionId) ?: return@forEach
            if (transaction.isPaid) return@forEach

            val isOverdue = reminder.reminderDate < now
            val title = context.getString(R.string.reminder_title)
            val text = if (isOverdue) {
                context.getString(R.string.reminder_overdue) + " — " + transaction.description
            } else {
                context.getString(R.string.reminder_due_soon) + " — " + transaction.description
            }
            val amountText = "، مبلغ: " + toPersianDigits(formatAmount(transaction.amount)) + " تومان"
            NotificationHelper.showReminderNotification(
                context,
                title,
                text + amountText,
                reminder.id.toInt()
            )
        }
        return Result.success()
    }

    private fun formatAmount(amount: Double): String =
        String.format("%,.0f", amount)
}
