package com.zjh.demo.imageloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.zjh.imageloader.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1589257005090&di=dff328646fb37fa17382a084089ec82a&imgtype=0&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D862704645%2C1557247143%26fm%3D214%26gp%3D0.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Application中调用
        ImageLoader.instance.defaultOptions = DefImgOpts()
        ImageLoader.instance.setOnLowMemory(application)

        //...
        ImageLoader.instance.load(iv, url, maxByteCount = 50000)

        iv.setOnClickListener {
            Toast.makeText(this, "点击", Toast.LENGTH_SHORT).show()
        }
    }
}
