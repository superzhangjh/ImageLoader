package com.zjh.imageloader.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Job

/**
 * job生命周期类，onDestroy自动取消
 * @author 张坚鸿
 * @date 2020/5/12 0012
 */
class JobLifecycleObserver(private vararg val jobs: Job) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cancel() {
        jobs.forEach {
            if (!it.isCancelled) {
                it.cancel()
            }
        }
    }

}