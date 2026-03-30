package com.mickey.loan_calc.result

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mickey.loan_calc.calculator.MonthlyPayment
import kotlinx.datetime.LocalDate

private val GreenColor = Color(0xFF4CAF50)
private val LabelColor = Color(0xFF9E9E9E)
private val ChartGrayColor = Color(0xFFB0BEC5)

@Composable
fun LoanResultBody(state: LoanResultState) {
    var selectedTab by remember { mutableStateOf(0) }

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
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) GreenColor else Color.Transparent,
                            contentColor = if (selectedTab == 0) Color.White else LabelColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(text = "График", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) GreenColor else Color.Transparent,
                            contentColor = if (selectedTab == 1) Color.White else LabelColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(text = "Диаграмма", fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedTab == 0) {
            if (state.paymentSchedule.isNotEmpty()) {
                item {
                    PaymentScheduleHeader()
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                }
                items(state.paymentSchedule) { payment ->
                    PaymentScheduleItem(payment)
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
private fun PaymentScheduleItem(payment: MonthlyPayment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${payment.month}  ${formatLocalDate(payment.date)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = formatMoney(payment.payment),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Долг: ${formatMoney(payment.principal)}", fontSize = 12.sp, color = LabelColor)
            Text(text = "Проценты: ${formatMoney(payment.interest)}", fontSize = 12.sp, color = LabelColor)
        }
        Text(
            text = "Остаток: ${formatMoney(payment.remainingBalance)}",
            fontSize = 12.sp,
            color = LabelColor
        )
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