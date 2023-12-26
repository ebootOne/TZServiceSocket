package com.main.accessible.tz.livedate

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer


/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《LiveData 数据倒灌 背景缘由全貌 独家解析》文章开头的解析
 *
 *
 * https://xiaozhuanlan.com/topic/6719328450
 *
 *
 * 本类参考了官方 SingleEventLive 的非入侵设计，
 * 以及小伙伴 Flywith24 在 wrapperLiveData 中通过 ViewModelStore 来唯一确定订阅者的思路，
 *
 *
 * TODO：在当前最新版中，我们透过对 ViewModelStore 的内存地址的遍历，
 * 来确保：
 * 1.一条消息能被多个观察者消费
 * 2.消息被所有观察者消费完毕后才开始阻止倒灌
 * 3.可以通过 clear 方法手动将消息从内存中移除
 * 4.让非入侵设计成为可能，遵循开闭原则
 *
 *
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 篡改来自 "数据层" 的数据，数据层的数据务必通过 "唯一可信源" 来分发，
 * 如果这样说还不理解，详见：
 * https://xiaozhuanlan.com/topic/0168753249 和 https://xiaozhuanlan.com/topic/6719328450
 *
 *
 * Create by KunMinX at 2020/7/21
 */
open class UnPeekLiveData<T> : ProtectedUnPeekLiveData<T>() {
    public override fun setValue(value: T?) {
        super.setValue(value)
    }

    public override fun postValue(value: T?) {
        super.postValue(value)
    }



    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        super.observe(owner, observer)
        throw IllegalArgumentException(
            """
                请不要在 UnPeekLiveData 中使用 observe 方法。取而代之的是在 Activity 和 Fragment 中分别使用 observeInActivity 和 observeInFragment 来观察。
                
                Taking into account the normal permission of preventing backflow logic,  do not use observeForever to communicate between pages.Instead, you can use ObserveInActivity and ObserveInFragment methods to observe in Activity and Fragment respectively.
                """.trimIndent()
        )
    }

    /**
     * TODO：Tip：请不要在 UnPeekLiveData 中使用 observeForever 方法。
     *
     *
     * 2020.8.1 背景缘由：
     * UnPeekLiveData 主要用于表现层的 页面转场 和 页面间通信 场景下的非粘性消息分发，
     * 出于生命周期安全等因素的考虑，不建议使用 observeForever 方法，
     *
     *
     * 对于数据层的工作，如有需要，可结合实际场景使用 RxJava 或 kotlin flow。
     *
     * @param observer
     */
    override fun observeForever(observer: Observer<in T?>) {
        throw IllegalArgumentException(
            """
                出于生命周期安全的考虑，请不要在 UnPeekLiveData 中使用 observeForever 方法。
                
                Considering avoid lifecycle security issues, do not use observeForever for communication between pages.
                """.trimIndent()
        )
    }
    class Builder<T> {
        /**
         * 是否允许传入 null value
         */
        private var isAllowNullValue = false
        fun setAllowNullValue(allowNullValue: Boolean): Builder<T> {
            isAllowNullValue = allowNullValue
            return this
        }

        fun create(): UnPeekLiveData<T> {
            val liveData: UnPeekLiveData<T> = UnPeekLiveData<T>()
            liveData.isAllowNullValue = isAllowNullValue
            return liveData
        }
    }
}
