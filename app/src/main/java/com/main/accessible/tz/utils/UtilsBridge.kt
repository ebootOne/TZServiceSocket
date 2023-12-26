package com.main.accessible.tz.utils

import android.content.Context

object UtilsBridge {
    /**
     * SPUtils
     */
    fun getSpUtils4Utils(context: Context): SPUtils? {
        return SPUtils.getInstance("Utils")
    }
}