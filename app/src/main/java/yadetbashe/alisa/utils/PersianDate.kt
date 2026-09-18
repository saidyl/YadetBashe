package yadetbashe.app.alisa.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

/**
 * تبدیل و قالب‌بندی تاریخ شمسی (جلالی) بدون وابستگی خارجی.
 * الگوریتم تبدیل: نسخه استاندارد و پرکاربرد jdf (جلالی <-> میلادی).
 */
object PersianDate {

    private val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    data class Jalali(val year: Int, val month: Int, val day: Int)
    data class Gregorian(val year: Int, val month: Int, val day: Int)

    /** تبدیل میلادی به جلالی */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Jalali {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy = if (gy <= 1600) 0 else 979
        val y = if (gy <= 1600) gy - 621 else gy - 1600
        val gy2 = if (gm > 2) y + 1 else y
        var days = 365 * y + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 -
            80 + gd + gDaysInMonth[gm - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
        return Jalali(jy, jm, jd)
    }

    /** تبدیل جلالی به میلادی */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Gregorian {
        var gy = if (jy <= 979) 621 else 1600
        val y = if (jy <= 979) jy else jy - 979
        var days = 365 * y + (y / 33) * 8 + ((y % 33) + 3) / 4 + 78 + jd +
            (if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186)
        gy += 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val monthDays = intArrayOf(
            31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 12 && gd > monthDays[gm]) {
            gd -= monthDays[gm]
            gm++
        }
        return Gregorian(gy, gm + 1, gd)
    }

    fun toJalali(epochMillis: Long): Jalali {
        val localDate = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return gregorianToJalali(localDate.year, localDate.monthValue, localDate.dayOfMonth)
    }

    /** تبدیل تاریخ جلالی به epochMillis (ابتدای روز به وقت محلی) */
    fun fromJalali(jy: Int, jm: Int, jd: Int): Long {
        val g = jalaliToGregorian(jy, jm, jd)
        val localDate = LocalDate.of(g.year, g.month, g.day)
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun monthName(month: Int): String = monthNames[month - 1]

    /** قالب ۱۴۰۳/۰۵/۱۲ با اعداد فارسی */
    fun formatNumeric(epochMillis: Long): String {
        val j = toJalali(epochMillis)
        return toPersianDigits("%04d/%02d/%02d".format(j.year, j.month, j.day))
    }

    /** قالب «مرداد ۱۴۰۳» */
    fun formatMonthYear(epochMillis: Long): String {
        val j = toJalali(epochMillis)
        return "${monthName(j.month)} ${toPersianDigits(j.year.toString())}"
    }

    /** ابتدای ماه جلالیِ شامل تاریخ داده‌شده */
    fun startOfMonth(epochMillis: Long): Long {
        val j = toJalali(epochMillis)
        return fromJalali(j.year, j.month, 1)
    }

    /** ابتدای ماهِ j ماه بعد/قبل نسبت به ماهِ تاریخ داده‌شده */
    fun startOfMonthPlusMonths(epochMillis: Long, monthsToAdd: Int): Long {
        val j = toJalali(epochMillis)
        var y = j.year
        var m = j.month + monthsToAdd
        while (m > 12) { m -= 12; y += 1 }
        while (m < 1) { m += 12; y -= 1 }
        return fromJalali(y, m, 1)
    }

    fun toPersianDigits(s: String): String {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val sb = StringBuilder()
        for (c in s) {
            if (c in '0'..'9') sb.append(persian[c - '0']) else sb.append(c)
        }
        return sb.toString()
    }

    fun fromPersianDigits(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            if (c in '۰'..'۹') sb.append(('0' + (c - '۰')))
            else sb.append(c)
        }
        return sb.toString()
    }
}
