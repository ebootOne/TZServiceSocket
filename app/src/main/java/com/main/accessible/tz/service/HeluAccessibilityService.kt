package com.main.accessible.tz.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.main.accessible.tz.data.bean.A
import com.main.accessible.tz.data.bean.ApiResponse
import com.main.accessible.tz.data.bean.MessageContentInfo
import com.main.accessible.tz.data.constants.Constans
import com.main.accessible.tz.network.apiService
import com.main.accessible.tz.utils.AccessibilityUtils
import com.main.accessible.tz.utils.DeviceUtils
import com.main.accessible.tz.utils.fromJson
import com.main.accessible.utils.HttpUtils
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


class HeluAccessibilityService : AccessibilityService() {
    private val channelId = "my_service_channel"
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        /*if(event.packageName?.toString() != "org.telegram.messenger.web"){

        }*/
        if (event != null) {
            try {
                findTextViews(event)
            }catch (e:Exception){
                submitTxt = null
            }

        }
    }

    private var isBusy = false
    private fun findTextViews(event: AccessibilityEvent) {
        if (event?.packageName?.toString() == "org.telegram.messenger.web" && submitTxt != null) {
            // 这里是只处理来自Telegram的事件的逻辑
            if (event.className == "androidx.recyclerview.widget.RecyclerView") {
                val recyclerViewNode = event.source
                try {
                    if (recyclerViewNode != null) {
                        if(recyclerViewNode.childCount > 0){
                            val child = recyclerViewNode.getChild(recyclerViewNode.childCount-1)
                            /*for(i in 0 until  recyclerViewNode.childCount){
                                val child = recyclerViewNode.getChild(i)
                                if (child?.className == "android.view.ViewGroup") {
                                    val messageText = child.text.toString()
                                    Log.i("", "Received event 4444: $i  ----- $messageText")
                                }
                            }*/
                            try {
                                if (child?.className == "android.view.ViewGroup") {
                                    // 现在你可以使用messageText
                                    val messageText = child.text?.toString()
                                    if(!isBusy && submitTxt != null && messageText?.contains("Received at")!!){
                                        isBusy = true
                                        Log.i("", "Received event 333: $  ----- $messageText")
                                        if(messageText.contains(submitTxt!!)){
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Log.i("", "Received event 333: 协程中$  ----- ")
                                                val data : ApiResponse<Any>
                                                try {
                                                    data  = withContext(Dispatchers.IO){
                                                        try {
                                                            apiService.submitResText(
                                                                DeviceUtils.getUniqueDeviceId(),
                                                                messageText,
                                                                "qk",
                                                                HttpUtils.sha256((System.currentTimeMillis() / 1000).toString()),
                                                                (System.currentTimeMillis() / 1000).toString(),submitId)
                                                        }catch (e:Exception){
                                                            e.printStackTrace()
                                                            ApiResponse<Any>(-1,"未知错误",Any())
                                                        }

                                                    }
                                                }finally {
                                                    isBusy = false
                                                    submitTxt = null
                                                }
                                                // 在 IO 线程中执行并返回结果
                                                data?.let {
                                                    Log.i("", "Received event 333: 协程中$data  ----- ")
                                                    if(data.isSucces()){
                                                        submitTxt = null
                                                        submitId = ""
                                                    }
                                                }
                                                }

                                        }else{
                                            isBusy = false
                                        }
                                    }

                                }
                            }finally {
                                child?.recycle()  // 回收child节点
                            }

                        }
                    }
                } finally {
                    recyclerViewNode?.recycle()  // 回收recyclerViewNode节点
                }
            }
        }
    }
    lateinit var remark : String
    var serviceOn: Boolean = false//是否还在连接之中
    var sendId : String = ""
    val mutex = Mutex()
    private val taskChannel = Channel<() -> Unit>()
    val broadcast = object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action == Constans.MAIN_UPDATE_STATE) {
                Log.i("message_----收到消息main:","main$serviceOn")
                sendMainService(Constans.MAIN_QUERY)//赶紧返回状态
            }
            if (intent?.action == Constans.OFF_OR_ON) {
                remark = intent.getStringExtra(Constans.REMARK_EDIT).toString()
                serviceOn= intent.getBooleanExtra("off", false)
                Log.i("message_----收到消息:","关闭按钮$serviceOn")
                serviceOn?.let {
                    if(!serviceOn){
                        job?.cancel()
                        job = null
                        webSocket?.close(1000,"close")//关闭接受
                    }else{
                        startPolling()
                    }
                }
            }
            if(intent?.action == Constans.ACTION_AIUI_UPDATE){
                var input = intent.getStringExtra("input")
                var type = intent.getIntExtra("type",0)
                Log.i("message_----自动下注:","$input"+"slides$sendId-----$type")
                if (input != null && type != null && sendId != null) {
                    val rootNode = rootInActiveWindow
                    if(rootNode == null && rootNode?.packageName.toString() == null){
                        onReportError("获取无障碍对象为空")
                    }
                    if(rootNode?.packageName?.toString() != "org.telegram.messenger.web"){
                        //onReportError("用户中途离开小飞机界面，导致下注失败")
                        return
                    }

                    val a:A = runBlocking {
                        sendId = intent.getStringExtra("id").toString()
                        val isRepay = intent.getBooleanExtra("isRepay",false)
                        var isSend : Boolean
                        try {
                             isSend =  mutex.withLock {
                                AccessibilityUtils.fillEditText(input, rootNode)
                                AccessibilityUtils.clickViewByContentDescription("Send",input, rootNode)
                            }
                        }catch (e:Exception){
                            onReportError("下注ID$sendId ,自动投注时发声异常 ${e.message}")
                            isSend = false
                            e.printStackTrace()
                        }
                        Log.i("message_----自动下注:","发送的状态$isSend---$sendId")
                        delay(200)
                        A(isSend,sendId,isRepay)
                    }
                    if (a.isSend) {
                            CoroutineScope(Dispatchers.Main).launch {
                                // 在 IO 线程中执行并返回结果
                                Log.i("message_----update2222:", "开始上报--${a.msgId}")
                                var currentRetry = 0
                                while(currentRetry < 3){
                                    kotlin.runCatching {
                                        onTZReport(a.msgId,type)
                                    }.onSuccess {
                                        if(it.code == 500) {
                                            Log.i("message_----上报:", "上报失败${a.msgId}---${it.code}")
                                            currentRetry++
                                        }else {
                                            Log.i("message_----上报:", "上报成功${a.msgId}")
                                            currentRetry = 4
                                        }
                                    }.onFailure {
                                        it.printStackTrace()
                                        Log.i("message_----上报:", "上报失败${a.msgId}---${it.message}")
                                        currentRetry++
                                        if(currentRetry < 3) {//最多补报三次
                                            onReportError("下注ID${a.msgId} ,上报时发生错误已补报三次 ${it.message}")
                                        }else{
                                            onReportError("下注ID${a.msgId} ,上报时发生错误正在进行补报 ${it.message}")
                                        }
                                    }
                                }
                            }
                    }else{
                        if(!a.isRepay) {//没有补报过需要补报
                            onReportError("下注ID${a.msgId} ,自动下注失败 重新下注")
                            onSendMsg(MessageContentInfo(type, a.msgId, input, 0), true)//重新下注一次
                        }
                    }
                }
            }
        }

    }

    override fun onInterrupt() {
    }
    private var submitTxt: String? = null
    private var submitId: String = ""
    override fun onCreate() {
        super.onCreate()
        Log.i("onCreate----onCreate:","")
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        startService(serviceIntent)
        regReceiver()
        startTaskProcessor()
    }



    private fun startTaskProcessor() {
        CoroutineScope(Dispatchers.Main).launch {
            for (task in taskChannel) {
                task()
            }
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("onCreate----onStartCommand:","")
        // 如果我们被杀死，从这里重新开始
        return START_STICKY
    }

    fun onReportError(content:String){
        Log.i("message_----上报错误:","上报error $content")
        CoroutineScope(Dispatchers.IO).launch {
            val name:String = CrashReport.getUserId()
            try {
                apiService.appException(name,content)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun onTZReport(msgId: String, type: Int):ApiResponse<Any>{
           return apiService.msgReceipt(
                DeviceUtils.getUniqueDeviceId(),
                msgId,
                "qk",
                HttpUtils.sha256((System.currentTimeMillis() / 1000).toString()),
                (System.currentTimeMillis() / 1000).toString(),
                type
            )
    }

    fun startPolling(){
        initWebSocket()
    }

     private var webSocket: WebSocket? = null
        var reConnection: Int = 0
    private fun initWebSocket() {
         val client = OkHttpClient.Builder()
             .writeTimeout(20, TimeUnit.SECONDS)
             .readTimeout(20, TimeUnit.SECONDS)
             .connectTimeout(20, TimeUnit.SECONDS)
             .build();

         val request = Request.Builder().url("ws://34.142.169.160:3456").build()
         webSocket = client.newWebSocket(request, object : WebSocketListener() {
             override fun onOpen(webSocket: WebSocket, response: Response) {
                 Log.i("message_----打开socket:","打开socket$serviceOn")
                 serviceOn = true
                 sendMainService(Constans.MAIN_BROADCAST)
                 startHeartbeat()
                 // 连接已打开
                 val deviceId = DeviceUtils.getUniqueDeviceId()
                 webSocket.send("{\"device_id\":\"$deviceId\"}")
             }

             override fun onMessage(webSocket: WebSocket, text: String) {
                 reConnection = -1
                 Log.i("message_----收到消息:","$text")
                 if (text == "heartbeat") {//服务器发过来的心跳
                     webSocket.send("heartbeat")//马上回补一个
                     return
                 }
                 try {
                     val response : ApiResponse<MessageContentInfo> = fromJson(text)
                     if(response != null) {
                         onSendMsg(response.data, false)
                     }
                 }catch (e:Exception){
                    e.printStackTrace()
                 }

             }

             override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                 Log.i("message_----","接受到消息-fail--onClosed:$code")
                 webSocket.cancel()
                 cancelLink();
                 sendMainService(Constans.MAIN_BROADCAST)
                 // 连接已关闭
             }

             override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                 Log.i("message_----","接受到消息-fail--response:$t")
             }
         })
     }
    /**
     * @isReply 是否需要补报
    * */
    fun onSendMsg(info: MessageContentInfo,isRepay :Boolean) {
        val intent = Intent(Constans.ACTION_AIUI_UPDATE)
        Log.i("", "Received event info: $info.type ")
        intent.putExtra("type",info.type)
        intent.putExtra("input", info.text)
        intent.putExtra("id", info.msg_id)
        intent.putExtra("isRepay", isRepay)
        if(info.reply == 1){
            submitId = info.msg_id
            submitTxt = info.text
        }
        sendOrderedBroadcast(intent,null)
    }

    fun cancelLink(){
        job?.cancel()
        job = null
        serviceOn = false
    }

    var job: Job? = null
    fun startHeartbeat(){
        if(job != null){
            return
        }
        job = GlobalScope.launch{
            while (isActive){
                try {
                    val rootNode = rootInActiveWindow
                    val deviceId = DeviceUtils.getUniqueDeviceId()
                    val isSend :Boolean = webSocket?.send("ping:{\"device_id\":\"$deviceId\",\"cur_page\":\"${rootNode!!.packageName}\"}") == true//发送心跳
                    Log.i("message_----",
                        "发送ping--response:$isSend---$serviceOn--ping----"+rootNode.packageName
                    )
                    //sendMainService()
                    if(!isSend!! && serviceOn){//发送失败
                      startPolling()
                    }
                    delay(20000)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun regReceiver(){
        val intentFilter = IntentFilter(Constans.INTENT_FILTER)
        intentFilter.addAction(Constans.ACTION_AIUI_UPDATE)
        intentFilter.addAction(Constans.OFF_OR_ON)
        intentFilter.addAction(Constans.MAIN_UPDATE_STATE)
        registerReceiver(broadcast,intentFilter)
    }

    fun sendMainService(action : String){
        val intent = Intent(Constans.MAIN_FILTER)
        intent.action = action
        intent.putExtra("is_on_line", serviceOn)
        sendBroadcast(intent)
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        setAccessibilityNodeInfos()
    }

    fun setAccessibilityNodeInfos(){
        val serviceInfo = AccessibilityServiceInfo()
//        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        //        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo.flags = serviceInfo.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS

        serviceInfo.flags =
            serviceInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE

        serviceInfo.flags =
            serviceInfo.flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS

        setServiceInfo(serviceInfo)
    }
}