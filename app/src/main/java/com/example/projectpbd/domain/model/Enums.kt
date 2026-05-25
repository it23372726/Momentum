package com.example.projectpbd.domain.model

enum class PaymentMethod {
    CASH,
    CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET,
    CRYPTO
}

enum class InsightType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

enum class TransactionType {
    REGULAR,
    GOAL_ALLOCATION,
    SAVINGS_ALLOCATION
}
