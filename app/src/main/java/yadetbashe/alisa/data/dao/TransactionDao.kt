package yadetbashe.app.alisa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import yadetbashe.app.alisa.data.model.PersonBalanceRow
import yadetbashe.app.alisa.data.model.Transaction
import yadetbashe.app.alisa.data.model.TransactionType

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC")
    fun getTransactionsByPerson(personId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBT' AND isPaid = 0")
    fun getTotalUnpaidDebts(): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT' AND isPaid = 0")
    fun getTotalUnpaidCredits(): LiveData<Double?>

    /** مجموع بدهی‌های پرداخت‌نشده در بازه زمانی (برای نمودار) */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBT' AND isPaid = 0 AND date BETWEEN :start AND :end")
    suspend fun getDebtsBetween(start: Long, end: Long): Double?

    /** مجموع طلب‌های پرداخت‌نشده در بازه زمانی (برای نمودار) */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT' AND isPaid = 0 AND date BETWEEN :start AND :end")
    suspend fun getCreditsBetween(start: Long, end: Long): Double?

    /** مانده حساب هر فرد: طلب‌ها منهای بدهی‌ها (فقط پرداخت‌نشده) */
    @Query(
        """
        SELECT p.id AS personId,
               IFNULL(SUM(CASE WHEN t.type = 'CREDIT' AND t.isPaid = 0 THEN t.amount ELSE 0 END), 0)
             - IFNULL(SUM(CASE WHEN t.type = 'DEBT' AND t.isPaid = 0 THEN t.amount ELSE 0 END), 0) AS balance
        FROM persons p
        LEFT JOIN transactions t ON t.personId = p.id
        GROUP BY p.id
        """
    )
    fun getPersonBalances(): LiveData<List<PersonBalanceRow>>

    /** پشتیبان‌گیری و بازیابی */
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsOnce(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    /** پاک‌کردن همه تراکنش‌ها */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
