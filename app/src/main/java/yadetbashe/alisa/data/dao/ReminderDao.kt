package yadetbashe.app.alisa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import yadetbashe.app.alisa.data.model.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY reminderDate ASC")
    fun getActiveReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY reminderDate ASC")
    suspend fun getActiveRemindersOnce(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE transactionId = :transactionId")
    suspend fun getReminderForTransaction(transactionId: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("UPDATE reminders SET isActive = 0 WHERE id = :id")
    suspend fun deactivateReminder(id: Long)

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersOnce(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<Reminder>)
}
