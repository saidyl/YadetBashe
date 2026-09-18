package yadetbashe.app.alisa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import yadetbashe.app.alisa.utils.ReminderScheduler
import javax.inject.Inject

@HiltAndroidApp
class YadetBasheApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // راه‌اندازی تاریخ/زمان ThreeTen برای API 24
        AndroidThreeTen.init(this)
        createNotificationChannel()
        // بازآرایی یادآوری‌های روزانه بعد از هر اجرا
        ReminderScheduler.ensureDailyCheckScheduled(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            ReminderScheduler.REMINDER_CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.reminder_channel_description)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
