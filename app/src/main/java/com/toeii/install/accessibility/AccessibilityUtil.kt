package com.toeii.install.accessibility

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.toeii.install.R
import java.io.File


object AccessibilityUtil {
    private val TAG = AccessibilityUtil::class.java.simpleName

    /**
     * 检查系统设置，并显示设置对话框
     */
    fun checkSetting(cxt: Context, service: Class<*>) {

        if (isSettingOpen(cxt))
            return
        AlertDialog.Builder(cxt)
            .setTitle(R.string.unknow_setting_title)
            .setMessage(R.string.unknow_setting_msg)
            .setPositiveButton(R.string.yes) { dialog, which -> jumpToInstallSetting(cxt) }
            .show()

        if (isSettingOpen(service, cxt))
            return
        AlertDialog.Builder(cxt)
            .setTitle(R.string.aby_setting_title)
            .setMessage(R.string.aby_setting_msg)
            .setPositiveButton(R.string.yes) { dialog, which -> jumpToSetting(cxt) }
            .show()
    }

    /**
     * 检查系统设置：是否开启辅助服务
     */
    private fun isSettingOpen(service: Class<*>, cxt: Context): Boolean {
        try {
            val enable = Settings.Secure.getInt(cxt.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
            if (enable != 1)
                return false
            val services =
                Settings.Secure.getString(cxt.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (!TextUtils.isEmpty(services)) {
                val split = TextUtils.SimpleStringSplitter(':')
                split.setString(services)
                while (split.hasNext()) {
                    if (split.next().equals(cxt.packageName + "/" + service.name, ignoreCase = true))
                        return true
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "isSettingOpen: " + e.message)
        }

        return false
    }

    /**
     * 跳转到系统设置：开启辅助服务
     */
    fun jumpToSetting(cxt: Context) {
        try {
            cxt.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Throwable) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                cxt.startActivity(intent)
            } catch (e2: Throwable) {
                Log.e(TAG, "jumpToSetting: " + e2.message)
            }

        }

    }

    /**
     * 唤醒点亮和解锁屏幕(60s)
     */
    fun wakeUpScreen(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm != null && pm.isScreenOn) {
                val wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "wakeUpScreen"
                )
                wl.acquire(60000)
            }

            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km != null && km.inKeyguardRestrictedInputMode()) {
                val kl = km.newKeyguardLock("unLock")
                kl.disableKeyguard()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "wakeUpScreen: " + e.message)
        }

    }


    /**
     * 检查系统设置：是否允许安装来自未知来源的应用
     */
    private fun isSettingOpen(cxt: Context): Boolean {
        val canInstall: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            canInstall = cxt.packageManager.canRequestPackageInstalls()
        else
            canInstall = Settings.Secure.getInt(cxt.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        return canInstall
    }

    /**
     * 跳转到系统设置：允许安装来自未知来源的应用
     */
    private fun jumpToInstallSetting(cxt: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            cxt.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + cxt.packageName)
                )
            )
        else
            cxt.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    /**
     * 安装APK
     * @param apkFile APK文件的本地路径
     */
    fun install(cxt: Context, apkFile: File) {
        AccessibilityUtil.wakeUpScreen(cxt)
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(cxt, cxt.packageName + ".fileProvider", apkFile)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(apkFile)
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            cxt.startActivity(intent)
        } catch (e: Throwable) {
            Toast.makeText(cxt, "安装失败：" + e.message, Toast.LENGTH_LONG).show()
        }

    }

}