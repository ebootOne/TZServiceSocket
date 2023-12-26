package com.main.accessible.tz.service

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException

class RetrofitUrlManager private constructor() {
    val TAG: String = "RetrofitUrlManager"
    companion object {
        val instance: RetrofitUrlManager by lazy { RetrofitUrlManager() }
    }
    private val mInterceptor = Interceptor { chain ->
        val UTF8 = Charset.forName("UTF-8")
        val request = chain.request()
        val requestBody = request.body
        var body: String? = null
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            body = buffer.readString(charset!!)
        }

        Log.i(TAG,
            String.format(
                "\n发送请求\nmethod：%s\nurl：%s\nheaders: %sbody：%s",
                request.method, request.url, request.headers, body
            )
        )
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            // 处理其他异常
            e.message?.let {
                Log.i("Http Error: %s", it)
            }
            throw e
        }

        response
    }
    fun with(builder: OkHttpClient.Builder):OkHttpClient.Builder{
        try {
             builder.addInterceptor(mInterceptor)
            val httpInterceptor = HttpLoggingInterceptor()
            httpInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.retryOnConnectionFailure(false)
        }catch (e:NullPointerException){
            e.printStackTrace()
        }
        return builder
    }
}