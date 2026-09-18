package yadetbashe.app.alisa.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * مدل یادآوری سررسید یک تراکنش.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** تراکنش مرتبط */
    val transactionId: Long,

    /** زمان یادآوری (میلی‌ثانیه از اپوک) */
    val reminderDate: Long,

    /** فعال بودن یادآوری */
    val isActive: Boolean = true
)
