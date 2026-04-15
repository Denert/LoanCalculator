package com.mickey.loan_calc.calculator

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TermUnit(val displayName: String) {
    DAYS("Дни"),
    MONTHS("Месяцы"),
    YEARS("Годы")
}

enum class PaymentType(val displayName: String) {
    ANNUITY("Аннуитетный"),
    DIFFERENTIATED("Дифференцированный")
}

data class DateState(
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int
)

data class LoanCalculatorState(
    val loanAmount: String = "1000000",
    val interestRate: String = "10",
    val termValue: String = "36",
    val termUnit: TermUnit = TermUnit.MONTHS,
    val startDate: DateState? = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .let { DateState(it.year, it.monthNumber, it.dayOfMonth) },
    val paymentType: PaymentType = PaymentType.ANNUITY,
    val additionalConditions: Boolean = false
)
