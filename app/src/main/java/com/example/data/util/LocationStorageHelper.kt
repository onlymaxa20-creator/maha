package com.example.data.util

import android.content.Context
import com.example.data.model.DeliveryLocationData

object LocationStorageHelper {
    private const val PREFS_NAME = "gagarin_delivery_location_prefs"
    private const val KEY_LATITUDE = "last_delivery_lat"
    private const val KEY_LONGITUDE = "last_delivery_lng"
    private const val KEY_ADDRESS = "last_delivery_address"
    private const val KEY_STREET = "last_delivery_street"
    private const val KEY_HOUSE = "last_delivery_house"
    private const val KEY_LANDMARK = "last_delivery_landmark"

    // Default Gagarin city center coordinates (Mirzacho'l District, Jizzakh Region, Uzbekistan)
    const val DEFAULT_GAGARIN_LAT = 40.6622
    const val DEFAULT_GAGARIN_LNG = 68.1672
    const val DEFAULT_GAGARIN_ADDRESS = "Gagarin shahri, Mirzacho‘l tumani"

    fun saveLocation(context: Context, location: DeliveryLocationData) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat(KEY_LATITUDE, location.latitude.toFloat())
                .putFloat(KEY_LONGITUDE, location.longitude.toFloat())
                .putString(KEY_ADDRESS, location.readableAddress)
                .putString(KEY_STREET, location.street)
                .putString(KEY_HOUSE, location.houseNumber)
                .putString(KEY_LANDMARK, location.landmark)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedLocation(context: Context): DeliveryLocationData {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lat = prefs.getFloat(KEY_LATITUDE, DEFAULT_GAGARIN_LAT.toFloat()).toDouble()
            val lng = prefs.getFloat(KEY_LONGITUDE, DEFAULT_GAGARIN_LNG.toFloat()).toDouble()
            val address = prefs.getString(KEY_ADDRESS, DEFAULT_GAGARIN_ADDRESS) ?: DEFAULT_GAGARIN_ADDRESS
            val street = prefs.getString(KEY_STREET, "") ?: ""
            val house = prefs.getString(KEY_HOUSE, "") ?: ""
            val landmark = prefs.getString(KEY_LANDMARK, "") ?: ""
            DeliveryLocationData(
                latitude = lat,
                longitude = lng,
                readableAddress = address,
                street = street,
                houseNumber = house,
                landmark = landmark
            )
        } catch (_: Exception) {
            DeliveryLocationData(
                latitude = DEFAULT_GAGARIN_LAT,
                longitude = DEFAULT_GAGARIN_LNG,
                readableAddress = DEFAULT_GAGARIN_ADDRESS
            )
        }
    }
}
