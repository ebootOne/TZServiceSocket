package com.main.accessible.tz.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}


inline fun <reified T> fromJson(json:String) : T{
    val type = object : TypeToken<T>(){}.type
    return Gson().fromJson(json,type)
}