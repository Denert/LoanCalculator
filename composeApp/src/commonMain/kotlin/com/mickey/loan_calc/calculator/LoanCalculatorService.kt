package com.mickey.loan_calc.calculator

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import kotlin.math.pow

object LoanCalculatorService {

    fun calculate(
        amount: Double,
        annualRate: Double,
        termValue: Int,
        termUnit: TermUnit,
        startDate: LocalDate,
        paymentType: PaymentType
    ): LoanCalculationResult {
        val months = termToMonths(termValue, termUnit, startDate)
        if (months <= 0) return emptyResult()
        return when (paymentType) {
            PaymentType.ANNUITY -> calculateAnnuity(amount, annualRate, months, startDate)
            PaymentType.DIFFERENTIATED -> calculateDifferentiated(amount, annualRate, months, startDate)
        }
    }

    private fun termToMonths(termValue: Int, termUnit: TermUnit, startDate: LocalDate): Int {
        return when (termUnit) {
            TermUnit.MONTHS -> termValue
            TermUnit.YEARS -> termValue * 12
            TermUnit.DAYS -> {
                val endDate = startDate.plus(termValue, DateTimeUnit.DAY)
                val wholeMonths = startDate.until(endDate, DateTimeUnit.MONTH)
                val dateAfterWholeMonths = startDate.plus(wholeMonths, DateTimeUnit.MONTH)
                if (dateAfterWholeMonths < endDate) wholeMonths + 1 else wholeMonths
            }
        }
    }

    private fun calculateAnnuity(
        amount: Double,
        annualRate: Double,
        months: Int,
        startDate: LocalDate
    ): LoanCalculationResult {
        val r = annualRate / 100.0 / 12.0
        val payment = if (r == 0.0) {
            amount / months
        } else {
            amount * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
        }

        val payments = mutableListOf<MonthlyPayment>()
        var balance = amount

        for (i in 1..months) {
            val date = startDate.plus(i, DateTimeUnit.MONTH)
            if (i == months) {
                val lastInterest = balance * r
                payments.add(
                    MonthlyPayment(i, date, balance + lastInterest, balance, lastInterest, 0.0)
                )
            } else {
                val interest = balance * r
                val principal = payment - interest
                balance -= principal
                payments.add(
                    MonthlyPayment(i, date, payment, principal, interest, balance.coerceAtLeast(0.0))
                )
            }
        }

        val totalPayment = payments.sumOf { it.payment }
        return LoanCalculationResult(
            monthlyPayments = payments,
            totalPayment = totalPayment,
            totalInterest = payments.sumOf { it.interest },
            overpayment = totalPayment - amount
        )
    }

    private fun calculateDifferentiated(
        amount: Double,
        annualRate: Double,
        months: Int,
        startDate: LocalDate
    ): LoanCalculationResult {
        val r = annualRate / 100.0 / 12.0
        val principalPart = amount / months

        val payments = mutableListOf<MonthlyPayment>()
        var balance = amount

        for (i in 1..months) {
            val date = startDate.plus(i, DateTimeUnit.MONTH)
            if (i == months) {
                val lastInterest = balance * r
                payments.add(
                    MonthlyPayment(i, date, balance + lastInterest, balance, lastInterest, 0.0)
                )
            } else {
                val interest = balance * r
                val payment = principalPart + interest
                balance -= principalPart
                payments.add(
                    MonthlyPayment(i, date, payment, principalPart, interest, balance.coerceAtLeast(0.0))
                )
            }
        }

        val totalPayment = payments.sumOf { it.payment }
        return LoanCalculationResult(
            monthlyPayments = payments,
            totalPayment = totalPayment,
            totalInterest = payments.sumOf { it.interest },
            overpayment = totalPayment - amount
        )
    }

    private fun emptyResult() = LoanCalculationResult(
        monthlyPayments = emptyList(),
        totalPayment = 0.0,
        totalInterest = 0.0,
        overpayment = 0.0
    )
}