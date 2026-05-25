package com.example.projectpbd.data.model

import com.example.projectpbd.domain.model.*

private inline fun <reified T : Enum<T>> safeEnum(value: String, default: T): T {
    return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
}

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    type = safeEnum(type, WalletType.CASH),
    currentBalance = currentBalance,
    currency = currency,
    iconKey = iconKey,
    colorKey = colorKey,
    isArchived = isArchived,
    createdAt = createdAt
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    type = type.name,
    currentBalance = currentBalance,
    currency = currency,
    iconKey = iconKey,
    colorKey = colorKey,
    isArchived = isArchived,
    createdAt = createdAt
)

fun TransferEntity.toDomain(): Transfer = Transfer(
    id = id,
    sourceWalletId = sourceWalletId,
    destinationWalletId = destinationWalletId,
    amount = amount,
    notes = notes,
    date = date,
    sourceCurrency = sourceCurrency,
    targetCurrency = targetCurrency,
    targetAmount = targetAmount,
    exchangeRate = exchangeRate,
    createdAt = createdAt
)

fun Transfer.toEntity(): TransferEntity = TransferEntity(
    id = id,
    sourceWalletId = sourceWalletId,
    destinationWalletId = destinationWalletId,
    amount = amount,
    notes = notes,
    date = date,
    sourceCurrency = sourceCurrency,
    targetCurrency = targetCurrency,
    targetAmount = targetAmount,
    exchangeRate = exchangeRate,
    createdAt = createdAt
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    type = safeEnum(type, CategoryType.EXPENSE),
    source = safeEnum(source, CategorySource.USER),
    iconKey = iconKey,
    colorKey = colorKey,
    isDefault = isDefault,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type.name,
    source = source.name,
    iconKey = iconKey,
    colorKey = colorKey,
    isDefault = isDefault,
    createdAt = createdAt
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    uid = uid,
    name = name,
    email = email,
    createdAt = createdAt,
    preferredCurrency = preferredCurrency,
    monthlyTargetSaving = monthlyTargetSaving,
    financialAwarenessScore = financialAwarenessScore,
    savingsBalance = savingsBalance
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    uid = uid,
    name = name,
    email = email,
    createdAt = createdAt,
    preferredCurrency = preferredCurrency,
    monthlyTargetSaving = monthlyTargetSaving,
    financialAwarenessScore = financialAwarenessScore,
    savingsBalance = savingsBalance
)

fun IncomeEntity.toDomain(): Income = Income(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    date = date,
    notes = notes,
    walletId = walletId,
    exchangeRate = exchangeRate,
    convertedAmountLkr = convertedAmountLkr,
    repeatConfig = RepeatConfiguration(
        frequency = safeEnum(repeatFrequency, RepeatFrequency.NONE),
        interval = repeatInterval,
        startDate = repeatStartDate,
        nextExecutionDate = repeatNextExecutionDate,
        lastExecutionDate = repeatLastExecutionDate,
        isActive = repeatIsActive,
        isInitialOccurrenceExecuted = repeatIsInitialExecuted
    ),
    createdAt = createdAt
)

fun Income.toEntity(): IncomeEntity = IncomeEntity(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    date = date,
    notes = notes,
    walletId = walletId,
    exchangeRate = exchangeRate,
    convertedAmountLkr = convertedAmountLkr,
    repeatFrequency = repeatConfig.frequency.name,
    repeatInterval = repeatConfig.interval,
    repeatStartDate = repeatConfig.startDate,
    repeatNextExecutionDate = repeatConfig.nextExecutionDate,
    repeatLastExecutionDate = repeatConfig.lastExecutionDate,
    repeatIsActive = repeatConfig.isActive,
    repeatIsInitialExecuted = repeatConfig.isInitialOccurrenceExecuted,
    createdAt = createdAt
)

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethod = safeEnum(paymentMethod, PaymentMethod.CASH),
    date = date,
    notes = notes,
    walletId = walletId,
    exchangeRate = exchangeRate,
    convertedAmountLkr = convertedAmountLkr,
    repeatConfig = RepeatConfiguration(
        frequency = safeEnum(repeatFrequency, RepeatFrequency.NONE),
        interval = repeatInterval,
        startDate = repeatStartDate,
        nextExecutionDate = repeatNextExecutionDate,
        lastExecutionDate = repeatLastExecutionDate,
        isActive = repeatIsActive,
        isInitialOccurrenceExecuted = repeatIsInitialExecuted
    ),
    isDiscretionary = isDiscretionary,
    createdAt = createdAt,
    transactionType = safeEnum(transactionType, TransactionType.REGULAR),
    goalId = goalId,
    generated = generated
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethod = paymentMethod.name,
    date = date,
    notes = notes,
    walletId = walletId,
    exchangeRate = exchangeRate,
    convertedAmountLkr = convertedAmountLkr,
    repeatFrequency = repeatConfig.frequency.name,
    repeatInterval = repeatConfig.interval,
    repeatStartDate = repeatConfig.startDate,
    repeatNextExecutionDate = repeatConfig.nextExecutionDate,
    repeatLastExecutionDate = repeatConfig.lastExecutionDate,
    repeatIsActive = repeatConfig.isActive,
    repeatIsInitialExecuted = repeatConfig.isInitialOccurrenceExecuted,
    isDiscretionary = isDiscretionary,
    createdAt = createdAt,
    transactionType = transactionType.name,
    goalId = goalId,
    generated = generated
)

fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
    id = id,
    title = title,
    targetAmount = targetAmount,
    walletId = walletId,
    categoryId = categoryId,
    targetDate = targetDate,
    createdAt = createdAt,
    description = description,
    status = safeEnum(status, GoalStatus.ACTIVE),
    completedAt = completedAt
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    title = title,
    targetAmount = targetAmount,
    walletId = walletId,
    categoryId = categoryId,
    targetDate = targetDate,
    createdAt = createdAt,
    description = description,
    status = status.name,
    completedAt = completedAt
)

fun FinancialInsightEntity.toDomain(): FinancialInsight = FinancialInsight(
    id = id,
    title = title,
    description = description,
    type = safeEnum(type, InsightType.NEUTRAL),
    createdAt = createdAt,
    severity = severity
)

fun FinancialInsight.toEntity(): FinancialInsightEntity = FinancialInsightEntity(
    id = id,
    title = title,
    description = description,
    type = type.name,
    createdAt = createdAt,
    severity = severity
)

fun MonthlySummaryEntity.toDomain(): MonthlySummary = MonthlySummary(
    month = month,
    totalIncome = totalIncome,
    totalExpenses = totalExpenses,
    discretionaryExpenses = discretionaryExpenses,
    committedExpenses = committedExpenses,
    savingsAmount = savingsAmount,
    savingsRate = savingsRate,
    topCategoryId = topCategoryId
)

fun MonthlySummary.toEntity(): MonthlySummaryEntity = MonthlySummaryEntity(
    month = month,
    totalIncome = totalIncome,
    totalExpenses = totalExpenses,
    discretionaryExpenses = discretionaryExpenses,
    committedExpenses = committedExpenses,
    savingsAmount = savingsAmount,
    savingsRate = savingsRate,
    topCategoryId = topCategoryId
)
