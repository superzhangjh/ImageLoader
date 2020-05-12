package com.zjh.demo.imageloader

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zjh.imageloader.options.ImageOptions

/**
 * 默认加载
 * @author 张坚鸿
 * @date 2020/5/12 0012
 */
open class DefImgOpts : ImageOptions {

    private val options by lazy { RequestOptions()
            .error(R.mipmap.ic_launcher)
            .placeholder(R.mipmap.ic_launcher)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    override fun getCompressionRatio() = 0.5f

    override fun getMaxByteCount() = 0

    override fun getRequestOptions(): RequestOptions? {
        return options
    }
}