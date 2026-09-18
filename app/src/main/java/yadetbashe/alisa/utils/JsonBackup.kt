package yadetbashe.app.alisa.utils

import android.content.Context
import java.io.File

/**
 * خواندن و نوشتن فایل پشتیبان JSON در پوشه اختصاصی برنامه
 * (Android/data/yadetbashe.app.alisa/files/backups).
 */
object JsonBackup {

    private fun backupDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun backupFile(context: Context): File = File(backupDir(context), "yadetbashe_backup.json")

    fun writeToFile(context: Context, json: String): Boolean {
        return try {
            backupFile(context).writeText(json, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readFromFile(context: Context): String? {
        return try {
            val f = backupFile(context)
            if (f.exists()) f.readText(Charsets.UTF_8) else null
        } catch (e: Exception) {
            null
        }
    }
}
