package com.main.accessible.tz

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.main.accessible.tz.base.BaseActivity
import com.main.accessible.tz.data.State
import com.main.accessible.tz.data.bean.ApiResponse
import com.main.accessible.tz.data.constants.Constans
import com.main.accessible.tz.ext.request
import com.main.accessible.tz.network.apiService
import com.main.accessible.tz.utils.DeviceUtils
import com.main.accessible.tz.utils.SPUtils
import com.main.accessible.tz.viewmodel.BaseViewModel
import com.main.accessible.utils.HttpUtils
import com.tencent.bugly.crashreport.CrashReport

class MainActivity : BaseActivity<MainActivity.MainActivityStates>() {
    var isOpenService = false  //是否打开服务
    var serviceOn = false;//服务是否运行
    override fun initContentView(): Int {
        return R.layout.activity_main
    }

    override fun initVariableId(): Int {
        return BR.vm
    }

    override fun initViewModel() {
        super.initViewModel()
        addBindingParam(BR.clickProxy, ProxyClick())
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(Constans.INTENT_FILTER)
        intent.action = Constans.MAIN_UPDATE_STATE
        sendBroadcast(intent)
        Log.i("message_----onResume:","main$isOpenService")
    }
    override fun createObserver() {
        super.createObserver()
        regReceiver()
        mViewModel.load.observe(this, Observer {resultState ->
            //CrashReport.testJavaCrash()
            Log.i("message_----onResume:","result:$resultState")
            isOpenService = resultState.isSucces()
            if(resultState.isSucces()){
                onSetButtonClicked()
            }else{
                Toast.makeText(this,"${resultState.msg}",Toast.LENGTH_SHORT).show()
            }

         })

        mViewModel.isOnLine.observe(this, Observer { isConcent ->
            //CrashReport.testJavaCrash()
            if(isConcent){
                Toast.makeText(this,"websocket 连接成功",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"群投注已关闭",Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun openAccessibilitySettings(){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        requestAccessibilityPermissionLauncher.launch(intent)
    }

    private val requestAccessibilityPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ _ ->
        if(!hasAccessibilityServiceEnabled()){
            Toast.makeText(this,"授予无障碍权限失败",Toast.LENGTH_SHORT).show()
        }else{
            isOpenService = true
            setOffService()
            Toast.makeText(this,"权限授权成功",Toast.LENGTH_SHORT).show()
        }
    }


    private val myPackageName: String? = "com.main.accessible.tz"
    private fun hasAccessibilityServiceEnabled(): Boolean{
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityManager = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityManager){
            val packageName = info.resolveInfo.serviceInfo.packageName
            if(packageName == myPackageName){
                return true;
            }
        }
        return false;
    }

    inner class ProxyClick{
        fun onStart() {

            if (mViewModel.editText.get().isNullOrEmpty() || mViewModel.userId.get()
                    .isNullOrEmpty()
            ) {
                Toast.makeText(this@MainActivity, "输入框不能为空", Toast.LENGTH_SHORT).show()
                return
            }
            if (!serviceOn) {//
                CrashReport.setUserId(this@MainActivity, mViewModel.editText.get().toString())
                mViewModel.deviceRemark()
            }else{
                isOpenService = false//关闭
                setOffService()
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        //super.onBackPressed()

    }
    fun onSetButtonClicked(){
        if (!hasAccessibilityServiceEnabled()) { //没有打开权限
            openAccessibilitySettings()
        } else {
            setOffService()
        }
    }



    fun setOffService(){//关闭开关服务
        val intent = Intent(Constans.INTENT_FILTER)
        intent.action = Constans.OFF_OR_ON
        intent.putExtra("off", isOpenService)
        intent.putExtra(Constans.REMARK_EDIT, mViewModel.editText.get())
        sendBroadcast(intent)
    }

    class MainActivityStates : BaseViewModel(){

        val editText: State<String> = State(SPUtils.getInstance()?.getString(Constans.REMARK_EDIT)!!)
        val userId : State<String> = State(SPUtils.getInstance()?.getString(Constans.USER_ID_EDIT)!!)
        var load = MutableLiveData<ApiResponse<String>>()
        val isOnLine = MutableLiveData<Boolean>()
        fun saveEditRemark(){
            SPUtils.getInstance()?.put(Constans.REMARK_EDIT,editText.get().toString())
        }

        fun saveEditUserId(){
            SPUtils.getInstance()?.put(Constans.USER_ID_EDIT,userId.get().toString())
        }

        fun deviceRemark(){
            request({apiService.deviceRemark(
                "qk",
                HttpUtils.sha256((System.currentTimeMillis() / 1000).toString()),
                (System.currentTimeMillis() / 1000).toString(),
                DeviceUtils.getDeviceBrand(),
                DeviceUtils.getModel(),
                DeviceUtils.getAppVersionName(),
                DeviceUtils.getUniqueDeviceId(),
                editText.get().toString(),
                userId.get().toString()
            ) },{
                load.postValue(it)
                saveEditRemark()
                saveEditUserId()
                },{
                val api = ApiResponse(0,it.errorMsg,"")
                load.postValue(api)
            },true)
        }
    }

    private val broadcast = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            Log.i("message_----onReceive:","main 收到服务消息")
            intent?.action?.let {
                val isOnLine = intent.getBooleanExtra("is_on_line",false)
                if(it == Constans.MAIN_BROADCAST ){
                    Log.i("message_----onReceive:","main MAIN_BROADCAST$isOnLine --serviceOn$serviceOn")
                    serviceOn = isOnLine
                    mViewModel.isOnLine.postValue(isOnLine)
                }
                if(it == Constans.MAIN_QUERY ){
                    Log.i("message_----onReceive:","main MAIN_QUERY$isOnLine --serviceOn$serviceOn")
                    if(serviceOn != isOnLine) {
                        serviceOn = isOnLine
                        mViewModel.isOnLine.postValue(isOnLine)
                    }
                }

            }

        }

    }

    private fun regReceiver(){
        val intentFilter = IntentFilter(Constans.MAIN_FILTER)
        intentFilter.addAction(Constans.MAIN_BROADCAST)
        intentFilter.addAction(Constans.MAIN_QUERY)
        registerReceiver(broadcast,intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }
}