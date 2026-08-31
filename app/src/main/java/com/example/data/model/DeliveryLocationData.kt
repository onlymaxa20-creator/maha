package com.example.data.model

data class DeliveryLocationData(
    val latitude: Double = 40.6622,
    val longitude: Double = 68.1672,
    val readableAddress: String = "Gagarin shahri, Mirzacho‘l tumani",
    val street: String = "",
    val houseNumber: String = "",
    val landmark: String = ""
) {
    fun getFullFormattedAddress(): String {
        val parts = mutableListOf<String>()
        if (readableAddress.isNotBlank()) parts.add(readableAddress)
        if (street.isNotBlank() && !readableAddress.contains(street, ignoreCase = true)) parts.add("Ko‘cha: $street")
        if (houseNumber.isNotBlank()) parts.add("Uy: $houseNumber")
        if (landmark.isNotBlank()) parts.add("Mo‘ljal: $landmark")
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "Gagarin shahri, Mirzacho‘l tumani"
    }
}
