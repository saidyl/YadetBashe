package yadetbashe.app.alisa.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * مدل تراکنش: بدهکاری (من بدهکارم) یا طلبکاری (من طلبکارم).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** نوع تراکنش: DEBT (بدهکاری) یا CREDIT (طلبکاری) */
    val type: TransactionType,

    /** مبلغ به تومان */
    val amount: Double,

    /** تاریخ تراکنش (میلی‌ثانیه از اپوک) */
    val date: Long,

    /** شناسه فرد (FK به جدول persons) */
    val personId: Long,

    /** توضیحات */
    val description: String = "",

    /** وضعیت پرداخت */
    val isPaid: Boolean = false,

    /** تاریخ سررسید (برای بدهی‌ها) - میلی‌ثانیه یا null */
    val dueDate: Long? = null,

    /** دسته‌بندی */
    val category: TransactionCategory = TransactionCategory.OTHER
)

enum class TransactionType {
    /** بدهکاری (من بدهکارم) */
    DEBT,

    /** طلبکاری (من طلبکارم) */
    CREDIT
}

enum class TransactionCategory {
    /** قرض */
    LOAN,

    /** خدمات */
    SERVICE,

    /** کالا */
    PRODUCT,

    /** سایر */
    OTHER
}
