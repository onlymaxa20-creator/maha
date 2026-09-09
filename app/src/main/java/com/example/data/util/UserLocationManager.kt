package com.example.data.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.DeliveryLocationData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Manages real-time user location detection and Geocoding for Gagarin Go.
 * Reactively responds to GPS toggle, auto-fetches precise location, and
 * formats coordinates into human-readable Uzbek street and district names.
 */
class UserLocationManager(private val context: Context) {

    private val TAG = "UserLocationManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val savedInitial = LocationStorageHelper.getSavedLocation(context)

    private val _currentAddress = MutableStateFlow(
        savedInitial.readableAddress.ifBlank { "Gagarin shahri, Markaz" }
    )
    val currentAddress: StateFlow<String> = _currentAddress.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(true)
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _coordinates = MutableStateFlow<Pair<Double, Double>?>(
        Pair(savedInitial.latitude, savedInitial.longitude)
    )
    val coordinates: StateFlow<Pair<Double, Double>?> = _coordinates.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private var activeCallback: LocationCallback? = null
    private var isReceiverRegistered = false

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val wasEnabled = _isGpsEnabled.value
                val nowEnabled = checkGpsState()
                Log.d(TAG, "GPS providers changed. Now enabled: $nowEnabled (was: $wasEnabled)")
                if (nowEnabled && !wasEnabled) {
                    // GPS was just switched ON! Immediately fetch location!
                    scope.launch {
                        fetchCurrentLocation(forceHighAccuracy = true)
                    }
                }
            }
        }
    }

    init {
        checkGpsState()
        startMonitoring()
    }

    fun startMonitoring() {
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
                context.registerReceiver(gpsReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register GPS receiver: ${e.message}")
            }
        }
    }

    fun stopMonitoring() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(gpsReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(TAG, "Failed to unregister GPS receiver: ${e.message}")
            }
        }
        stopLocationUpdates()
    }

    fun checkGpsState(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val enabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        _isGpsEnabled.value = enabled
        if (!enabled) {
            _isLocating.value = false
        }
        return enabled
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open location settings: ${e.message}")
        }
    }

    fun setManualAddress(address: String) {
        if (address.isNotBlank()) {
            val trimmed = address.trim()
            _currentAddress.value = trimmed
            val coords = _coordinates.value ?: Pair(
                LocationStorageHelper.DEFAULT_GAGARIN_LAT,
                LocationStorageHelper.DEFAULT_GAGARIN_LNG
            )
            LocationStorageHelper.saveLocation(
                context,
                DeliveryLocationData(
                    latitude = coords.first,
                    longitude = coords.second,
                    readableAddress = trimmed,
                    street = trimmed
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(forceHighAccuracy: Boolean = true) = withContext(Dispatchers.IO) {
        val gpsOn = checkGpsState()
        if (!gpsOn) {
            withContext(Dispatchers.Main) {
                _isLocating.value = false
            }
            return@withContext
        }

        if (!hasLocationPermission()) {
            withContext(Dispatchers.Main) {
                _isLocating.value = false
            }
            return@withContext
        }

        withContext(Dispatchers.Main) {
            _isLocating.value = true
        }

        try {
            // 1. Single accurate location request
            val cts = CancellationTokenSource()
            val priority = if (forceHighAccuracy) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

            fusedClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        resolveAddress(loc.latitude, loc.longitude)
                    } else {
                        // 2. Try last known location from fused client
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                resolveAddress(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                fallbackToDeviceLocationManager()
                            }
                        }.addOnFailureListener {
                            fallbackToDeviceLocationManager()
                        }
                    }
                }
                .addOnFailureListener {
                    fallbackToDeviceLocationManager()
                }

            // 3. Simultaneously request a quick location update for satellite lock
            withContext(Dispatchers.Main) {
                requestFreshLocationUpdate()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fused location error: ${e.message}, falling back")
            fallbackToDeviceLocationManager()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocationUpdate() {
        if (!hasLocationPermission() || !checkGpsState()) return

        stopLocationUpdates()
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMaxUpdates(2)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        resolveAddress(location.latitude, location.longitude)
                        stopLocationUpdates()
                    }
                }
            }
            activeCallback = callback
            fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to request fresh location update: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        activeCallback?.let { callback ->
            try {
                fusedClient.removeLocationUpdates(callback)
            } catch (_: Exception) {}
            activeCallback = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToDeviceLocationManager() {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val netLoc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val best = gpsLoc ?: netLoc

            if (best != null) {
                resolveAddress(best.latitude, best.longitude)
            } else {
                _isLocating.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "LocationManager fallback failed: ${e.message}")
            _isLocating.value = false
        }
    }

    private fun resolveAddress(latitude: Double, longitude: Double) {
        _coordinates.value = Pair(latitude, longitude)
        stopLocationUpdates()

        try {
            val geocoder = Geocoder(context, Locale("uz", "UZ"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    handleAddressList(addresses, latitude, longitude)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                handleAddressList(addresses, latitude, longitude)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder failed: ${e.message}")
            handleAddressList(null, latitude, longitude)
        }
    }

    private fun handleAddressList(addresses: List<Address>?, latitude: Double, longitude: Double) {
        var streetName = ""
        var houseNum = ""
        var formattedAddress = ""

        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            streetName = addr.thoroughfare ?: ""
            houseNum = addr.subThoroughfare ?: ""
            val subLocality = addr.subLocality ?: ""
            val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Gagarin"

            formattedAddress = when {
                streetName.isNotBlank() && houseNum.isNotBlank() -> "$streetName, $houseNum, $locality"
                streetName.isNotBlank() -> "$streetName, $locality"
                subLocality.isNotBlank() -> "$subLocality, $locality"
                locality.isNotBlank() -> locality
                else -> addr.getAddressLine(0)?.split(",")?.take(2)?.joinToString(",") ?: "Gagarin shahri"
            }
        } else {
            formattedAddress = "Gagarin shahri (GPS)"
        }

        _currentAddress.value = formattedAddress
        _isLocating.value = false

        // Save into LocationStorageHelper for persistent access across the entire app
        try {
            LocationStorageHelper.saveLocation(
                context,
                DeliveryLocationData(
                    latitude = latitude,
                    longitude = longitude,
                    readableAddress = formattedAddress,
                    street = streetName,
                    houseNumber = houseNum,
                    landmark = ""
                )
            )
        } catch (_: Exception) {}
    }
}
