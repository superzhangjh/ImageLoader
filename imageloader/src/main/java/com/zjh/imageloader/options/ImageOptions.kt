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
    fun getCompressionRatio(): Float

    /**
     * 由于采样率是采用2的N次方的临近数，所以结果会有误差
     * @return 限制压缩后的图片最大（如果大于压缩比例，则以压缩比例为准，反之则以其为准）
     */
    fun getMaxByteCount(): Int

    /**
     * @return Glide请求设置
     */
    fun getRequestOptions(): RequestOptions?
}