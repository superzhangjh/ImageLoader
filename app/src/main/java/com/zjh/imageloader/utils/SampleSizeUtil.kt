package com.zjh.imageloader.utils

import android.graphics.BitmapFactory
import kotlin.math.roundToInt


object SampleSizeUtil {

    /**
     * 计算inSampleSize的值
     * @param scale 缩放比例
     */
    fun calSampleSize(options: BitmapFactory.Options, scale: Float = 1f): Int {
        //图片宽高
        val width = options.outWidth
        val height = options.outHeight
        //目标大小
        val reqWidth = width * scale
        val reqHeight= height * scale

        var inSampleSize = 1
        if (width > reqWidth || height > reqHeight) {
            val wRound = (width * 1f / reqWidth).roundToInt()
            val hRound = (height * 1f / reqHeight).roundToInt()
            inSampleSize = Math.max(wRound, hRound)
        }
        return inSampleSize
    }

}