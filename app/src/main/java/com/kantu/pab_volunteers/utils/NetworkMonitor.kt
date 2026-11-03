package com.kantu.pab_volunteers.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import android.net.Network as AndroidNetwork

/**
 * Follows the phone's connection as it changes, so a screen can say it is offline before the
 * person taps Join or Save, rather than only after a save fails. [Network.isOnline] is the
 * one-off check the screens still make just before saving.
 */
object NetworkMonitor {

    /** True while there is a connection that can reach the internet. Starts with the current state. */
    fun onlineStatus(context: Context): Flow<Boolean> = callbackFlow {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(ConnectivityManager::class.java)
        if (manager == null) {
            trySend(true)
            awaitClose()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: AndroidNetwork, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }

            override fun onLost(network: AndroidNetwork) {
                trySend(false)
            }
        }

        trySend(Network.isOnline(appContext))
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
