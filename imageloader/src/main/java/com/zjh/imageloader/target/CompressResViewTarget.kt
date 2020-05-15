package com.zjh.imageloader.target

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View

/**
 * 由于加载本地资源到背景时，时间间隔过短会导致背景无法及时跟换，所以需要尽用占位图
 * @author 张坚鸿
 * @date 2020/5/15 0015
 */
abstract class CompressResViewTarget<Z>(v: View, ratio: Double = 1.0) : CompressViewTarget<Z>(v, ratio) {

    @SuppressLint("MissingSuperCall")
    override fun onLoadStarted(placeholder: Drawable?) {
    }
}