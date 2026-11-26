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

    fun parseDateOrNull(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateStr.trim().take(10), ISO_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDate(dateStr: String?): LocalDate {
        return parseDateOrNull(dateStr) ?: LocalDate.now()
    }

    fun formatDate(value: String, language: Language = Language.EN): String {
        return try {
            val date = parseDateOrNull(value) ?: return value
            val locale = if (language == Language.MR) Locale.forLanguageTag("mr-IN") else Locale.ENGLISH
            val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
            date.format(formatter)
        } catch (e: Exception) {
            value
        }
    }

    fun formatDateFull(value: String, language: Language = Language.EN): String {
        return try {
            val date = parseDateOrNull(value) ?: return value
            val locale = if (language == Language.MR) Locale.forLanguageTag("mr-IN") else Locale.ENGLISH
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", locale)
            date.format(formatter)
        } catch (e: Exception) {
            value
        }
    }

    fun daysUntil(value: String, todayStr: String = todayString()): Long {
        return try {
            val target = parseDateOrNull(value) ?: return 0L
            val today = parseDateOrNull(todayStr) ?: LocalDate.now()
            val diff = ChronoUnit.DAYS.between(today, target)
            maxOf(0L, diff)
        } catch (e: Exception) {
            0L
        }
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

        return try {
            val target = parseDateOrNull(expiryDate) ?: return ExpiryInfo(
                daysRemaining = 0,
                status = ExpiryStatus.ACTIVE,
                isExpired = false,
                isExpiringSoon = false,
                isExpiringToday = false
            )
            val today = parseDateOrNull(todayStr) ?: LocalDate.now()
            val daysRemaining = ChronoUnit.DAYS.between(today, target)

            when {
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
        } catch (e: Exception) {
            ExpiryInfo(
                daysRemaining = 0,
                status = ExpiryStatus.ACTIVE,
                isExpired = false,
                isExpiringSoon = false,
                isExpiringToday = false
            )
        }
    }

    fun addDays(dateStr: String, days: Long): String {
        return try {
            val date = parseDate(dateStr)
            date.plusDays(days).format(ISO_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now().plusDays(days).format(ISO_FORMATTER)
        }
    }

    fun daysBetween(fromStr: String, toStr: String): Long {
        return try {
            val from = parseDateOrNull(fromStr) ?: return 0L
            val to = parseDateOrNull(toStr) ?: return 0L
            ChronoUnit.DAYS.between(from, to)
        } catch (e: Exception) {
            0L
        }
    }
}
