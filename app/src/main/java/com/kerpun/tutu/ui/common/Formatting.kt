package com.kerpun.tutu.ui.common

import java.text.NumberFormat
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

private val amountNumberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private val monthAbbreviations = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

fun formatAmount(amount: Double): String = "S/ ${amountNumberFormat.format(amount)}"

fun todayLocalDate(): LocalDate = java.time.LocalDate.now().toKotlinLocalDate()

fun LocalDate.toDisplayLabel(today: LocalDate = todayLocalDate()): String {
    if (this == today) return "Hoy"
    return "$dayOfMonth ${monthAbbreviations[monthNumber - 1]}"
}
