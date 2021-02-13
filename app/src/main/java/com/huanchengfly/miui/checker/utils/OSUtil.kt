package com.huanchengfly.miui.checker.utils

import android.os.Build
import java.io.File
import java.util.*

object OSUtil {
    fun isXiaomi(): Boolean {
        return Build.MANUFACTURER.toLowerCase(Locale.ENGLISH) == "xiaomi" ||
                Build.BRAND.toLowerCase(Locale.ENGLISH) == "xiaomi"
    }

    fun isMIUI(): Boolean {
        return File("/system/framework/framework-miui-res.apk").exists() ||
                File("/system/app/miui/miui.apk").exists() ||
                File("/system/app/miuisystem/miuisystem.apk").exists()
    }
}