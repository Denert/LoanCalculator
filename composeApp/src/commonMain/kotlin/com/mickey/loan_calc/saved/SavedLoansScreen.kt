package com.mickey.loan_calc.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenColor = Color(0xFF4CAF50)
private val LabelColor = Color(0xFF9E9E9E)
private val BackgroundColor = Color(0xFFF5F5F5)

@Composable
fun SavedLoansScreen(onLoanSelected: (SavedLoan) -> Unit) {
    val loans by SavedLoansRepository.loans.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Сохранённые",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (loans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Нет сохранённых кредитов",
                    fontSize = 16.sp,
                    color = LabelColor
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(loans) { loan ->
                    SavedLoanItem(loan = loan, onClick = { onLoanSelected(loan) })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SavedLoanItem(loan: SavedLoan, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = loan.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = formatAmount(loan.loanAmount),
                    fontSize = 14.sp,
                    color = GreenColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${loan.termValue} ${termUnitLabel(loan.termValue, loan.termUnit.displayName)} · ${loan.interestRate}%",
                    fontSize = 13.sp,
                    color = LabelColor
                )
            }
        }
    }
}

private fun formatAmount(raw: String): String {
    val amount = raw.toLongOrNull() ?: return raw
    val s = amount.toString()
    val grouped = buildString {
        s.forEachIndexed { i, c ->
            if (i > 0 && (s.length - i) % 3 == 0) append('\u00A0')
            append(c)
        }
    }
    return "$grouped ₽"
}

private fun termUnitLabel(value: String, displayName: String): String = displayName
