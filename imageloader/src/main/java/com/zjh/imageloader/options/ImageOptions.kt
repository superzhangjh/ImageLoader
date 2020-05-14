package com.zjh.imageloader.options

import com.bumptech.glide.request.RequestOptions

/**
 * 图片加载设置
 * @author 张坚鸿
 * @date 2020/5/12 0012
 */
interface ImageOptions {

    /**
     * @return 图片压缩比例 0~1
     */
    fun getCompressRatio(): Double

    /**
     * @return Glide请求设置
     */
    fun getRequestOptions(): RequestOptions?
}