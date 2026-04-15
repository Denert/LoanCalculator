package com.mickey.loan_calc.saved

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SavedLoansRepository {
    private val _loans = MutableStateFlow<List<SavedLoan>>(emptyList())
    val loans: StateFlow<List<SavedLoan>> = _loans.asStateFlow()

    fun save(loan: SavedLoan) {
        _loans.update { it + loan }
    }

    fun delete(id: String) {
        _loans.update { it.filter { loan -> loan.id != id } }
    }
}
