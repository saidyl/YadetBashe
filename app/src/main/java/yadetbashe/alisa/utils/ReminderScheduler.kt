package yadetbashe.app.alisa.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * زمان‌بندی بررسی روزانه یادآوری‌ها با WorkManager.
 * بررسی هر ۲۴ ساعت اجرا می‌شود و اعلان سررسیدهای نزدیک/گذشته را می‌فرستد.
 */
object ReminderScheduler {

    const val REMINDER_CHANNEL_ID = "reminder_channel"
    private const val DAILY_CHECK_WORK = "daily_reminder_check"

    fun ensureDailyCheckScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDailyCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_CHECK_WORK)
    }
}
