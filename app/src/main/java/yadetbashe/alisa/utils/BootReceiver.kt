package yadetbashe.app.alisa.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * بعد از ری‌استارت دستگاه، WorkManager خودش کارهای دوره‌ای را نگه می‌دارد؛
 * این گیرنده صرفاً تضمین می‌کند زمان‌بند روزانه وجود دارد.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.ensureDailyCheckScheduled(context)
        }
    }
}
