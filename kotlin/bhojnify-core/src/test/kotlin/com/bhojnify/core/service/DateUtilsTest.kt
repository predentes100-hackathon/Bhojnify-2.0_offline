package com.bhojnify.core.service

import com.bhojnify.core.model.ExpiryStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateUtilsTest {

    @Test
    fun `test daysBetween calculation`() {
        assertEquals(5L, DateUtils.daysBetween("2026-09-01", "2026-09-06"))
        assertEquals(0L, DateUtils.daysBetween("2026-09-10", "2026-09-10"))
        assertEquals(-2L, DateUtils.daysBetween("2026-09-12", "2026-09-10"))
    }

    @Test
    fun `test addDays calculation`() {
        assertEquals("2026-09-15", DateUtils.addDays("2026-09-10", 5))
        assertEquals("2026-10-10", DateUtils.addDays("2026-09-10", 30))
        assertEquals("2026-09-05", DateUtils.addDays("2026-09-10", -5))
    }

    @Test
    fun `test leap year date addition`() {
        assertEquals("2028-02-29", DateUtils.addDays("2028-02-28", 1))
        assertEquals("2028-03-01", DateUtils.addDays("2028-02-28", 2))
    }

    @Test
    fun `test getExpiryInfo for active member`() {
        val info = DateUtils.getExpiryInfo("2026-10-25", "2026-10-10")
        assertEquals(15L, info.daysRemaining)
        assertEquals(ExpiryStatus.ACTIVE, info.status)
        assertFalse(info.isExpired)
        assertFalse(info.isExpiringSoon)
        assertFalse(info.isExpiringToday)
    }

    @Test
    fun `test getExpiryInfo for expiring soon 1 to 5 days`() {
        val info3Days = DateUtils.getExpiryInfo("2026-10-13", "2026-10-10")
        assertEquals(3L, info3Days.daysRemaining)
        assertEquals(ExpiryStatus.SOON, info3Days.status)
        assertFalse(info3Days.isExpired)
        assertTrue(info3Days.isExpiringSoon)
        assertFalse(info3Days.isExpiringToday)

        val info1Day = DateUtils.getExpiryInfo("2026-10-11", "2026-10-10")
        assertEquals(1L, info1Day.daysRemaining)
        assertEquals(ExpiryStatus.SOON, info1Day.status)
        assertTrue(info1Day.isExpiringSoon)
    }

    @Test
    fun `test getExpiryInfo for expiring today`() {
        val info = DateUtils.getExpiryInfo("2026-10-10", "2026-10-10")
        assertEquals(0L, info.daysRemaining)
        assertEquals(ExpiryStatus.TODAY, info.status)
        assertFalse(info.isExpired)
        assertTrue(info.isExpiringSoon)
        assertTrue(info.isExpiringToday)
    }

    @Test
    fun `test getExpiryInfo for expired member`() {
        val info = DateUtils.getExpiryInfo("2026-10-08", "2026-10-10")
        assertEquals(-2L, info.daysRemaining)
        assertEquals(ExpiryStatus.EXPIRED, info.status)
        assertTrue(info.isExpired)
        assertFalse(info.isExpiringSoon)
        assertFalse(info.isExpiringToday)
    }

    @Test
    fun `test getExpiryInfo for null or blank expiry`() {
        val nullInfo = DateUtils.getExpiryInfo(null, "2026-10-10")
        assertEquals(0L, nullInfo.daysRemaining)
        assertEquals(ExpiryStatus.ACTIVE, nullInfo.status)
        assertFalse(nullInfo.isExpired)

        val blankInfo = DateUtils.getExpiryInfo("   ", "2026-10-10")
        assertEquals(0L, blankInfo.daysRemaining)
        assertEquals(ExpiryStatus.ACTIVE, blankInfo.status)
    }

    @Test
    fun `test getExpiryInfo across month boundaries and leap years`() {
        // Month boundary (Jan 31 to Feb 2)
        val monthEnd = DateUtils.getExpiryInfo("2026-02-02", "2026-01-31")
        assertEquals(2L, monthEnd.daysRemaining)
        assertEquals(ExpiryStatus.SOON, monthEnd.status)

        // Leap year: Feb 28 to Feb 29, 2028
        val leapYearInfo1 = DateUtils.getExpiryInfo("2028-02-29", "2028-02-28")
        assertEquals(1L, leapYearInfo1.daysRemaining)
        assertEquals(ExpiryStatus.SOON, leapYearInfo1.status)

        // Leap year: Feb 28 to Mar 1, 2028 (2 days)
        val leapYearInfo2 = DateUtils.getExpiryInfo("2028-03-01", "2028-02-28")
        assertEquals(2L, leapYearInfo2.daysRemaining)
    }

    @Test
    fun `test year month navigation and formatting`() {
        assertEquals("2026-10", DateUtils.previousYearMonth("2026-11"))
        assertEquals("2026-12", DateUtils.nextYearMonth("2026-11"))
        assertEquals("2025-12", DateUtils.previousYearMonth("2026-01"))
        assertEquals("2027-01", DateUtils.nextYearMonth("2026-12"))

        assertEquals("November 2026", DateUtils.formatYearMonth("2026-11", com.bhojnify.core.i18n.Language.EN))
        assertEquals("नोव्हेंबर 2026", DateUtils.formatYearMonth("2026-11", com.bhojnify.core.i18n.Language.MR))
        assertEquals("जानेवारी 2027", DateUtils.formatYearMonth("2027-01", com.bhojnify.core.i18n.Language.MR))

        // Defensive trimming and full-date string handling
        assertEquals("2026-10", DateUtils.previousYearMonth("2026-11-15"))
        assertEquals("2026-12", DateUtils.nextYearMonth("  2026-11  "))
        assertEquals("November 2026", DateUtils.formatYearMonth("2026-11-20", com.bhojnify.core.i18n.Language.EN))
    }

    @Test
    fun `test DateUtils safe fallback parsing on malformed inputs`() {
        // parseDate should not throw on invalid dates, falls back to today
        val fallbackDate = DateUtils.parseDate("not-a-valid-date")
        assertEquals(java.time.LocalDate.now(), fallbackDate)

        val emptyFallback = DateUtils.parseDate("")
        assertEquals(java.time.LocalDate.now(), emptyFallback)

        val nullFallback = DateUtils.parseDate(null)
        assertEquals(java.time.LocalDate.now(), nullFallback)

        val invalidFormat = DateUtils.parseDate("2026/13/45")
        assertEquals(java.time.LocalDate.now(), invalidFormat)

        // parseDateOrNull returns null on invalid dates
        assertEquals(null, DateUtils.parseDateOrNull("invalid"))
        assertEquals(null, DateUtils.parseDateOrNull(""))
        assertEquals(null, DateUtils.parseDateOrNull(null))

        // getExpiryInfo should not throw on malformed dates, returns safe ACTIVE status
        val malformedExpiryInfo = DateUtils.getExpiryInfo("invalid-date-string")
        assertEquals(ExpiryStatus.ACTIVE, malformedExpiryInfo.status)
        assertEquals(0L, malformedExpiryInfo.daysRemaining)
        assertFalse(malformedExpiryInfo.isExpired)
        assertFalse(malformedExpiryInfo.isExpiringSoon)
        assertFalse(malformedExpiryInfo.isExpiringToday)

        // daysBetween should safely return 0 on unparseable inputs
        assertEquals(0L, DateUtils.daysBetween("bad-date", "2026-10-01"))
        assertEquals(0L, DateUtils.daysBetween("2026-10-01", "bad-date"))

        // daysUntil should safely return 0 on unparseable inputs
        assertEquals(0L, DateUtils.daysUntil("bad-date"))

        // addDays should safely fallback on invalid date input
        val fallbackAddDays = DateUtils.addDays("garbage-input", 5)
        val expectedFallbackAddDays = java.time.LocalDate.now().plusDays(5).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        assertEquals(expectedFallbackAddDays, fallbackAddDays)

        // formatDate should return original string on unparseable input without throwing
        assertEquals("not-a-date", DateUtils.formatDate("not-a-date"))
        assertEquals("not-a-date", DateUtils.formatDateFull("not-a-date"))
    }
}
