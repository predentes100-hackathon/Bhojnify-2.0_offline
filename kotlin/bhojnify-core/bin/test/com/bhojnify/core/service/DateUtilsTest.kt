package com.bhojnify.core.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
}
