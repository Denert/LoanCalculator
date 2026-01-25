package com.mickey.loan_calc.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route: NavKey {

    sealed interface TopLevel: Route {
        @Serializable
        data object CalculateScreen: TopLevel

        @Serializable
        data object SaveListScreen: TopLevel

        @Serializable
        data object SettingsScreen: TopLevel
    }

    @Serializable
    data object CalculationResultScreen: Route

}