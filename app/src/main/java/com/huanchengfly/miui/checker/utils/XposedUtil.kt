package com.huanchengfly.miui.checker.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


object XposedUtil {
    private val MANAGER_PACKAGE_NAME_LIST = listOf(
        "org.meowcat.edxposed.manager"
    )

    @JvmStatic
    val isModuleEnabled: Boolean
        get() = false

    fun isManagerInstalled(context: Context): Boolean {
        return getInstalledManagerPackageName(context) != null
    }

    fun getInstalledManagerPackageName(context: Context): String? {
        return MANAGER_PACKAGE_NAME_LIST.firstOrNull {
            checkAppInstalled(context, it)
        }
    }

    fun checkAppInstalled(context: Context, pkgName: String): Boolean {
        if (pkgName.isEmpty()) {
            return false
        }
        var packageInfo: PackageInfo?
        try {
            packageInfo = context.packageManager.getPackageInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            packageInfo = null
            e.printStackTrace()
        }
        return packageInfo != null
    }
}