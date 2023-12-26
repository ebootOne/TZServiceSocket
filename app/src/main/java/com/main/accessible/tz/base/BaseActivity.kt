package com.main.accessible.tz.base

import android.os.Bundle
import androidx.lifecycle.Observer
import com.main.accessible.tz.ext.dismissLoadingExt
import com.main.accessible.tz.ext.showLoadingExt
import com.main.accessible.tz.viewmodel.BaseViewModel

abstract class BaseActivity<VM: BaseViewModel>() : SimpleActivity<VM>() {
    private lateinit var dataBindingConfig: DataBindingConfig
    override fun getDataBinding(): DataBindingConfig {
        return dataBindingConfig
    }
    abstract fun initContentView(): Int
    abstract fun initVariableId(): Int
    override fun initView(savedInstanceState: Bundle?) {
        dataBindingConfig = DataBindingConfig(initContentView(), initVariableId())
        initViewModel()
    }

    fun addBindingParam(variableId: Int,
                        obj: Any
    ){
        dataBindingConfig = dataBindingConfig.putBindingParam(variableId, obj)
    }

    override fun createObserver() {
        registerUiChange()
    }

    open fun initViewModel(){

    }

    override fun showLoading(message: String) {
        showLoadingExt(message)
    }

    override fun dismissLoading() {
        dismissLoadingExt()
    }

    /**
     * 注册UI 事件
     */
    private fun registerUiChange() {
        //显示弹窗
        mViewModel.loadingChange.showDialog.observeInActivity(this, Observer {
            //it?.let { it1 -> showLoading(it1) }
            showLoading();
        })
        //关闭弹窗
        mViewModel.loadingChange.dismissDialog.observeInActivity(this, Observer {
            dismissLoading()
        })
    }


}