package com.mickey.loan_calc.saved

import com.mickey.loan_calc.calculator.DateState
import com.mickey.loan_calc.calculator.PaymentType
import com.mickey.loan_calc.calculator.TermUnit

data class SavedLoan(
    val id: String,
    val name: String,
    val loanAmount: String,
    val interestRate: String,
    val termValue: String,
    val termUnit: TermUnit,
    val startDate: DateState?,
    val paymentType: PaymentType
)
