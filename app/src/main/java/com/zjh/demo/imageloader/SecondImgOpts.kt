package com.wyt.common.utils.imageoptions

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zjh.demo.imageloader.DefImgOpts
import com.zjh.demo.imageloader.R

/**
 * 全屏加载
 * @author 张坚鸿
 * @date 2020/5/12 0012
 */
class SecondImgOpts : DefImgOpts() {

    private val options by lazy { RequestOptions()
        .error(R.mipmap.ic_launcher_round)
        .placeholder(R.mipmap.ic_launcher_round)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    override fun getCompressionRatio() = 0.5f

    override fun getMaxByteCount() = 0

    override fun getRequestOptions(): RequestOptions? {
        return options
    }

    companion object {
        private val instance: SecondImgOpts by lazy { SecondImgOpts() }

        @JvmStatic
        fun get(): SecondImgOpts {
            return instance
        }
    }
}