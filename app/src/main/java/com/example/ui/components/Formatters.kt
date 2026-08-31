package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val uzbekSymbols = DecimalFormatSymbols(Locale.getDefault()).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    private val decimalFormat = DecimalFormat("#,###", uzbekSymbols)

    fun formatPrice(amount: Double): String {
        return "${decimalFormat.format(amount.toLong())} so‘m"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
