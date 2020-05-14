package com.zjh.imageloader.target

import android.graphics.drawable.Drawable
import android.view.View

/**
 * 图片等比压缩的ViewTarget
 * @author 张坚鸿
 * @date 2020/5/14 0014
 * @param ratio 图片质量压缩比例
 */
abstract class CompressViewTarget<Z>(v: View, ratio: Double = 1.0) : BaseCompAnimatableViewTarget<View, Z>(v, ratio) {

    override fun getCurrentDrawable(): Drawable? {
        return view.background
    }

    override fun setDrawable(drawable: Drawable?) {
        view.background = drawable
    }
}