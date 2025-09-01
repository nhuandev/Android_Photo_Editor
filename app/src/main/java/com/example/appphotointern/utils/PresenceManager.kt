package com.example.appphotointern.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.example.appphotointern.models.Device
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object PresenceManager {
    private const val DB_REALTIME_DEVICE_STATUS = "device_status"

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun setUserOnline(context: Context) {
        val deviceId = getDeviceId(context)
        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("$DB_REALTIME_DEVICE_STATUS/$deviceId")

        val onlineStatus = Device(
            deviceStatus = true,
            deviceTime = System.currentTimeMillis().toString()
        )
        userStatusRef.setValue(onlineStatus)
        userStatusRef.onDisconnect().setValue(
            Device(
                deviceStatus = false,
                deviceTime = System.currentTimeMillis().toString()
            )
        )
    }

    fun setUserOffline(context: Context) {
        val deviceId = getDeviceId(context)
        val userStatusRef =
            FirebaseDatabase.getInstance().getReference("$DB_REALTIME_DEVICE_STATUS/$deviceId")

        val offlineStatus = Device(
            deviceStatus = false,
            deviceTime = System.currentTimeMillis().toString()
        )
        userStatusRef.setValue(offlineStatus)
    }

    fun listenOnlineUsers(onResult: (List<Device>) -> Unit) {
        val statusRef = FirebaseDatabase.getInstance().getReference(DB_REALTIME_DEVICE_STATUS)
        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val onlineUsers = snapshot.children.mapNotNull { child ->
                    val device = child.getValue(Device::class.java)
                    if (device?.deviceStatus == true) device else null
                }
                onResult(onlineUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}
