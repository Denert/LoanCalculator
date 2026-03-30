package com.mickey.loan_calc.result

import com.mickey.loan_calc.calculator.MonthlyPayment

data class LoanResultState(
    val loanAmount: String = "0 ₽",
    val interestRate: String = "—",
    val term: String = "—",
    val startDate: String = "—",
    val monthlyPayment: String = "0 ₽",
    val paymentSize: String = "0 ₽",
    val overpayment: String = "0 ₽",
    val totalPayment: String = "0 ₽",
    val nextPaymentDate: String = "—",
    val paymentSchedule: List<MonthlyPayment> = emptyList(),
    val loanAmountRaw: Double = 0.0,
    val overpaymentRaw: Double = 0.0
)