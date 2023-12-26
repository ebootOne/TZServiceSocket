package com.main.accessible.tz.app

import android.app.Application
import android.util.Log
import com.main.accessible.tz.network.apiService
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MyApp : Application() {
companion object{//静态成员方法
    lateinit var instance : MyApp //lateinit 关键字 “稍后初始化” 告诉编译器稍后初始化 但是在第一次使用之前必须初始化
        private set//可见性修饰符 限制了只可以本类设置 其它外部类是不可以设置的 是个只读属性
}


    override fun onCreate() {
        super.onCreate()
        instance = this
        val strategy = UserStrategy(this);
        strategy.setCrashHandleCallback(CrashListenern())
        CrashReport.initCrashReport(this, "155cdde237", true,strategy);
        //LeakCanary.config = LeakCanary.config.copy(dumpHeap = BuildConfig.DEBUG)
    }

    inner class CrashListenern : CrashReport.CrashHandleCallback(){
        override fun onCrashHandleStart(
            crashType: Int,
            errorType: String?,
            errorMessage: String?,
            errorStack: String?
        ): MutableMap<String, String> {
            // 执行异步操作
            CoroutineScope(Dispatchers.IO).launch {
                val name:String = CrashReport.getUserId()
                Log.i("onCrashHandleStart","onCrashHandleStart----$errorMessage--$name")
                if (errorMessage != null) {
                    apiService.appException(name,errorMessage)
                }
            }
            return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack)
        }
    }

    fun getMyApplication(): MyApp? {
        return instance
    }
}