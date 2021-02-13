package com.huanchengfly.miui.checker.utils

import java.io.File

object OSUtil {
    fun isMIUI(): Boolean {
        return File("/system/framework/framework-miui-res.apk").exists() ||
                File("/system/app/miui/miui.apk").exists() ||
                File("/system/app/miuisystem/miuisystem.apk").exists()
    }
}