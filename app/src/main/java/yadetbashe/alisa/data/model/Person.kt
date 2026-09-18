package yadetbashe.app.alisa.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مدل فرد: اعضای خانواده، دوستان و همکارانی که تراکنش مالی با آن‌ها داریم.
 */
@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** نام فرد */
    val name: String,

    /** شماره تلفن (اختیاری) */
    val phone: String = "",

    /** یادداشت (اختیاری) */
    val note: String = ""
)

/**
 * نتیجه تجمیعی مانده حساب هر فرد:
 * مانده = (طلبات پرداخت‌نشده) - (بدهی‌های پرداخت‌نشده)
 * مثبت یعنی طلبکاریم و منفی یعنی بدهکاریم.
 */
data class PersonBalance(
    val personId: Long,
    val balance: Double
)

/**
 * ردیف نتیجه کوئری Room برای مانده حساب افراد (نام ستون‌ها باید با کوئری یکی باشد).
 */
data class PersonBalanceRow(
    val personId: Long,
    val balance: Double
)
