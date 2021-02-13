package com.huanchengfly.miui.checker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.view.LayoutInflater
import android.widget.Toast
import androidx.viewbinding.ViewBinding


inline fun <reified Binding : ViewBinding> Activity.bindView(): Binding {
    val clazz = Binding::class.java
    val inflateMethod = clazz.getDeclaredMethod("inflate", LayoutInflater::class.java)
    val binding: Binding = inflateMethod.invoke(null, layoutInflater) as Binding
    setContentView(binding.root)
    return binding
}

fun Context.toastShort(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.toastShort(resId: Int, vararg args: Any) {
    Toast.makeText(this, getString(resId, *args), Toast.LENGTH_SHORT).show()
}

inline fun <reified T : Activity> Context.goToActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Context.goToActivity(pre: Intent.() -> Unit) {
    startActivity(Intent(this, T::class.java).apply(pre))
}

fun Context.startApp(packageName: String) {
    startActivity(packageManager.getLaunchIntentForPackage(packageName))
}

fun Context.startURL(uri: Uri) {
    startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
        putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
    })
}

fun Context.startURL(url: String) {
    startURL(Uri.parse(url))
}