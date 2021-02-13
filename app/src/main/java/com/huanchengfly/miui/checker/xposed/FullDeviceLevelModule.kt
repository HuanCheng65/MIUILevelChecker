package com.huanchengfly.miui.checker.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class FullDeviceLevelModule : IXposedHookLoadPackage {
    override fun handleLoadPackage(packageParam: XC_LoadPackage.LoadPackageParam) {
        if (SELF_PACKAGE_NAME == packageParam.packageName) {
            val clazz =
                XposedHelpers.findClassIfExists(
                    "com.miui.fuck.utils.XposedUtil",
                    packageParam.classLoader
                ) ?: return
            XposedHelpers.findAndHookMethod(
                clazz,
                "isModuleEnabled",
                XC_MethodReplacement.returnConstant(true)
            )
        } else if (packageParam.packageName.contains("miui") || packageParam.packageName.contains("xiaomi")) {
            val clazz =
                XposedHelpers.findClassIfExists(
                    MIUIX_DEVICE_UTILS_CLASS_NAME,
                    packageParam.classLoader
                ) ?: return
            XposedHelpers.findAndHookMethod(
                clazz,
                "getDeviceLevel",
                XC_MethodReplacement.returnConstant(2)
            )
        }
    }

    companion object {
        const val SELF_PACKAGE_NAME = "com.miui.fuck"
        const val MIUIX_DEVICE_UTILS_CLASS_NAME = "miuix.animation.utils.DeviceUtils"
    }
}