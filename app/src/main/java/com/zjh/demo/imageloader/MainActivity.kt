package com.zjh.demo.imageloader

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.zjh.imageloader.ImageLoader
import com.zjh.imageloader.target.BaseCompressViewTarget
import com.zjh.imageloader.target.CompressImageViewTarget
import com.zjh.imageloader.target.CompressViewTarget
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1589436076722&di=cc316c13d38ca5a2089b8d2a500d3b37&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201806%2F29%2F20180629212944_werXh.jpeg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Application中调用
        ImageLoader.instance.setOnLowMemory(application)

        //...
        Glide.with(this)
            .load(url)
            .into(object : CompressViewTarget<Drawable>(iv1, 0.01) {
            override fun setResource(resource: Drawable?) {
                iv1.background = resource
            }
        })

//        //原图
//        Glide.with(this)
//            .asBitmap()
//            .placeholder(R.drawable.loading)
//            .error(R.drawable.error)
//            .load(url)
//            .into(object : SimpleTarget<Bitmap>() {
//                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
//                    iv1.setImageBitmap(bitmap)
//                    tv1.text = "原图\n${getBitmapInfo(bitmap)}"
//                }
//
//            })
//
//        //Glide默认 PREFER_ARGB_8888
//        Glide.with(this)
//            .asBitmap()
//            .placeholder(R.drawable.loading)
//            .error(R.drawable.error)
//            .load(url)
//            .into(object : ImageViewTarget<Bitmap>(iv2) {
//                override fun setResource(resource: Bitmap?) {
//                    resource?.run {
//                        iv2.setImageBitmap(this)
//                        tv2.text = "自适应(ARGB_8888)\n${getBitmapInfo(this)}"
//                    }
//                }
//            })
//
//        //Glide默认 RGB_565 不透明
//        Glide.with(this)
//            .asBitmap()
//            .format(DecodeFormat.PREFER_RGB_565)
//            .placeholder(R.drawable.loading)
//            .error(R.drawable.error)
//            .load(url)
//            .into(object : ImageViewTarget<Bitmap>(iv3) {
//                override fun setResource(resource: Bitmap?) {
//                    resource?.run {
//                        iv3.setImageBitmap(this)
//                        tv3.text = "自适应(RGB_565 不透明)\n${getBitmapInfo(this)}"
//                    }
//                }
//            })
//
//        //CompressViewTarget
//        val ratio = 0.75
//        Glide.with(this)
//            .asBitmap()
//            .placeholder(R.drawable.loading)
//            .error(R.drawable.error)
//            .load(url)
//            .into(object : CompressViewTarget<Bitmap>(iv4, ratio) {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    iv4.setImageBitmap(resource)
//                    tv4.text = "压缩 ${ratio}倍\n${getBitmapInfo(resource)}"
//                }
//            })
//
//        //CompressViewTarget
//        Glide.with(this)
//            .asBitmap()
//            .format(DecodeFormat.PREFER_RGB_565)
//            .placeholder(R.drawable.loading)
//            .error(R.drawable.error)
//            .load(url)
//            .into(object : CompressImageViewTarget<Bitmap>(iv5, ratio) {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    iv5.setImageBitmap(resource)
//                    tv5.text = "压缩 ${ratio}倍(RGB_565 不透明)\n${getBitmapInfo(resource)}"
//                }
//            })
    }

    private fun getBitmapInfo(bitmap: Bitmap?): String {
        return "${bitmap?.width}*${bitmap?.height} ${String.format("%.2f",  bitmap?.run { byteCount / 1024f / 1024f } ?: 0.00)}M"
    }
}
