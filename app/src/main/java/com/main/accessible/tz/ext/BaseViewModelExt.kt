package com.main.accessible.tz.ext

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.main.accessible.tz.base.BaseActivity
import com.main.accessible.tz.data.bean.ApiResponse
import com.main.accessible.tz.network.AppException
import com.main.accessible.tz.network.BaseResponse
import com.main.accessible.tz.network.ExceptionHandle
import com.main.accessible.tz.state.ResultState
import com.main.accessible.tz.state.paresException
import com.main.accessible.tz.state.paresResult
import com.main.accessible.tz.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


fun <T> BaseViewModel.request(
    block: suspend () -> BaseResponse<T>,
    resultState: MutableLiveData<ResultState<T>>,
    isShowDialog: Boolean = false,
    loadingMessage: String = "正在请求..."
): Job {
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) resultState.value = ResultState.onAppLoading(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            resultState.paresResult(it)
        }.onFailure {
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            resultState.paresException(it)
        }
    }
}


fun <T : Any> BaseViewModel.request(
    block: suspend () -> ApiResponse<T>,
    success:(ApiResponse<T>) -> Unit,
    error:(AppException) -> Unit = {},
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中..."
): Job{
    return viewModelScope.launch {
        runCatching {
            if(isShowDialog){
                loadingChange.showDialog.postValue(loadingMessage)
            }
            //请求体
            block()
        }.onSuccess {
            loadingChange.dismissDialog.postValue(false)

            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it) { t -> success(t)
                }
            }.onFailure { e ->
                //打印错误消息
                e.message?.loge()
                //打印错误栈信息
                e.printStackTrace()
                //失败回调
                error(ExceptionHandle.handleException(e))
            }
        }.onFailure {
            //网络请求异常 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            //打印错误消息
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            //失败回调
            error(ExceptionHandle.handleException(it))
        }
    }
}



/**
 * 显示页面状态，这里有个技巧，成功回调在第一个，其后两个带默认值的回调可省
 * @param resultState 接口返回值
 * @param onLoading 加载中
 * @param onSuccess 成功回调
 * @param onError 失败回调
 *
 */
fun <T> BaseActivity<*>.parseState(
    resultState: ResultState<T>,
    onSuccess: (T) -> Unit,
    onError: ((AppException) -> Unit)? = null,
    onLoading: (() -> Unit)? = null
) {
    when (resultState) {
        is ResultState.Loading -> {
            //showLoading(resultState.loadingMessage)
            onLoading?.run { this }
        }
        is ResultState.Success -> {
            //dismissLoading()
            onSuccess(resultState.data)
        }
        is ResultState.Error -> {
            //dismissLoading()
            onError?.run { this(resultState.error) }
        }
    }
}


/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
suspend fun <T> executeResponse(
    response: ApiResponse<T>,
    success: suspend CoroutineScope.(ApiResponse<T>) -> Unit
) {
    coroutineScope {
        when {
            response.isSucces() -> {
                success(response)
            }
            else -> {
                throw AppException(
                    response.getResponseCode(),
                    response.getResponseMsg(),
                    response.getResponseMsg()
                )
            }
        }
    }
}