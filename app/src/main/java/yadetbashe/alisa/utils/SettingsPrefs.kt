package yadetbashe.app.alisa.utils

import android.content.Context

object SettingsPrefs {
    private const val PREFS = "yadetbashe_prefs"
    private const val KEY_REMINDERS = "reminders_enabled"

    fun isRemindersEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMINDERS, true)

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_REMINDERS, enabled).apply()
    }
}
