package com.huanchengfly.miui.checker

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.huanchengfly.miui.checker.databinding.ActivityMainBinding
import com.huanchengfly.miui.checker.databinding.DialogAboutBinding
import com.huanchengfly.miui.checker.utils.DeviceUtil
import com.huanchengfly.miui.checker.utils.OSUtil
import com.huanchengfly.miui.checker.utils.VersionUtil
import com.huanchengfly.miui.checker.utils.XposedUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView()
        setSupportActionBar(binding.toolbar)
        if (OSUtil.isMIUI()) {
            load()
        } else {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_dialog_not_miui)
                    .setMessage(R.string.text_dialog_not_miui)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
        }
    }

    private fun load() {
        binding.contentContainer.visibility = View.VISIBLE
        var manufacturer = Character.toUpperCase(Build.MANUFACTURER[0]).toString() + Build.MANUFACTURER.substring(1)
        if (Build.BRAND != Build.MANUFACTURER) {
            manufacturer += " " + Character.toUpperCase(Build.BRAND[0]) + Build.BRAND.substring(1)
        }
        manufacturer += " " + Build.MODEL
        binding.phoneInfo.text = manufacturer
        binding.socInfo.text = DeviceUtil.getHardwareInfo()
        binding.ramInfo.text = getString(R.string.text_ram_info, DeviceUtil.getTotalRam())
        when (DeviceUtil.getDeviceLevel()) {
            DeviceUtil.DEVICE_HIGH_END -> {
                binding.statusIcon.setImageResource(R.drawable.emoji_grinning_squinting_face)
                binding.statusTitle.setText(R.string.status_title_full)
                binding.statusText.setText(R.string.status_text_full)
                binding.deviceLevelInfo.setText(R.string.level_text_high_end)
            }
            DeviceUtil.DEVICE_MIDDLE -> {
                binding.statusIcon.setImageResource(R.drawable.emoji_expressionless_face)
                binding.statusTitle.setText(R.string.status_title_basic)
                binding.statusText.setText(R.string.status_text_basic)
                binding.deviceLevelInfo.setText(R.string.level_text_middle)
            }
            DeviceUtil.DEVICE_PRIMARY -> {
                binding.statusIcon.setImageResource(R.drawable.emoji_confused_face)
                binding.statusTitle.setText(R.string.status_title_none)
                binding.statusText.setText(R.string.status_text_none)
                binding.deviceLevelInfo.setText(R.string.level_text_primary)
            }
            DeviceUtil.DEVICE_UNKNOWN -> {
                binding.statusIcon.setImageResource(R.drawable.emoji_thinking_face)
                binding.statusTitle.setText(R.string.status_title_unknown)
                binding.statusText.setText(R.string.status_text_unknown)
                binding.deviceLevelInfo.setText(R.string.level_text_unknown)
            }
        }
        if (DeviceUtil.isMiuiLite()) {
            binding.miuiLiteInfo.setText(R.string.miui_lite_yes)
        } else {
            binding.miuiLiteInfo.setText(R.string.miui_lite_no)
        }
        binding.miuiLiteHelpBtn.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_dialog_whats_this)
                    .setMessage(R.string.text_dialog_miui_lite_help)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
        }
        if (XposedUtil.isModuleEnabled) {
            binding.xposedModuleStatusCard.setCardBackgroundColor(getColor(R.color.green_400))
            binding.xposedModuleStatus.setText(R.string.title_xposed_module_enabled)
            binding.xposedModuleStatusMessage.text = Html.fromHtml(getString(R.string.text_xposed_module_enabled), Html.FROM_HTML_MODE_COMPACT)
            binding.xposedModuleStatusActionBtn.setImageResource(R.drawable.ic_round_settings)
            binding.xposedModuleStatusActionBtn.visibility = View.GONE
        } else if (!XposedUtil.isManagerInstalled(this)) {
            binding.xposedModuleStatusCard.visibility = View.GONE
        } else {
            binding.xposedModuleStatusCard.setCardBackgroundColor(getColor(R.color.red_A400))
            binding.xposedModuleStatus.setText(R.string.title_xposed_module_not_actived)
            binding.xposedModuleStatusMessage.text = Html.fromHtml(getString(R.string.text_xposed_module_not_enabled), Html.FROM_HTML_MODE_COMPACT)
            binding.xposedModuleStatusActionBtn.setImageResource(R.drawable.ic_round_exit_to_app)
            binding.xposedModuleStatusCard.setOnClickListener {
                startApp(XposedUtil.getInstalledManagerPackageName(this)!!)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_about) {
            val dialogViewBinding = DialogAboutBinding.inflate(layoutInflater)
            dialogViewBinding.dialogAboutText.text = getString(R.string.text_dialog_about, VersionUtil.getVersionName(this), VersionUtil.getVersionCode(this))
            dialogViewBinding.btnGithub.setOnClickListener {
                startURL(getString(R.string.link_source))
            }
            AlertDialog.Builder(this)
                    .setView(dialogViewBinding.root)
                    .show()
        }
        return false
    }
}