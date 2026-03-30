package com.mickey.loan_calc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mickey.loan_calc.result.LoanResultScreen
import com.mickey.loan_calc.navigation.LoanNavigationBar
import com.mickey.loan_calc.navigation.Route

@Composable
fun App() {
    MaterialTheme {
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
                    Route.TopLevel.CalculatorScreen -> LoanResultScreen()
                    Route.TopLevel.SaveListScreen -> PlaceholderScreen("Сохраненные")
                    Route.TopLevel.SettingsScreen -> PlaceholderScreen("Настройки")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
