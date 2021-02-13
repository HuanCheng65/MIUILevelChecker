package com.huanchengfly.miui.checker.utils

import android.content.Context
import android.content.pm.PackageManager

object VersionUtil {
    fun getVersionCode(context: Context): Int {
        var versionCode = 0
        try {
            versionCode =
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }

    fun getVersionName(context: Context): String {
        var verName = ""
        try {
            verName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return verName
    }
}