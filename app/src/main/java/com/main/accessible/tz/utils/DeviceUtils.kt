package com.main.accessible.tz.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.main.accessible.tz.app.MyApp
import java.util.UUID


object DeviceUtils {

    fun getDeviceBrand(): String{
        return Build.BRAND
    }

    fun getUniqueDeviceId(): String {
        return getUniqueDeviceId(MyApp.instance,"",false)
    }

    /**
     * 设备型号
     */
    fun getModel(): String {
        var model = Build.MODEL
        model = model?.trim { it <= ' ' }?.replace("\\s*".toRegex(), "") ?: ""
        return model
    }

    /**
     * Return version code of device's system.
     *
     * @return version code of device's system
     */
    fun getSDKVersionCode(): Int {
        return Build.VERSION.SDK_INT
    }

    fun getAppVersionName(): String {
        return try {
            val packageInfo = MyApp.instance.packageManager.getPackageInfo(MyApp.instance.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getUniqueDeviceId(context: Context, prefix: String, useCache: Boolean): String {
        if (!useCache) {
            return  getUniqueDeviceIdReal(context, prefix)
        }
        if (udid == null) {
            synchronized(DeviceUtils::class.java) {
                if (udid == null) {
                    val id: String? =
                        UtilsBridge.getSpUtils4Utils(context)?.getString(DeviceUtils.KEY_UDID, null)
                    if (id != null) {
                        udid = id
                        return udid as String
                    }
                    return  getUniqueDeviceIdReal(context, prefix)
                }
            }
        }
        return udid!!
    }

    private fun getUniqueDeviceIdReal(context: Context, prefix: String): String {
        try {
            val androidId: String = getAndroidID(context)
            if (!TextUtils.isEmpty(androidId)) {
                return saveUdid(context, prefix + 2, androidId)
            }
        } catch (ignore: Exception) { /**/
        }
        return saveUdid(context, prefix + 9, "")
    }

    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String {
        val id = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return if ("9774d56d682e549c" == id) "" else id ?: ""
    }


    @Volatile
    private var udid: String? = ""
    private const val KEY_UDID = "KEY_UDID"

    private fun saveUdid(context: Context, prefix: String, id: String): String {
        udid = getUdid(prefix, id)
        UtilsBridge.getSpUtils4Utils(context)?.put(KEY_UDID, udid!!)
        return udid as String
    }

    private fun getUdid(prefix: String, id: String): String {
        return if (id == "") {
            prefix + UUID.randomUUID().toString().replace("-", "")
        } else prefix + UUID.nameUUIDFromBytes(id.toByteArray()).toString().replace("-", "")
    }

}