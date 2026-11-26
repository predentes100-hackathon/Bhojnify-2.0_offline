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
}
