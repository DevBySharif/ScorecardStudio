package com.example.scorecardstudio

import java.io.File

object RootDetector {
    private val knownRootPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        "/system/app/Magisk.apk",
        "/system/app/MagiskManager.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/system/xbin/busybox",
        "/system/bin/busybox"
    )

    private val knownRootCommands = arrayOf(
        "su -c id",
        "su -c echo test"
    )

    fun isDeviceRooted(): Boolean {
        for (path in knownRootPaths) {
            if (File(path).exists()) return true
        }
        return false
    }
}
