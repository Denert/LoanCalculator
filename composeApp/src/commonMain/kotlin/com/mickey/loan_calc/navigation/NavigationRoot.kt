package com.mickey.loan_calc.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.mickey.loan_calc.calculator.LoanCalculatorScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    var selectedKey: NavKey by remember { mutableStateOf(Route.TopLevel.CalculatorScreen) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            LoanNavigationBar(
                selectedKey = selectedKey,
                onSelectKey = { selectedKey = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedKey) {
                Route.TopLevel.CalculatorScreen -> LoanCalculatorScreen()
                Route.TopLevel.SaveListScreen -> PlaceholderScreen("Сохраненные")
                Route.TopLevel.SettingsScreen -> PlaceholderScreen("Настройки")
                else -> PlaceholderScreen("Unknown")
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
        Text(text = title)
    }
}
