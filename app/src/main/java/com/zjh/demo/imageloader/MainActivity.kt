package com.zjh.demo.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.zjh.imageloader.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private val url = "http://attach.bbs.miui.com/forum/201706/16/174318fpxmpt5titpz56qx.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Application中调用
        ImageLoader.instance.defaultOptions = DefImgOpts()
        ImageLoader.instance.setOnLowMemory(application)

        //...
//        ImageLoader.instance.load(iv, url, maxByteCount = 50000)

//        iv.setOnClickListener {
//            Toast.makeText(this, "点击", Toast.LENGTH_SHORT).show()
//        }

        Glide.with(this).load(url)
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .apply(RequestOptions.bitmapTransform(object : Transformation<Bitmap?> {
                override fun updateDiskCacheKey(messageDigest: MessageDigest) {

                }

                override fun transform(
                    context: Context,
                    resource: Resource<Bitmap?>, //bitmap会生成最小比例
                    outWidth: Int,  //设置到View时的大小, 以长宽的最大值，去作为最小值获取原有比例的图片大小
                    outHeight: Int
                ): Resource<Bitmap?> {
                    //TODO：这里可以进行图片压缩的操作
                    //TODO:重写一个CustonViewTarget，然后修改getTargetWidth getTargetHeight
                    val bitmap = resource.get()
                    Log.d("测试", "transform outW:$outWidth outH:$outHeight " +
                            "w:${bitmap.width} h:${bitmap.height} count:${bitmap.byteCount}")
                    return resource
                }
            }))
            .into(object : CompressViewTarget<View, Drawable>(iv, 0.7) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                Log.d("测试", "CompressViewTarget onLoadFailed")
            }

            override fun onResourceCleared(placeholder: Drawable?) {
                Log.d("测试", "CompressViewTarget onResourceCleared")

            }

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                val bitmap = (resource as BitmapDrawable).bitmap
                Log.d("测试", "CompressViewTarget ${bitmap.width} ${bitmap.height} count:${bitmap.byteCount}")
            }
        })
    }
}
