package yadetbashe.app.alisa.data

import androidx.room.Database
import androidx.room.RoomDatabase
import yadetbashe.app.alisa.data.dao.PersonDao
import yadetbashe.app.alisa.data.dao.ReminderDao
import yadetbashe.app.alisa.data.dao.TransactionDao
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.Reminder
import yadetbashe.app.alisa.data.model.Transaction

@Database(
    entities = [Transaction::class, Person::class, Reminder::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun personDao(): PersonDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "yadetbashe.db"
    }
}
