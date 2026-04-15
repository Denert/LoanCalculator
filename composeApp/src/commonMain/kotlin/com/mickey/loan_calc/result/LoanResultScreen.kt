package com.mickey.loan_calc.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mickey.loan_calc.calculator.LoanCalculationResult
import com.mickey.loan_calc.calculator.LoanCalculatorScreen
import com.mickey.loan_calc.calculator.LoanCalculatorState
import com.mickey.loan_calc.calculator.LoanCalculatorViewModel
import com.mickey.loan_calc.calculator.TermUnit

private val BackgroundColor = Color(0xFFF5F5F5)

@Composable
fun LoanResultScreen(
    modifier: Modifier = Modifier,
    viewModel: LoanCalculatorViewModel = viewModel { LoanCalculatorViewModel() }
) {
    val inputState by viewModel.state.collectAsState()
    val result by viewModel.calculationResult.collectAsState()
    val resultState = remember(inputState, result) { buildResultState(inputState, result) }

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val isLoanExpired = remember(result, today) {
        result?.monthlyPayments?.lastOrNull()?.date?.let { it < today } ?: false
    }

    var showCalculatorSheet by remember { mutableStateOf(true) }

    LaunchedEffect(isLoanExpired) {
        if (isLoanExpired) showCalculatorSheet = true
    }

    val backButtonAlpha by animateFloatAsState(
        targetValue = if (showCalculatorSheet) 0f else 1f,
        label = "backButtonAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showCalculatorSheet) "Кредитный калькулятор" else "Ежемесячный платёж",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showCalculatorSheet = true },
                enabled = !showCalculatorSheet,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .alpha(backButtonAlpha)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        }

        // Monthly payment amount
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = resultState.monthlyPayment,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (showCalculatorSheet && resultState.monthlyPayment != "0 ₽" && !isLoanExpired) {
                IconButton(
                    onClick = { showCalculatorSheet = false },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Показать результаты",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Content area — takes remaining space
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LoanResultBody(state = resultState)
            AnimatedCalculatorSheet(
                visible = showCalculatorSheet,
                canDismiss = result != null && !isLoanExpired,
                isLoanExpired = isLoanExpired,
                viewModel = viewModel,
                onDismiss = { showCalculatorSheet = false }
            )
        }
    }
}

@Composable
private fun AnimatedCalculatorSheet(
    visible: Boolean,
    canDismiss: Boolean,
    isLoanExpired: Boolean,
    viewModel: LoanCalculatorViewModel,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = slideOutVertically(
            animationSpec = tween(350, easing = FastOutSlowInEasing),
            targetOffsetY = { it }
        )
    ) {
        LoanCalculatorScreen(
            viewModel = viewModel,
            canDismiss = canDismiss,
            isLoanExpired = isLoanExpired,
            onDismiss = onDismiss
        )
    }
}

private fun buildResultState(
    input: LoanCalculatorState,
    result: LoanCalculationResult?
): LoanResultState {
    if (result == null || result.monthlyPayments.isEmpty()) return LoanResultState()
    val firstPayment = result.monthlyPayments.first()
    return LoanResultState(
        loanAmount = formatMoney(input.loanAmount.toDoubleOrNull() ?: 0.0),
        interestRate = "${input.interestRate}%",
        term = formatTerm(input.termValue, input.termUnit),
        startDate = input.startDate?.let {
            formatDate(it.dayOfMonth, it.monthNumber, it.year)
        } ?: "—",
        monthlyPayment = formatMoney(firstPayment.payment),
        paymentSize = formatMoney(firstPayment.payment),
        overpayment = formatMoney(result.overpayment),
        totalPayment = formatMoney(result.totalPayment),
        nextPaymentDate = formatDate(
            firstPayment.date.dayOfMonth,
            firstPayment.date.monthNumber,
            firstPayment.date.year
        ),
        paymentSchedule = result.monthlyPayments,
        loanAmountRaw = input.loanAmount.toDoubleOrNull() ?: 0.0,
        overpaymentRaw = result.overpayment
    )
}

private fun formatMoney(amount: Double): String {
    val intPart = amount.toLong()
    val frac = kotlin.math.round((amount - intPart) * 100).toInt().coerceIn(0, 99)
    val s = intPart.toString()
    val grouped = buildString {
        s.forEachIndexed { i, c ->
            if (i > 0 && (s.length - i) % 3 == 0) append('\u00A0')
            append(c)
        }
    }
    return "$grouped,${frac.toString().padStart(2, '0')} ₽"
}

private fun formatDate(day: Int, month: Int, year: Int) =
    "${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year"

private fun formatTerm(termValue: String, termUnit: TermUnit): String {
    val v = termValue.toIntOrNull() ?: return "—"
    val suffix = when (termUnit) {
        TermUnit.DAYS -> pluralRu(v, "день", "дня", "дней")
        TermUnit.MONTHS -> pluralRu(v, "месяц", "месяца", "месяцев")
        TermUnit.YEARS -> pluralRu(v, "год", "года", "лет")
    }
    return "$v $suffix"
}

private fun pluralRu(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..19 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}