package com.mickey.loan_calc.calculator

import kotlinx.datetime.LocalDate

data class MonthlyPayment(
    val month: Int,
    val date: LocalDate,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
)

data class LoanCalculationResult(
    val monthlyPayments: List<MonthlyPayment>,
    val totalPayment: Double,
    val totalInterest: Double,
    val overpayment: Double
)