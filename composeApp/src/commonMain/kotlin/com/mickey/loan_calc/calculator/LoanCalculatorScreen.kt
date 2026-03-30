package com.mickey.loan_calc.calculator

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val GreenColor = Color(0xFF4CAF50)
private val PlaceholderColor = Color(0xFFBDBDBD)
private val LabelColor = Color(0xFF9E9E9E)

@Composable
fun LoanCalculatorScreen(
    onDismiss: () -> Unit = {},
    canDismiss: Boolean = true,
    viewModel: LoanCalculatorViewModel = viewModel { LoanCalculatorViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(2000f) }
    var containerHeight by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }

    val onDismissState = rememberUpdatedState(onDismiss)
    val canDismissState = rememberUpdatedState(canDismiss)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            // When sheet is pulled down and user scrolls back up — snap sheet back first
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && offsetY.value > 0f) {
                    val newOffset = (offsetY.value + delta).coerceAtLeast(0f)
                    val consumed = newOffset - offsetY.value
                    coroutineScope.launch { offsetY.snapTo(newOffset) }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            // When content is already at top and user drags down — move the sheet only if dismissible
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f && source == NestedScrollSource.UserInput && canDismissState.value) {
                    coroutineScope.launch {
                        offsetY.snapTo((offsetY.value + delta).coerceAtLeast(0f))
                    }
                    return available
                }
                return Offset.Zero
            }

            // Finger lifted — decide immediately without waiting for fling to finish
            override suspend fun onPreFling(available: Velocity): Velocity {
                val threshold = containerHeight * 0.2f
                return when {
                    available.y < 0f && offsetY.value > 0f -> {
                        offsetY.animateTo(0f, spring())
                        available
                    }
                    offsetY.value > threshold -> {
                        if (canDismissState.value) {
                            onDismissState.value()
                        } else {
                            offsetY.animateTo(0f, spring())
                        }
                        available
                    }
                    offsetY.value > 0f -> {
                        offsetY.animateTo(0f, spring())
                        available
                    }
                    else -> Velocity.Zero
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height }
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .nestedScroll(nestedScrollConnection)
            .padding(top = 8.dp)
    ) {
        BottomSheetContainer(
            modifier = Modifier.fillMaxSize(),
            shape = BottomSheetWithHandleShape(
                cornerRadius = 16.dp,
                handleBumpWidth = 100.dp,
                handleBumpHeight = 14.dp,
                dipDepth = 14.dp
            )
        ) {
            FormContent(
                state = state,
                onLoanAmountChange = viewModel::onLoanAmountChange,
                onInterestRateChange = viewModel::onInterestRateChange,
                onTermValueChange = viewModel::onTermValueChange,
                onTermUnitChange = viewModel::onTermUnitChange,
                onPaymentTypeChange = viewModel::onPaymentTypeChange,
                onAdditionalConditionsChange = viewModel::onAdditionalConditionsChange,
                onSaveClick = viewModel::onSaveClick,
                topPadding = 24.dp
            )
        }
    }
}

@Composable
private fun FormContent(
    state: LoanCalculatorState,
    onLoanAmountChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit,
    onTermValueChange: (String) -> Unit,
    onTermUnitChange: (TermUnit) -> Unit,
    onPaymentTypeChange: (PaymentType) -> Unit,
    onAdditionalConditionsChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    topPadding: androidx.compose.ui.unit.Dp = 24.dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LabeledTextField(
            label = "Сумма кредита, ₽",
            value = state.loanAmount,
            onValueChange = onLoanAmountChange,
            placeholder = "Введите сумму кредита",
            leadingIcon = Icons.Default.CreditCard,
            visualTransformation = ThousandsVisualTransformation()
        )

        LabeledTextField(
            label = "Процентная ставка, %",
            value = state.interestRate,
            onValueChange = onInterestRateChange,
            placeholder = "Введите процент",
            leadingIcon = Icons.Default.Percent
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Срок кредита", fontSize = 14.sp, color = LabelColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TermValueField(
                    value = state.termValue,
                    onValueChange = onTermValueChange,
                    modifier = Modifier.weight(1f)
                )
                TermUnitDropdown(
                    selected = state.termUnit,
                    onSelect = onTermUnitChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        DateField(label = "Дата кредита", date = state.startDate)

        PaymentTypeSelector(selected = state.paymentType, onSelect = onPaymentTypeChange)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Дополнительные условия", fontSize = 16.sp, color = Color.Black)
            Switch(
                checked = state.additionalConditions,
                onCheckedChange = onAdditionalConditionsChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GreenColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenColor)
        ) {
            Text(text = "Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontSize = 14.sp, color = LabelColor)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = PlaceholderColor) },
            leadingIcon = {
                Icon(imageVector = leadingIcon, contentDescription = null, tint = PlaceholderColor)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = GreenColor,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = visualTransformation
        )
    }
}

@Composable
private fun TermValueField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(text = "Срок", color = PlaceholderColor) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = PlaceholderColor
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = GreenColor,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TermUnitDropdown(
    selected: TermUnit,
    onSelect: (TermUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = GreenColor,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TermUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.displayName) },
                    onClick = { onSelect(unit); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DateField(label: String, date: DateState?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontSize = 14.sp, color = LabelColor)
        OutlinedTextField(
            value = date?.let {
                "${it.dayOfMonth.toString().padStart(2, '0')}.${it.monthNumber.toString().padStart(2, '0')}.${it.year}"
            } ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            placeholder = { Text(text = "Выберите дату", color = PlaceholderColor) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PlaceholderColor
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = GreenColor,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun PaymentTypeSelector(selected: PaymentType, onSelect: (PaymentType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        PaymentType.entries.forEachIndexed { index, type ->
            val isSelected = type == selected
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                PaymentType.entries.lastIndex -> RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Surface(
                modifier = Modifier.weight(1f).height(48.dp).clickable { onSelect(type) },
                shape = shape,
                color = if (isSelected) GreenColor else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = type.displayName,
                        color = if (isSelected) Color.White else LabelColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}