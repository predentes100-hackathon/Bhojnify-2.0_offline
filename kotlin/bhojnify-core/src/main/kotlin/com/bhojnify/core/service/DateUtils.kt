package com.bhojnify.core.service

import com.bhojnify.core.i18n.Language
import com.bhojnify.core.model.ExpiryInfo
import com.bhojnify.core.model.ExpiryStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val MARATHI_MONTHS = arrayOf(
        "जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून",
        "जुलै", "ऑगस्ट", "सप्टेंबर", "ऑक्टोबर", "नोव्हेंबर", "डिसेंबर"
    )

    fun todayString(): String {
        return LocalDate.now().format(ISO_FORMATTER)
    }

    fun currentYearMonth(): String {
        return todayString().take(7)
    }

    fun previousYearMonth(yearMonth: String): String {
        return try {
            val ym = YearMonth.parse(yearMonth.trim().take(7))
            ym.minusMonths(1).toString()
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun nextYearMonth(yearMonth: String): String {
        return try {
            val ym = YearMonth.parse(yearMonth.trim().take(7))
            ym.plusMonths(1).toString()
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun formatYearMonth(yearMonth: String, language: Language = Language.EN): String {
        return try {
            val ym = YearMonth.parse(yearMonth.trim().take(7))
            if (language == Language.MR) {
                val monthName = MARATHI_MONTHS[ym.monthValue - 1]
                "$monthName ${ym.year}"
            } else {
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
                ym.format(formatter)
            }
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr.trim().take(10), ISO_FORMATTER)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format: $dateStr")
        }
    }

    fun formatDate(value: String, language: Language = Language.EN): String {
        return try {
            val date = parseDate(value)
            val locale = if (language == Language.MR) Locale.forLanguageTag("mr-IN") else Locale.ENGLISH
            val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
            date.format(formatter)
        } catch (e: Exception) {
            value.toString()
        }
    }

    fun formatDateFull(value: String, language: Language = Language.EN): String {
        return try {
            val date = parseDate(value)
            val locale = if (language == Language.MR) Locale.forLanguageTag("mr-IN") else Locale.ENGLISH
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", locale)
            date.format(formatter)
        } catch (e: Exception) {
            value.toString()
        }
    }

    fun daysUntil(value: String, todayStr: String = todayString()): Long {
        val target = parseDate(value)
        val today = parseDate(todayStr)
        val diff = ChronoUnit.DAYS.between(today, target)
        return maxOf(0L, diff)
    }

    fun getExpiryInfo(expiryDate: String?, todayStr: String = todayString()): ExpiryInfo {
        if (expiryDate.isNullOrBlank()) {
            return ExpiryInfo(
                daysRemaining = 0,
                status = ExpiryStatus.ACTIVE,
                isExpired = false,
                isExpiringSoon = false,
                isExpiringToday = false
            )
        }

        val target = parseDate(expiryDate)
        val today = parseDate(todayStr)
        val daysRemaining = ChronoUnit.DAYS.between(today, target)

        return when {
            daysRemaining < 0 -> ExpiryInfo(
                daysRemaining = daysRemaining,
                status = ExpiryStatus.EXPIRED,
                isExpired = true,
                isExpiringSoon = false,
                isExpiringToday = false
            )
            daysRemaining == 0L -> ExpiryInfo(
                daysRemaining = 0,
                status = ExpiryStatus.TODAY,
                isExpired = false,
                isExpiringSoon = true,
                isExpiringToday = true
            )
            daysRemaining <= 5L -> ExpiryInfo(
                daysRemaining = daysRemaining,
                status = ExpiryStatus.SOON,
                isExpired = false,
                isExpiringSoon = true,
                isExpiringToday = false
            )
            else -> ExpiryInfo(
                daysRemaining = daysRemaining,
                status = ExpiryStatus.ACTIVE,
                isExpired = false,
                isExpiringSoon = false,
                isExpiringToday = false
            )
        }
    }

    fun addDays(dateStr: String, days: Long): String {
        val date = parseDate(dateStr)
        return date.plusDays(days).format(ISO_FORMATTER)
    }

    fun daysBetween(fromStr: String, toStr: String): Long {
        val from = parseDate(fromStr)
        val to = parseDate(toStr)
        return ChronoUnit.DAYS.between(from, to)
    }
}
