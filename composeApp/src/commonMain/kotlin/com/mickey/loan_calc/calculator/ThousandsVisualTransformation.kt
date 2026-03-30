package com.mickey.loan_calc.calculator

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ThousandsVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text

        val dotIndex = original.indexOf('.')
        val intPart = if (dotIndex >= 0) original.substring(0, dotIndex) else original
        val decPart = if (dotIndex >= 0) original.substring(dotIndex) else ""

        val formattedInt = buildString {
            intPart.reversed().forEachIndexed { i, c ->
                if (i > 0 && i % 3 == 0) append('\u00A0')
                append(c)
            }
        }.reversed()

        val formatted = formattedInt + decPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= intPart.length) {
                    val n = intPart.length
                    val totalSpaces = if (n > 0) (n - 1) / 3 else 0
                    val digitsRight = n - offset
                    val spacesRight = if (digitsRight > 0) (digitsRight - 1) / 3 else 0
                    offset + (totalSpaces - spacesRight)
                } else {
                    formattedInt.length + (offset - intPart.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                var origCount = 0
                for (i in 0 until minOf(offset, formatted.length)) {
                    if (formatted[i] != '\u00A0') origCount++
                }
                return origCount.coerceAtMost(original.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}