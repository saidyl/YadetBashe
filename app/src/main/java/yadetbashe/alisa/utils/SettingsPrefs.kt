package yadetbashe.app.alisa.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * تنظیمات ذخیره‌شده برنامه (SharedPreferences).
 */
object SettingsPrefs {
    private const val PREFS = "yadetbashe_prefs"
    private const val KEY_REMINDERS = "reminders_enabled"
    private const val KEY_THEME = "app_theme"
    private const val KEY_LAST_BACKUP = "last_backup_time"

    // مقادیر حالت نمایش
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    fun isRemindersEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMINDERS, true)

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_REMINDERS, enabled).apply()
    }

    /** حالت نمایش ذخیره‌شده (سیستم/روشن/تاریک) */
    fun getThemeMode(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, THEME_SYSTEM)

    fun setThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, mode).apply()
    }

    /** اعمال حالت نمایش روی کل برنامه (در onCreate اکتیویتی صدا زده می‌شود) */
    fun applyTheme(context: Context) {
        when (getThemeMode(context)) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /** زمان آخرین پشتیبان‌گیری (میلی‌ثانیه) یا ۰ */
    fun getLastBackupTime(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP, 0L)

    fun setLastBackupTime(context: Context, time: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_BACKUP, time).apply()
    }
}
