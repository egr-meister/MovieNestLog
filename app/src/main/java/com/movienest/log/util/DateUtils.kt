package com.movienest.log.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Date helpers. All parsing is guarded — malformed stored values return null
 * rather than throwing into the UI. Storage format is always ISO YYYY-MM-DD.
 */
object DateUtils {

    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val DISPLAY_DATE: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private val MONTH_KEY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH)

    /** Today's local date as a stored YYYY-MM-DD string. */
    fun todayIso(): String = LocalDate.now().format(ISO_DATE)

    /** Current instant as an ISO-8601 timestamp string. */
    fun nowTimestamp(): String = java.time.OffsetDateTime.now().toString()

    /** Parse a stored YYYY-MM-DD string safely. Returns null when invalid/blank. */
    fun parseIsoOrNull(value: String?): LocalDate? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        return try {
            LocalDate.parse(v, ISO_DATE)
        } catch (e: Exception) {
            null
        }
    }

    fun isValidIsoDate(value: String?): Boolean = parseIsoOrNull(value) != null

    /** Format a stored date for display, or return a fallback if missing/invalid. */
    fun formatForDisplay(value: String?, fallback: String = "Not set"): String {
        val date = parseIsoOrNull(value) ?: return fallback
        return date.format(DISPLAY_DATE)
    }

    /** Month key (YYYY-MM) from a stored date, or null when the date is invalid. */
    fun monthKeyOf(value: String?): String? {
        val date = parseIsoOrNull(value) ?: return null
        return date.format(MONTH_KEY)
    }

    /** Current month key (YYYY-MM). */
    fun currentMonthKey(): String = YearMonth.now().format(MONTH_KEY)

    /** Parse a month key safely into a YearMonth. */
    fun parseMonthKeyOrNull(key: String?): YearMonth? {
        val v = key?.trim().orEmpty()
        if (v.isEmpty()) return null
        return try {
            YearMonth.parse(v, MONTH_KEY)
        } catch (e: Exception) {
            null
        }
    }

    fun monthKeyFrom(yearMonth: YearMonth): String = yearMonth.format(MONTH_KEY)

    /** Human month label such as "July 2026". */
    fun monthDisplayLabel(key: String?): String {
        val ym = parseMonthKeyOrNull(key) ?: YearMonth.now()
        val month = ym.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        return "$month ${ym.year}"
    }

    /** Short month label such as "Jul 2026". */
    fun monthShortLabel(key: String?): String {
        val ym = parseMonthKeyOrNull(key) ?: YearMonth.now()
        val month = ym.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        return "$month ${ym.year}"
    }

    fun previousMonthKey(key: String?): String {
        val ym = parseMonthKeyOrNull(key) ?: YearMonth.now()
        return monthKeyFrom(ym.minusMonths(1))
    }

    /** Next month key, clamped so we never move past a safe forward limit. */
    fun nextMonthKey(key: String?): String {
        val ym = parseMonthKeyOrNull(key) ?: YearMonth.now()
        val limit = YearMonth.now().plusYears(10)
        val next = ym.plusMonths(1)
        return monthKeyFrom(if (next.isAfter(limit)) limit else next)
    }

    /** True if [a] (start) is strictly after [b] (completion). Null-safe. */
    fun isStartAfterCompletion(startIso: String?, completionIso: String?): Boolean {
        val start = parseIsoOrNull(startIso) ?: return false
        val completion = parseIsoOrNull(completionIso) ?: return false
        return start.isAfter(completion)
    }
}
