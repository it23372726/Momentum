package com.example.projectpbd.core.util

import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.model.RepeatFrequency
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object RecurrenceCalculator {

    fun calculateNextOccurrence(config: RepeatConfiguration, fromDate: Long = System.currentTimeMillis()): Long {
        if (config.frequency == RepeatFrequency.NONE) return 0L

        val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(config.startDate), ZoneId.systemDefault())
        var next = LocalDateTime.ofInstant(Instant.ofEpochMilli(fromDate), ZoneId.systemDefault())

        // Ensure we calculate from the start date if fromDate is before it
        if (next.isBefore(startDateTime)) {
            next = startDateTime
        }

        return when (config.frequency) {
            RepeatFrequency.DAILY -> next.plusDays(config.interval.toLong())
            RepeatFrequency.WEEKLY -> next.plusWeeks(config.interval.toLong())
            RepeatFrequency.MONTHLY -> next.plusMonths(config.interval.toLong())
            RepeatFrequency.ANNUALLY -> next.plusYears(config.interval.toLong())
            RepeatFrequency.NONE -> next
        }.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getRepeatSummary(config: RepeatConfiguration): String {
        if (!config.isEnabled) return "No Repeat"
        
        val freq = config.frequency.name.lowercase().replaceFirstChar { it.uppercase() }
        val unit = when (config.frequency) {
            RepeatFrequency.DAILY -> if (config.interval == 1) "day" else "days"
            RepeatFrequency.WEEKLY -> if (config.interval == 1) "week" else "weeks"
            RepeatFrequency.MONTHLY -> if (config.interval == 1) "month" else "months"
            RepeatFrequency.ANNUALLY -> if (config.interval == 1) "year" else "years"
            RepeatFrequency.NONE -> ""
        }

        return if (config.interval == 1) {
            freq
        } else {
            "Every ${config.interval} $unit"
        }
    }
}
