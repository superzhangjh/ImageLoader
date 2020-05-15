package com.zjh.imageloader

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.zjh.imageloader.options.ImageOptions
import com.zjh.imageloader.target.BaseCompAnimatableViewTarget
import com.zjh.imageloader.target.CompressImageViewTarget
import com.zjh.imageloader.target.CompressResViewTarget
import com.zjh.imageloader.target.CompressViewTarget

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
     * 默认缩放
     */
    fun getDefCompressRatio(): Double {
        return defaultOptions?.getCompressRatio() ?: 1.0
    }

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

    /**
     * 兼容java调用
     */
    fun load(target: View, url: Any?) {
        load(target, url, defaultOptions)
    }

    /**
     * 兼容java调用
     */
    fun load(view: View, url: Any?, options: ImageOptions) {
        load(view, url, options, getCompressTarget(view, url, options.getCompressRatio()))
    }

    /**
     * 兼容java调用
     */
    fun load(view: View, url: Any?, target: BaseCompAnimatableViewTarget<View, Drawable>) {
        load(view, url, defaultOptions, target)
    }

    /**
     * 加载图片
     * @param view 显示图片的View， 如果是ImageView则设置设置到src，如果是普通的view则设置成背景(背景铺满)
     */
    fun load(view: View,
             url: Any?,
             options: ImageOptions? = defaultOptions,
             target: BaseCompAnimatableViewTarget<out View, Drawable> = getCompressTarget(view, url, options?.getCompressRatio() ?: 1.0)
    ) {
        Glide.with(view.context)
            .load(url)
            .apply { options?.getRequestOptions()?.let { apply(it) } }
            .into(target)
    }

    private fun getCompressTarget(view: View, url: Any?, ratio: Double) : BaseCompAnimatableViewTarget<out View, Drawable> {
        return if (view is ImageView) {
            object : CompressImageViewTarget<Drawable>(view, ratio) {
                override fun setResource(target: ImageView, resource: Drawable?) {
                    target.setImageDrawable(resource)
                }
            }
        } else {
            if (url is Int) {
                object : CompressResViewTarget<Drawable>(view, ratio) {
                    override fun setResource(target: View, resource: Drawable?) {
                        target.background = resource
                    }
                }
            } else {
                object : CompressViewTarget<Drawable>(view, ratio) {
                    override fun setResource(target: View, resource: Drawable?) {
                        target.background = resource
                    }
                }
            }
        }
    }

//    fun load(iv: ImageView, url: Any?, options: ImageOptions? = defaultOptions) {
//        //Glide 发起请求
//        Glide.with(iv.context).load(url).run {
//
//            options?.getRequestOptions()?.let { apply(it) }
//
//            into(object : ImageViewTarget<Drawable?>(iv) {
//                /**
//                 * Glide会自动将图片压缩至imageView的大小，这里压缩的操作其实是对图片按指定比例去压缩大小，从而减少内存占用。
//                 */
//                override fun setResource(resource: Drawable?) {
//                    if (options == null) {
//                        load(iv, resource)
//                    } else {
//                        load(iv, resource, options.getCompressionRatio(), options.getMaxByteCount())
//                    }
//                }
//            })
//        }
//    }

//    /**
//     * 加载图片，并提供回调（用于设置View的background等）
//     */
//    fun load(context: Context, bitmap: Bitmap?, ratio: Float = 1f, maxByteCount: Int = -1, listener: (bitmap: Bitmap) -> Unit) {
//        if (bitmap == null) {
//            return
//        }
//        var bitmapDeferred: Deferred<Bitmap>? = null
//        if (BitmapUtils.isNeedCompress(maxByteCount, ratio)) {
//            //io:压缩bitmap
//            bitmapDeferred = GlobalScope.async(context = Dispatchers.IO) {
//                Log.d(TAG, "压缩前:${bitmap.byteCount} width:${bitmap.width} height:${bitmap.height}")
//                BitmapUtils.compress(bitmap, ratio, maxByteCount).apply {
//                    Log.d(TAG, "压缩后:${byteCount} width:${width} height:${height}")
//                }
//            }
//        }
//
//        bitmapDeferred?.run {
//            //main:设置image的bitmap
//            val imageJob = GlobalScope.launch(context = Dispatchers.Main) {
//                listener.invoke(await())
//            }
//
//            //注册job自动回收
//            if (context is FragmentActivity) {
//                context.lifecycle.addObserver(JobLifecycleObserver(this, imageJob))
//            }
//        } ?: run {
//            listener.invoke(bitmap)
//        }
//    }
//
//    fun load(iv: ImageView, drawable: Drawable?, ratio: Float = 1f, maxByteCount: Int = -1) {
//        if (drawable == null) {
//            return
//        }
//        var bitmapDeferred: Deferred<Bitmap>? = null
//        if (BitmapUtils.isNeedCompress(maxByteCount, ratio)) {
//            //io:压缩bitmap
//            bitmapDeferred = GlobalScope.async(context = Dispatchers.IO) {
//                val bitmap = (drawable as BitmapDrawable).bitmap.apply {
//                    Log.d(TAG, "压缩前:${byteCount} width:${width} height:${height}")
//                }
//
//                BitmapUtils.compress(bitmap, ratio, maxByteCount).apply {
//                    Log.d(TAG, "压缩后:${byteCount} width:${width} height:${height}")
//                }
//            }
//        }
//
//        bitmapDeferred?.run {
//            //main:设置image的bitmap
//            val imageJob = GlobalScope.launch(context = Dispatchers.Main) {
//                iv.setImageBitmap(await())
//            }
//
//            //注册job自动回收
//            if (iv.context is FragmentActivity) {
//                (iv.context as FragmentActivity).lifecycle.addObserver(JobLifecycleObserver(this, imageJob))
//            }
//        } ?: run {
//            iv.setImageDrawable(drawable)
//        }
//    }
}