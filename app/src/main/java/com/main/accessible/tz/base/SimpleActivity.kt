package com.main.accessible.tz.base

import android.os.Bundle
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.main.accessible.tz.viewmodel.BaseViewModel
import java.lang.reflect.ParameterizedType

abstract class SimpleActivity<VM : BaseViewModel> : AppCompatActivity() {
    lateinit var mDatabind : ViewDataBinding
    protected lateinit var mViewModel: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = createViewModel()
        initView(savedInstanceState)
        val dataBindingConfig : DataBindingConfig = getDataBinding()
        val binding : ViewDataBinding?  = DataBindingUtil.setContentView(this, dataBindingConfig.layout);
        binding!!.lifecycleOwner = this
        binding.setVariable(
            dataBindingConfig.vmVariableId,
            mViewModel
        )
        val bindingParams: SparseArray<Any>? = dataBindingConfig.bindingParams
        if (bindingParams != null) {
            for (i in 0 until bindingParams.size()) {
                binding.setVariable(bindingParams.keyAt(i), bindingParams.valueAt(i))
            }
        }
        mDatabind = binding;
        createObserver()
    }



    override fun onDestroy() {
        super.onDestroy()
        mDatabind.unbind()
    }
    open fun initParam(){

    }
    abstract fun showLoading(message: String = "请求网络中...")

    abstract fun dismissLoading()
    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()
    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun getDataBinding(): DataBindingConfig
    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <VM> getVmClazz(obj: Any): VM {
        return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as VM
    }
}