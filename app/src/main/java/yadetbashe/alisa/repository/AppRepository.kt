package yadetbashe.app.alisa.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import yadetbashe.app.alisa.data.AppDatabase
import yadetbashe.app.alisa.data.model.PersonBalanceRow
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.Reminder
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionCategory
import yadetbashe.app.alisa.data.model.TransactionType
import yadetbashe.app.alisa.utils.JsonBackup
import org.json.JSONArray
import org.json.JSONObject

/**
 * ریپازیتوری مرکزی: تمام عملیات داده از اینجا عبور می‌کند.
 */
class AppRepository(private val db: AppDatabase) {

    // ---------- تراکنش‌ها ----------

    fun getAllTransactions(): LiveData<List<Transaction>> = db.transactionDao().getAllTransactions()

    fun getTransactionsByPerson(personId: Long): LiveData<List<Transaction>> =
        db.transactionDao().getTransactionsByPerson(personId)

    fun getTotalUnpaidDebts(): LiveData<Double?> = db.transactionDao().getTotalUnpaidDebts()

    fun getTotalUnpaidCredits(): LiveData<Double?> = db.transactionDao().getTotalUnpaidCredits()

    suspend fun getTransactionById(id: Long): Transaction? = db.transactionDao().getTransactionById(id)

    suspend fun saveTransaction(transaction: Transaction): Long =
        db.transactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        db.transactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        db.transactionDao().deleteTransaction(transaction)

    suspend fun getDebtsBetween(start: Long, end: Long) = db.transactionDao().getDebtsBetween(start, end)

    suspend fun getCreditsBetween(start: Long, end: Long) = db.transactionDao().getCreditsBetween(start, end)

    // ---------- افراد ----------

    fun getAllPersons(): LiveData<List<Person>> = db.personDao().getAllPersons()

    suspend fun getAllPersonsOnce(): List<Person> = db.personDao().getAllPersonsOnce()

    suspend fun getPersonById(id: Long): Person? = db.personDao().getPersonById(id)

    suspend fun savePerson(person: Person): Long = db.personDao().insertPerson(person)

    suspend fun updatePerson(person: Person) = db.personDao().updatePerson(person)

    suspend fun deletePerson(person: Person) = db.personDao().deletePerson(person)

    fun getPersonBalances(): LiveData<List<PersonBalanceRow>> = db.transactionDao().getPersonBalances()

    // ---------- یادآوری‌ها ----------

    fun getActiveReminders(): LiveData<List<Reminder>> = db.reminderDao().getActiveReminders()

    /** یادآوری‌های فعال در بازه زمانی (برای Worker اعلان‌ها) */
    suspend fun getRemindersDueBetween(start: Long, end: Long): List<Reminder> {
        return db.reminderDao().getActiveRemindersOnce().filter {
            it.reminderDate in start..end
        }
    }

    suspend fun getReminderForTransaction(transactionId: Long): Reminder? =
        db.reminderDao().getReminderForTransaction(transactionId)

    suspend fun saveReminder(reminder: Reminder): Long = db.reminderDao().insertReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) = db.reminderDao().deleteReminder(reminder)

    suspend fun deactivateReminder(id: Long) = db.reminderDao().deactivateReminder(id)

    // ---------- پشتیبان‌گیری / بازیابی (JSON) ----------

    fun exportBackup(context: Context): String {
        val root = JSONObject()
        root.put("app", "YadetBashe")
        root.put("version", 1)

        val persons = JSONArray()
        db.personDao().getAllPersonsOnce().forEach { p ->
            persons.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("phone", p.phone)
                    .put("note", p.note)
            )
        }
        root.put("persons", persons)

        val transactions = JSONArray()
        db.transactionDao().getAllTransactionsOnce().forEach { t ->
            transactions.put(
                JSONObject()
                    .put("id", t.id)
                    .put("type", t.type.name)
                    .put("amount", t.amount)
                    .put("date", t.date)
                    .put("personId", t.personId)
                    .put("description", t.description)
                    .put("isPaid", t.isPaid)
                    .put("dueDate", t.dueDate ?: JSONObject.NULL)
                    .put("category", t.category.name)
            )
        }
        root.put("transactions", transactions)

        val reminders = JSONArray()
        db.reminderDao().getAllRemindersOnce().forEach { r ->
            reminders.put(
                JSONObject()
                    .put("id", r.id)
                    .put("transactionId", r.transactionId)
                    .put("reminderDate", r.reminderDate)
                    .put("isActive", r.isActive)
            )
        }
        root.put("reminders", reminders)

        val json = root.toString(2)
        JsonBackup.writeToFile(context, json)
        return json
    }

    suspend fun importBackup(context: Context): Boolean {
        val json = JsonBackup.readFromFile(context) ?: return false
        return try {
            val root = JSONObject(json)
            val persons = mutableListOf<Person>()
            val personsArr = root.optJSONArray("persons") ?: JSONArray()
            for (i in 0 until personsArr.length()) {
                val o = personsArr.getJSONObject(i)
                persons.add(
                    Person(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        phone = o.optString("phone", ""),
                        note = o.optString("note", "")
                    )
                )
            }
            val transactions = mutableListOf<Transaction>()
            val transactionsArr = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until transactionsArr.length()) {
                val o = transactionsArr.getJSONObject(i)
                transactions.add(
                    Transaction(
                        id = o.getLong("id"),
                        type = TransactionType.valueOf(o.getString("type")),
                        amount = o.getDouble("amount"),
                        date = o.getLong("date"),
                        personId = o.getLong("personId"),
                        description = o.optString("description", ""),
                        isPaid = o.optBoolean("isPaid", false),
                        dueDate = if (o.isNull("dueDate")) null else o.getLong("dueDate"),
                        category = TransactionCategory.valueOf(o.optString("category", "OTHER"))
                    )
                )
            }
            val reminders = mutableListOf<Reminder>()
            val remindersArr = root.optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until remindersArr.length()) {
                val o = remindersArr.getJSONObject(i)
                reminders.add(
                    Reminder(
                        id = o.getLong("id"),
                        transactionId = o.getLong("transactionId"),
                        reminderDate = o.getLong("reminderDate"),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
            db.personDao().insertAll(persons)
            db.transactionDao().insertAll(transactions)
            db.reminderDao().insertAll(reminders)
            true
        } catch (e: Exception) {
            false
        }
    }
}
