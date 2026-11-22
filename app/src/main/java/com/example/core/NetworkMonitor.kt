package com.example.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ConnectivityObserver {
    val isOffline: StateFlow<Boolean>
}

class StaticConnectivityObserver(initiallyOffline: Boolean = false) : ConnectivityObserver {
    private val _isOffline = MutableStateFlow(initiallyOffline)
    override val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }
}

class NetworkMonitor(context: Context) : ConnectivityObserver {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOffline = MutableStateFlow(!hasValidatedInternet())
    override val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOffline.value = !hasValidatedInternet()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val online = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            _isOffline.value = !online
        }

        override fun onLost(network: Network) {
            _isOffline.value = !hasValidatedInternet()
        }

        override fun onUnavailable() {
            _isOffline.value = true
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            // In restricted test environments without full ConnectivityService, keep initial check safe.
        }
    }

    fun unregister() {
        try {
            connectivityManager?.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
            // Safe ignore if already unregistered.
        }
    }

    private fun hasValidatedInternet(): Boolean {
        val cm = connectivityManager ?: return true
        return try {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) {
            true
        }
    }
}
