package com.zjh.imageloader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.zjh.imageloader.utils.SampleSizeUtil.calSampleSize
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

/**
 * bitmap压缩
 */
class BitmapCompression(private val iv: ImageView, private val bitmap: Bitmap) : LifecycleObserver {

    private var deferred: Deferred<Bitmap>? = null

    init {
        if (iv.context is FragmentActivity) {
            (iv.context as FragmentActivity).lifecycle.addObserver(this)
        }
    }

    suspend fun compress() {
        //IO线程压缩Bitmap
        deferred = GlobalScope.async<Bitmap>(context = Dispatchers.IO) {
            //将bitmap转为byte[]
            val byte = ByteArrayOutputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray()

            //一次采样，只返回图片的信息，不占用内存
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byte, 0, byte.size, options)

            //计算采样率，并取消只返回图片信息
            options.inSampleSize = calSampleSize(options, scale = 0.5f)
            options.inJustDecodeBounds = false
            //加载图片
            BitmapFactory.decodeByteArray(byte, 0, byte.size, options)
        }
        deferred?.run {
            //主线程设置结果
            GlobalScope.launch(context = Dispatchers.Main) {
                iv.setImageBitmap(await())
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cancel() {
        if (deferred?.isCancelled == false) {
            deferred?.cancel()
        }
    }
}