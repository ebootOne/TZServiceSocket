package com.main.accessible.tz.viewmodel

import androidx.lifecycle.ViewModel
import com.main.accessible.tz.livedate.EventLiveData

open class BaseViewModel: ViewModel() {
    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }

    inner class UiLoadingChange{
        val showDialog by lazy { EventLiveData<String> ()  }
        val dismissDialog by lazy { EventLiveData<Boolean> ()  }
    }
}