package com.zjh.imageloader

import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.zjh.imageloader.options.ImageOptions
import com.zjh.imageloader.utils.BitmapUtils
import com.zjh.imageloader.utils.JobLifecycleObserver
import kotlinx.coroutines.*

/**
 * 图片加载工具类（网络图片加载、bitmap压缩）
 * @date 2020/5/11 0011
 */
class ImageLoader {

    companion object {
        const val TAG = "ImageLoader+"

        @JvmStatic
        val instance: ImageLoader by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ImageLoader() }
    }

    /**
     * 默认设置
     */
    var defaultOptions : ImageOptions? = null

    /**
     * 设置低内存时释放缓存
     */
    fun setOnLowMemory(app: Application) {
        app.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onLowMemory() {
                Glide.get(app).clearMemory()
            }

            override fun onConfigurationChanged(newConfig: Configuration) {
            }
        })
    }

    fun load(iv: ImageView, url: Any?, options: ImageOptions? = defaultOptions) {
        //Glide 发起请求
        Glide.with(iv.context).load(url).run {

            options?.getRequestOptions()?.let { apply(it) }

            into(object : ImageViewTarget<Drawable?>(iv) {
                /**
                 * Glide会自动将图片压缩至imageView的大小，这里压缩的操作其实是对图片按指定比例去压缩大小，从而减少内存占用。
                 */
                override fun setResource(resource: Drawable?) {
                    if (options == null) {
                        load(iv, resource)
                    } else {
                        load(iv, resource, options.getCompressionRatio(), options.getMaxByteCount())
                    }
                }
            })
        }
    }

    fun load(iv: ImageView, url: Any?, ratio: Float = 1f, maxByteCount: Int = -1) {
        Glide.with(iv.context).load(url).into(object : ImageViewTarget<Drawable?>(iv) {
            override fun setResource(resource: Drawable?) {
                load(iv, resource, ratio, maxByteCount)
            }
        })
    }

    /**
     * 加载图片，并提供回调（用于设置View的background等）
     */
    fun load(context: Context, url: Any?, ratio: Float = 1f, maxByteCount: Int = -1, callback: (bitmap: Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                load(context, resource, ratio, maxByteCount, callback)
            }
        })
    }

    /**
     * 加载图片，并提供回调（用于设置View的background等）
     */
    fun load(context: Context, bitmap: Bitmap?, ratio: Float = 1f, maxByteCount: Int = -1, listener: (bitmap: Bitmap) -> Unit) {
        if (bitmap == null) {
            return
        }
        var bitmapDeferred: Deferred<Bitmap>? = null
        if (BitmapUtils.isNeedCompress(maxByteCount, ratio)) {
            //io:压缩bitmap
            bitmapDeferred = GlobalScope.async(context = Dispatchers.IO) {
                Log.d(TAG, "压缩前:${bitmap.byteCount} width:${bitmap.width} height:${bitmap.height}")
                BitmapUtils.compress(bitmap, ratio, maxByteCount).apply {
                    Log.d(TAG, "压缩后:${byteCount} width:${width} height:${height}")
                }
            }
        }

        bitmapDeferred?.run {
            //main:设置image的bitmap
            val imageJob = GlobalScope.launch(context = Dispatchers.Main) {
                listener.invoke(await())
            }

            //注册job自动回收
            if (context is FragmentActivity) {
                context.lifecycle.addObserver(JobLifecycleObserver(this, imageJob))
            }
        } ?: run {
            listener.invoke(bitmap)
        }
    }

    fun load(iv: ImageView, drawable: Drawable?, ratio: Float = 1f, maxByteCount: Int = -1) {
        if (drawable == null) {
            return
        }
        var bitmapDeferred: Deferred<Bitmap>? = null
        if (BitmapUtils.isNeedCompress(maxByteCount, ratio)) {
            //io:压缩bitmap
            bitmapDeferred = GlobalScope.async(context = Dispatchers.IO) {
                val bitmap = (drawable as BitmapDrawable).bitmap.apply {
                    Log.d(TAG, "压缩前:${byteCount} width:${width} height:${height}")
                }

                BitmapUtils.compress(bitmap, ratio, maxByteCount).apply {
                    Log.d(TAG, "压缩后:${byteCount} width:${width} height:${height}")
                }
            }
        }

        bitmapDeferred?.run {
            //main:设置image的bitmap
            val imageJob = GlobalScope.launch(context = Dispatchers.Main) {
                iv.setImageBitmap(await())
            }

            //注册job自动回收
            if (iv.context is FragmentActivity) {
                (iv.context as FragmentActivity).lifecycle.addObserver(JobLifecycleObserver(this, imageJob))
            }
        } ?: run {
            iv.setImageDrawable(drawable)
        }
    }

    /**
     * 兼容Java调用
     */
    fun load(iv: ImageView, url: Any?) {
        load(iv, url, defaultOptions)
    }
}