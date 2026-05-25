package com.example.projectpbd.domain.model

enum class RepeatFrequency {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    ANNUALLY
}

data class RepeatConfiguration(
    val frequency: RepeatFrequency = RepeatFrequency.NONE,
    val interval: Int = 1,
    val startDate: Long = System.currentTimeMillis(),
    val nextExecutionDate: Long = 0L,
    val lastExecutionDate: Long = 0L,
    val isActive: Boolean = true,
    val isInitialOccurrenceExecuted: Boolean = false
) {
    val isEnabled: Boolean get() = frequency != RepeatFrequency.NONE
}
