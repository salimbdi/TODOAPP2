package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val ISO_PATTERN = "yyyy-MM-dd"
    private val locale = Locale.getDefault()
    private val timeZone = TimeZone.getDefault()

    private fun getCalendar(): Calendar {
        return Calendar.getInstance(timeZone, locale)
    }

    private fun getIsoFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat(ISO_PATTERN, locale)
        sdf.timeZone = timeZone
        return sdf
    }

    fun today(): String {
        return getIsoFormat().format(Date())
    }

    fun currentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", locale)
        sdf.timeZone = timeZone
        return sdf.format(Date())
    }

    fun parseDate(dateStr: String): Calendar? {
        return try {
            val date = getIsoFormat().parse(dateStr) ?: return null
            val cal = getCalendar()
            cal.time = date
            cal
        } catch (_: Exception) {
            null
        }
    }

    fun addDays(dateStr: String, days: Int): String {
        val cal = parseDate(dateStr) ?: getCalendar()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return getIsoFormat().format(cal.time)
    }

    fun getDayOfWeek(dateStr: String): Int {
        // Returns 1 for Monday, 2 for Tuesday, ..., 7 for Sunday (ISO standard)
        val cal = parseDate(dateStr) ?: return 1
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getDayOfMonth(dateStr: String): Int {
        val cal = parseDate(dateStr) ?: return 1
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun getYear(dateStr: String): Int {
        val cal = parseDate(dateStr) ?: return 2026
        return cal.get(Calendar.YEAR)
    }

    fun getMonth(dateStr: String): Int {
        val cal = parseDate(dateStr) ?: return 10
        return cal.get(Calendar.MONTH) + 1 // 1-12
    }

    fun isToday(dateStr: String): Boolean {
        return dateStr == today()
    }

    fun isPast(dateStr: String): Boolean {
        return dateStr < today()
    }

    fun isFuture(dateStr: String): Boolean {
        return dateStr > today()
    }

    fun formatDisplay(dateStr: String): String {
        val cal = parseDate(dateStr) ?: return dateStr
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", locale)
        sdf.timeZone = timeZone
        return sdf.format(cal.time)
    }

    fun formatDayHeader(dateStr: String): String {
        val cal = parseDate(dateStr) ?: return dateStr
        val sdf = SimpleDateFormat("EEEE, MMMM d", locale)
        sdf.timeZone = timeZone
        return sdf.format(cal.time)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = getCalendar()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", locale)
        sdf.timeZone = timeZone
        return sdf.format(cal.time)
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance(timeZone, locale).get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun getWeekDates(centerDate: String): List<String> {
        val cal = parseDate(centerDate) ?: getCalendar()
        val isoDow = getDayOfWeek(centerDate)
        // Offset to start Monday
        cal.add(Calendar.DAY_OF_YEAR, -(isoDow - 1))
        val dates = mutableListOf<String>()
        val format = getIsoFormat()
        for (i in 0 until 7) {
            dates.add(format.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }

    fun getDaysInMonth(year: Int, month: Int): List<String> {
        val cal = getCalendar()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dates = mutableListOf<String>()
        val format = getIsoFormat()
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            dates.add(format.format(cal.time))
        }
        return dates
    }
}
