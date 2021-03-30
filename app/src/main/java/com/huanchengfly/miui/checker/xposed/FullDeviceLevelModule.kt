package com.huanchengfly.miui.checker.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class FullDeviceLevelModule : IXposedHookLoadPackage {
    override fun handleLoadPackage(packageParam: XC_LoadPackage.LoadPackageParam) {
        val pkgName = packageParam.packageName
        if (SELF_PACKAGE_NAME == pkgName) {
            val clazz =
                XposedHelpers.findClassIfExists(
                    "$SELF_PACKAGE_NAME.utils.XposedUtil",
                    packageParam.classLoader
                ) ?: return
            XposedHelpers.findAndHookMethod(
                clazz,
                "isModuleEnabled",
                XC_MethodReplacement.returnConstant(true)
            )
        } else if ("miui" in pkgName || "xiaomi" in pkgName) {
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
        const val SELF_PACKAGE_NAME = "com.huanchengfly.miui.checker"
        const val MIUIX_DEVICE_UTILS_CLASS_NAME = "miuix.animation.utils.DeviceUtils"
    }
}
