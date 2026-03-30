package com.mickey.loan_calc.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class LoanCalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoanCalculatorState())
    val state: StateFlow<LoanCalculatorState> = _state.asStateFlow()

    private val _calculationResult = MutableStateFlow<LoanCalculationResult?>(null)
    val calculationResult: StateFlow<LoanCalculationResult?> = _calculationResult.asStateFlow()

    init {
        viewModelScope.launch {
            state.collect { s ->
                _calculationResult.value = withContext(Dispatchers.Default) { calculate(s) }
            }
        }
    }

    private fun calculate(s: LoanCalculatorState): LoanCalculationResult? {
        val amount = s.loanAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
        val rate = s.interestRate.toDoubleOrNull()?.takeIf { it >= 0 } ?: return null
        val termVal = s.termValue.toIntOrNull()?.takeIf { it > 0 } ?: return null
        val dateState = s.startDate ?: return null
        val startDate = LocalDate(dateState.year, dateState.monthNumber, dateState.dayOfMonth)
        return LoanCalculatorService.calculate(amount, rate, termVal, s.termUnit, startDate, s.paymentType)
    }

    fun onLoanAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(loanAmount = filtered) }
    }

    fun onInterestRateChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(interestRate = filtered) }
    }

    fun onTermValueChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _state.update { it.copy(termValue = filtered) }
    }

    fun onTermUnitChange(unit: TermUnit) {
        _state.update { it.copy(termUnit = unit) }
    }

    fun onPaymentTypeChange(type: PaymentType) {
        _state.update { it.copy(paymentType = type) }
    }

    fun onAdditionalConditionsChange(enabled: Boolean) {
        _state.update { it.copy(additionalConditions = enabled) }
    }

    fun onSaveClick() {
        // TODO: implement save logic
    }
}