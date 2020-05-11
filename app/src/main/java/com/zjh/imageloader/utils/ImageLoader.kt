package com.zjh.imageloader.utils

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget

/**
 * 图片加载工具类（网络图片加载、bitmap压缩）
 * @date 2020/5/11 0011
 */
class ImageLoader {

    companion object {
        @JvmStatic
        val instance: ImageLoader by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ImageLoader() }
    }

    /**
     * 初始化
     */
    fun init(app: Application) {
        app.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onLowMemory() {
                //低内存时释放缓存
                Glide.get(app).clearMemory()
            }

            override fun onConfigurationChanged(newConfig: Configuration) {
            }
        })
    }

    fun load(iv: ImageView, url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        Glide.with(iv.context).load(url).into(object : ImageViewTarget<Drawable?>(iv) {
            override fun setResource(resource: Drawable?) {
                if (resource != null && iv.measuredWidth > 0 && iv.measuredHeight > 0) {
                    val bitmap = (resource as BitmapDrawable).bitmap
//                    BitmapCompression(iv, bitmap).compress()
                }
            }
        })
    }
}