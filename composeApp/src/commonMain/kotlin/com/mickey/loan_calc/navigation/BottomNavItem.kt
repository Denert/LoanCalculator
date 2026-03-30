package com.mickey.loan_calc.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings

data class BottomNavItem(
    val icon: ImageVector,
    val title: String
)

val TOP_LEVEL_DESTINATION = mapOf(
    Route.TopLevel.CalculatorScreen to BottomNavItem(
        icon = Icons.Outlined.Calculate,
        title = "Расчёт"
    ),
    Route.TopLevel.SaveListScreen to BottomNavItem(
        icon = Icons.Outlined.Savings,
        title = "Сохраненные"
    ),
    Route.TopLevel.SettingsScreen to BottomNavItem(
        icon = Icons.Outlined.Settings,
        title = "Настройки"
    ),
)