package com.zjh.imageloader.target

import android.graphics.drawable.Drawable
import android.widget.ImageView

/**
 * ImageView压缩
 * @author 张坚鸿
 * @date 2020/5/14 0014
 */
abstract class CompressImageViewTarget<Z>(private val imageView: ImageView, ratio: Double = 1.0) : CompressViewTarget<Z>(imageView, ratio) {


}