package com.example.appphotointern.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData

class NetworkReceiver(context: Context) : LiveData<Boolean>() {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            checkNetworkStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }

        override fun onCapabilitiesChanged(network: Network, networkCap: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCap)
            val hasInternet = networkCap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            postValue(hasInternet)
        }
    }

    override fun onActive() {
        super.onActive()
        checkNetworkStatus()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork == null) {
            postValue(false)
            return
        }
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        postValue(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
    }

    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}