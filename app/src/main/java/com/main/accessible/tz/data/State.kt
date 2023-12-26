package com.main.accessible.tz.data

import androidx.databinding.ObservableField

class State<T>
/**
 * 务必根据泛型提供初值，以彻底规避 Null 安全问题
 * Be sure to provide initial values based on generics to completely avoid null security issues
 *
 * @param value initial value
 */ @JvmOverloads constructor(value: T, private val mIsDebouncing: Boolean = false) :
    ObservableField<T>(value) {
    /**
     * 如果 isDebouncing = true 开启防抖，那么只在值变化时通知刷新。默认总是通知刷新
     * 如果注册 DiffCallback，在值变化时可做一些操作
     * @param value set value
     * @param callback diff callback
     */
    operator fun set(value: T, callback: DiffCallback<T>?) {
        val isUnChanged = get() === value
        super.set(value)
        if (!mIsDebouncing && isUnChanged) notifyChange()
        if (!isUnChanged && callback != null) callback.onValueChanged(value)

    }

    override fun set(value: T) {
        set(value, null)
    }

    fun

    interface DiffCallback<T> {
        fun onValueChanged(value: T)
    }
}