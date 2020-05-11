package com.zjh.imageloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zjh.imageloader.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1589215595696&di=b04c3dc95f7c6064247474a9dd24ede5&imgtype=0&src=http%3A%2F%2Fimg.nga.178.com%2Fattachments%2Fmon_202004%2F10%2FeyQ5-8zvoZfT3cSj6-pk.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImageLoader.instance.load(iv, url)
    }
}
