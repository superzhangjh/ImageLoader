package com.zjh.imageloader.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.zjh.imageloader.ImageLoader.Companion.TAG
import com.zjh.imageloader.options.ImageOptions
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object BitmapUtils {

    /**
     * 压缩Bitmap，生成新的bitmap
     * @param defRatio 压缩比例
     * @param maxByteCount 最大限制
     */
    @WorkerThread
    fun compress(bitmap: Bitmap, defRatio: Float, maxByteCount: Int): Bitmap {
        if (!isNeedCompress(maxByteCount, defRatio)) {
            return bitmap
        }
        //将bitmap转为byte[]
        val byte = ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
        }.toByteArray()

        //一次采样：只返回图片的信息，不占用内存
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byte, 0, byte.size, options)

        //二次采样：计算采样率，并取消只返回图片信息
        val ratio = calRatio(bitmap.byteCount, maxByteCount, defRatio)
        Log.d(TAG, "压缩采样率:${ratio}")
        options.inSampleSize = calSampleSize(options, ratio)
        options.inJustDecodeBounds = false

        //生成新的bitmap并返回
        return BitmapFactory.decodeByteArray(byte, 0, byte.size, options)
    }

    /**
     * @return 压缩比例
     * @param preByteCount 原先大小
     * @param maxByteCount 最大限制
     * @param ratio 预设比例
     */
    private fun calRatio(preByteCount: Int, maxByteCount: Int, ratio: Float): Float {
        return if (maxByteCount <= 0) {
            ratio
        } else {
            //压缩后大小
            val preRatioSize = (preByteCount * ratio).toInt()
            if (preRatioSize > maxByteCount) {
                1f - (preByteCount - maxByteCount) / preByteCount.toFloat()
            } else {
                ratio
            }
        }
    }

    /**
     * 计算inSampleSize的值
     * @param scale 缩放比例
     */
    private fun calSampleSize(options: BitmapFactory.Options, ratio: Float): Int {
        //图片宽高
        val width = options.outWidth
        val height = options.outHeight
        //目标大小
        val reqWidth = width * ratio
        val reqHeight= height * ratio

        var inSampleSize = 1
        if (width > reqWidth || height > reqHeight) {
            val wRound = (width * 1f / reqWidth).roundToInt()
            val hRound = (height * 1f / reqHeight).roundToInt()
            inSampleSize = Math.max(wRound, hRound)
        }
        return inSampleSize
    }

    /**
     * 是否需要压缩
     */
    fun isNeedCompress(maxByteCount: Int, ratio: Float): Boolean {
        return maxByteCount != 0 || ratio != 1f
    }
}