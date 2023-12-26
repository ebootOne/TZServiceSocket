package com.main.accessible.tz.network

import com.main.accessible.tz.data.bean.ApiResponse
import com.main.accessible.tz.data.bean.MessageContentInfo
import retrofit2.http.*

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/23
 * 描述　: 网络API
 */
interface ApiService {

    companion object {
        const val SERVER_URL = "https://win.pc28.ai/api/groupbetsys/"
        const val SERVER_URL1 = "https://wanandroid.com/"
    }

    /**
     * 打开设备
     */
    @FormUrlEncoded
    @POST("device_remark.htm")
    suspend fun deviceRemark(@Field("api_key") apiKey: String
                             ,@Field("sign") sign: String
                             ,@Field("time") time: String
                             ,@Field("factory") factory: String
                             ,@Field("number") number: String
                             ,@Field("version") version: String
                             ,@Field("device_id") deviceId: String?
                             ,@Field("remark") remark: String
                             ,@Field("chat_id") chatId:String
        ): ApiResponse<String>


    /**
     * 获取消息
     */
    @FormUrlEncoded
    @POST("get_msg.htm")
    suspend fun getMsg(@Field("device_id") deviceId: String
                       ,@Field("api_key") apiKey: String
                             ,@Field("sign") sign: String
                             ,@Field("time") time: String
    ): ApiResponse<ArrayList<MessageContentInfo>>


    /**
     * 修改消息状态
     */
    @FormUrlEncoded
    @POST("msg_receipt.htm")
    suspend fun msgReceipt(@Field("device_id") deviceId: String
                           ,@Field("ids") ids: String
                           ,@Field("api_key") apiKey: String
                           ,@Field("sign") sign: String
                           ,@Field("time") time: String
                           ,@Field("type") type: Int
    ): ApiResponse<Any>


    /**
     * 提交消息响应
     */
    @FormUrlEncoded
    @POST("submit_res_text.htm")
    suspend fun submitResText(@Field("device_id") deviceId: String
                           ,@Field("res_text") resText: String
                           ,@Field("api_key") apiKey: String
                           ,@Field("sign") sign: String
                           ,@Field("time") time: String
                           ,@Field("msg_id") msgId: String
    ): ApiResponse<Any>

    /**
     * 获取收藏文章数据
     */
    @GET("lg/collect/list/{page}/json")
    suspend fun getCollectData(@Path("page") pageNo: Int): ApiResponse<Any>


    /**
     * app崩溃消息
     */
    @FormUrlEncoded
    @POST("app_exception")
    suspend fun appException(@Field("device_name") name: String
                       ,@Field("err_msg") error: String
    ): ApiResponse<String>

}