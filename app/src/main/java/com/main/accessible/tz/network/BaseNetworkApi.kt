package com.main.accessible.tz.network

import com.main.accessible.tz.service.RetrofitUrlManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit

abstract class BaseNetworkApi {
    fun <T> getApi(serviceClass: Class<T> ,baseUsl: String):T{
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUsl)
            .client(okHttpClient)
        return setRetrofitBuilder(retrofitBuilder).build().create(serviceClass)
    }

    abstract fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder

    abstract fun setRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder
    private val okHttpClient : OkHttpClient
        get() {
            var builder = RetrofitUrlManager.instance.with(OkHttpClient.Builder())
            builder =  setHttpClientBuilder(builder)
            return builder.build()
        }

}