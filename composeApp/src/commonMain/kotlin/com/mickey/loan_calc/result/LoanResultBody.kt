package com.mickey.loan_calc.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mickey.loan_calc.calculator.MonthlyPayment
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val GreenColor = Color(0xFF4CAF50)
private val LabelColor = Color(0xFF9E9E9E)
private val ChartGrayColor = Color(0xFFB0BEC5)

private enum class PaymentStatus { PAID, CURRENT, UPCOMING }

@Composable
fun LoanResultBody(state: LoanResultState) {
    var selectedTab by remember { mutableStateOf(0) }

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val currentPaymentIndex = remember(state.paymentSchedule, today) {
        state.paymentSchedule.indexOfFirst { it.date >= today }
    }

    val availableYears = remember(state.paymentSchedule) {
        state.paymentSchedule.map { it.date.year }.distinct().sorted()
    }
    var selectedYear by remember(availableYears) {
        val currentYear = today.year
        mutableStateOf(
            if (currentYear in availableYears) currentYear else availableYears.firstOrNull() ?: 0
        )
    }
    var showYearPickerDialog by remember { mutableStateOf(false) }

    if (showYearPickerDialog) {
        YearPickerDialog(
            years = availableYears,
            selectedYear = selectedYear,
            onYearSelected = { year ->
                selectedYear = year
                showYearPickerDialog = false
            },
            onDismiss = { showYearPickerDialog = false }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryRow(label = "Кредит:", value = state.loanAmount)
                SummaryRow(label = "Кредитная ставка:", value = state.interestRate)
                SummaryRow(label = "Срок выплаты:", value = state.term)
                SummaryRow(label = "Дата получения:", value = state.startDate)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultRow(label = "Размер платежа:", value = state.paymentSize)
                    ResultRow(label = "Переплата:", value = state.overpayment)
                    ResultRow(label = "Сумма выплат:", value = state.totalPayment)
                    ResultRow(label = "Дата первого платежа:", value = state.nextPaymentDate)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(48.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) GreenColor else Color.Transparent,
                            contentColor = if (selectedTab == 0) Color.White else LabelColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(text = "График", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) GreenColor else Color.Transparent,
                            contentColor = if (selectedTab == 1) Color.White else LabelColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(text = "Диаграмма", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedTab == 0) {
            if (state.paymentSchedule.isNotEmpty()) {
                item {
                    YearSelectorRow(
                        years = availableYears,
                        selectedYear = selectedYear,
                        onYearSelected = { selectedYear = it },
                        onEllipsisClick = { showYearPickerDialog = true }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PaymentScheduleHeader()
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                }
                items(state.paymentSchedule.filter { it.date.year == selectedYear }) { payment ->
                    val globalIndex = state.paymentSchedule.indexOf(payment)
                    val status = when {
                        currentPaymentIndex == -1 -> PaymentStatus.PAID
                        globalIndex < currentPaymentIndex -> PaymentStatus.PAID
                        globalIndex == currentPaymentIndex -> PaymentStatus.CURRENT
                        else -> PaymentStatus.UPCOMING
                    }
                    PaymentScheduleItem(payment, status)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                LoanPieChartCard(state = state)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LoanPieChartCard(state: LoanResultState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Состав платежа:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                DonutChart(
                    loanAmount = state.loanAmountRaw,
                    overpayment = state.overpaymentRaw,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 8.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.loanAmount,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenColor
                    )
                    Text(
                        text = state.overpayment,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartGrayColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = GreenColor, label = "Размер кредита")
                LegendItem(color = ChartGrayColor, label = "Переплата")
            }
        }
    }
}

@Composable
private fun DonutChart(
    loanAmount: Double,
    overpayment: Double,
    modifier: Modifier = Modifier
) {
    val total = loanAmount + overpayment
    val loanFraction = if (total > 0) (loanAmount / total).toFloat() else 0f

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.26f
        val arcRadius = (size.minDimension - strokeWidth) / 2f

        val capExtDeg = if (arcRadius > 0f) {
            val ratio = (strokeWidth / 2f / arcRadius).toDouble()
            (kotlin.math.atan(ratio) * 180.0 / kotlin.math.PI).toFloat()
        } else 0f

        val visualGap = 10f
        val totalGap = visualGap + 2f * capExtDeg

        val greenDrawnSweep = (loanFraction * 360f - totalGap).coerceAtLeast(0f)
        val grayDrawnSweep = ((1f - loanFraction) * 360f - totalGap).coerceAtLeast(0f)

        val greenStart = -90f + capExtDeg
        val grayStart = greenStart + greenDrawnSweep + totalGap

        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        if (grayDrawnSweep > 0f) {
            drawArc(
                color = ChartGrayColor,
                startAngle = grayStart,
                sweepAngle = grayDrawnSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }

        if (greenDrawnSweep > 0f) {
            drawArc(
                color = GreenColor,
                startAngle = greenStart,
                sweepAngle = greenDrawnSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color
        ) {}
        Text(text = label, fontSize = 14.sp, color = LabelColor)
    }
}

@Composable
private fun YearSelectorRow(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onEllipsisClick: () -> Unit
) {
    val selectedIndex = years.indexOf(selectedYear)
    val prevYear = if (selectedIndex > 0) years[selectedIndex - 1] else null
    val nextYear = if (selectedIndex < years.lastIndex) years[selectedIndex + 1] else null
    val hasMoreLeft = selectedIndex > 1
    val hasMoreRight = selectedIndex < years.lastIndex - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasMoreLeft) {
            Text(
                text = "…",
                fontSize = 18.sp,
                color = LabelColor,
                modifier = Modifier
                    .clickable { onEllipsisClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(42.dp))
        }

        if (prevYear != null) {
            Text(
                text = prevYear.toString(),
                fontSize = 15.sp,
                color = LabelColor,
                modifier = Modifier
                    .clickable { onYearSelected(prevYear) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(66.dp))
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GreenColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = selectedYear.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (nextYear != null) {
            Text(
                text = nextYear.toString(),
                fontSize = 15.sp,
                color = LabelColor,
                modifier = Modifier
                    .clickable { onYearSelected(nextYear) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(66.dp))
        }

        if (hasMoreRight) {
            Text(
                text = "…",
                fontSize = 18.sp,
                color = LabelColor,
                modifier = Modifier
                    .clickable { onEllipsisClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(42.dp))
        }
    }
}

@Composable
private fun YearPickerDialog(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Выберите год",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                years.forEach { year ->
                    val isSelected = year == selectedYear
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GreenColor else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onYearSelected(year) }
                    ) {
                        Text(
                            text = year.toString(),
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentScheduleHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Месяц / Дата", fontSize = 12.sp, color = LabelColor, fontWeight = FontWeight.SemiBold)
        Text(text = "Платёж", fontSize = 12.sp, color = LabelColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PaymentScheduleItem(payment: MonthlyPayment, status: PaymentStatus) {
    val isPaid = status == PaymentStatus.PAID
    val isCurrent = status == PaymentStatus.CURRENT

    val textColor = if (isPaid) LabelColor else Color.Black
    val amountColor = if (isPaid) LabelColor else GreenColor
    val detailColor = if (isPaid) Color(0xFFBDBDBD) else LabelColor
    val bgColor = if (isCurrent) Color(0xFFF1F8F1) else Color.Transparent

    Surface(color = bgColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${payment.month}  ${formatLocalDate(payment.date)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    when (status) {
                        PaymentStatus.PAID -> Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0F0F0)
                        ) {
                            Text(
                                text = "✓",
                                fontSize = 11.sp,
                                color = LabelColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        PaymentStatus.CURRENT -> Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GreenColor
                        ) {
                            Text(
                                text = "Текущий",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        PaymentStatus.UPCOMING -> {}
                    }
                }
                Text(
                    text = formatMoney(payment.payment),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Долг: ${formatMoney(payment.principal)}", fontSize = 12.sp, color = detailColor)
                Text(text = "Проценты: ${formatMoney(payment.interest)}", fontSize = 12.sp, color = detailColor)
            }
            Text(
                text = "Остаток: ${formatMoney(payment.remainingBalance)}",
                fontSize = 12.sp,
                color = detailColor
            )
        }
    }
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = LabelColor)
        Text(text = value, fontSize = 15.sp, color = LabelColor)
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = LabelColor)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
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

private fun formatLocalDate(date: LocalDate): String =
    "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}.${date.year}"