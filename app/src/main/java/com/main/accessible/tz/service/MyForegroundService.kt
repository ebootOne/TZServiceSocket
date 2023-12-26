package com.main.accessible.tz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.main.accessible.tz.R
import com.main.accessible.tz.data.constants.Constans

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        regReceiver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        startForeground(1, notification)

        // TODO: 实现服务的工作逻辑

        return START_NOT_STICKY
    }

    private fun createNotificationChannel(isRun:Boolean) {
        // 创建通知渠道的代码，和上面相同
        val content: String = getServiceStatusMessage({"群投注正在运行中"},{"群投注已关闭开"},isRun)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
        startForeground(1, notification)
    }


    fun getServiceStatusMessage(strOff:()->String,strOn:()->String,boolean: Boolean):String{
        when (boolean) {
            true -> { return  strOff()}
            false -> { return  strOn()}
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun regReceiver(){
        val intentFilter = IntentFilter(Constans.MAIN_FILTER)
        intentFilter.addAction(Constans.MAIN_BROADCAST)
        registerReceiver(broadcast,intentFilter)
    }


    private val broadcast = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            if(intent?.action == Constans.MAIN_BROADCAST){
                val isOnLine = intent.getBooleanExtra("is_on_line",false)
                createNotificationChannel(isOnLine)
            }
        }

    }

}