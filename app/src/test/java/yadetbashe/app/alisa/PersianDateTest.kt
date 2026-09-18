package yadetbashe.app.alisa

import org.junit.Assert.assertEquals
import org.junit.Test
import yadetbashe.app.alisa.utils.PersianDate

class PersianDateTest {

    @Test
    fun `nowruz 1403 converts to March 20 2024`() {
        val j = PersianDate.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun `jalali to gregorian roundtrip`() {
        val cases = listOf(
            Triple(1403, 1, 1),
            Triple(1403, 5, 12),
            Triple(1402, 12, 29),
            Triple(1399, 7, 15),
            Triple(1375, 10, 2)
        )
        for ((jy, jm, jd) in cases) {
            val g = PersianDate.jalaliToGregorian(jy, jm, jd)
            val back = PersianDate.gregorianToJalali(g.year, g.month, g.day)
            assertEquals(Triple(jy, jm, jd), Triple(back.year, back.month, back.day))
        }
    }

    @Test
    fun `persian digits conversion`() {
        assertEquals("۱۲۳", PersianDate.toPersianDigits("123"))
        assertEquals("123", PersianDate.fromPersianDigits("۱۲۳"))
        assertEquals("۱۴۰۳/۰۵/۱۲", PersianDate.toPersianDigits("1403/05/12"))
    }

    @Test
    fun `month names are correct`() {
        assertEquals("فروردین", PersianDate.monthName(1))
        assertEquals("اسفند", PersianDate.monthName(12))
    }
}
