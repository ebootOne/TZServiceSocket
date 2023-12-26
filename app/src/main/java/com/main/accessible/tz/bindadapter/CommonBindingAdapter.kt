package com.main.accessible.tz.bindadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.main.accessible.tz.R

object CommonBindingAdapter {
    @BindingAdapter("android:textColor")
    @JvmStatic
    fun setTextColor(textView: TextView, isOnline: Boolean) {
        val color = if (isOnline) {
            // 在线状态的颜色（例如绿色）
            textView.context.getColor(R.color.green)
        } else {
            // 离线状态的颜色（例如红色）
            textView.context.getColor(R.color.colorAccent)
        }
        textView.setTextColor(color)
    }
}