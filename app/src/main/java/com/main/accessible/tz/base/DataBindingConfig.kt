package com.main.accessible.tz.base

import android.util.SparseArray

data class DataBindingConfig(val layout: Int,val vmVariableId: Int,val bindingParams: SparseArray<Any>? = null){
    fun putBindingParam(key: Int, value: Any): DataBindingConfig {
        val updatedParams = bindingParams?.clone() as SparseArray<Any>? ?: SparseArray()
        updatedParams.put(key, value)
        return this.copy(bindingParams = updatedParams)
    }
}

