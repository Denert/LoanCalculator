package com.mickey.loan_calc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mickey.loan_calc.calculator.LoanCalculatorViewModel
import com.mickey.loan_calc.navigation.LoanNavigationBar
import com.mickey.loan_calc.navigation.Route
import com.mickey.loan_calc.result.LoanResultScreen
import com.mickey.loan_calc.saved.SavedLoansScreen

@Composable
fun App() {
    MaterialTheme {
        val viewModel = viewModel { LoanCalculatorViewModel() }
        var selectedRoute by remember { mutableStateOf<Route.TopLevel>(Route.TopLevel.CalculatorScreen) }

        Scaffold(
            bottomBar = {
                LoanNavigationBar(
                    selectedKey = selectedRoute,
                    onSelectKey = { key ->
                        if (key is Route.TopLevel) {
                            selectedRoute = key
                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedRoute) {
                    Route.TopLevel.CalculatorScreen -> LoanResultScreen(viewModel = viewModel)
                    Route.TopLevel.SaveListScreen -> SavedLoansScreen(
                        onLoanSelected = { loan ->
                            viewModel.loadFromSaved(loan)
                            selectedRoute = Route.TopLevel.CalculatorScreen
                        }
                    )
                    Route.TopLevel.SettingsScreen -> PlaceholderScreen("Настройки")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
