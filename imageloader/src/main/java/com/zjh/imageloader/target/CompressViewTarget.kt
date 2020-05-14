package com.zjh.imageloader.target

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Synthetic
import com.zjh.imageloader.R
import java.lang.ref.WeakReference
import java.util.*

/**
 * 图片等比压缩的ViewTarget
 * @author 张坚鸿
 * @date 2020/5/14 0014
 * @param ratio 图片质量压缩比例
 */
abstract class CompressViewTarget<Z>(view: View, ratio: Double = 1.0) : Target<Z>, Transition.ViewAdapter {

    private val sizeDeterminer: SizeDeterminer
    private var animatable: Animatable? = null

    private val view: View = Preconditions.checkNotNull(view)

    override fun getView(): View {
        return view
    }

    private var attachStateListener: OnAttachStateChangeListener? = null
    private var isClearedByUs = false
    private var isAttachStateListenerAdded = false

    override fun onStart() {
        animatable?.start()
    }

    override fun onStop() {
        animatable?.stop()
    }

    override fun onDestroy() {
        // Default empty.
    }

    override fun getCurrentDrawable(): Drawable? {
        return view.background
    }

    override fun setDrawable(drawable: Drawable?) {
        view.background = drawable
    }

    private fun setResourceInternal(resource: Z?) {
        setResource(resource)
        maybeUpdateAnimatable(resource)
    }

    private fun maybeUpdateAnimatable(resource: Z?) {
        if (resource is Animatable) {
            animatable = resource
            animatable!!.start()
        } else {
            animatable = null
        }
    }

    fun waitForLayout(): CompressViewTarget<Z> {
        sizeDeterminer.waitForLayout = true
        return this
    }

    // Public API.
    fun clearOnDetach(): CompressViewTarget<Z> {
        if (attachStateListener != null) {
            return this
        }
        attachStateListener = object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                resumeMyRequest()
            }

            override fun onViewDetachedFromWindow(v: View) {
                pauseMyRequest()
            }
        }
        maybeAddAttachStateListener()
        return this
    }

    // Public API.
    @Deprecated(
        """Using this method prevents clearing the target from working properly. Glide uses
        its own internal tag id so this method should not be necessary. This method is currently a
        no-op."""
    )
    fun useTagId(@IdRes tagId: Int): CompressViewTarget<Z> {
        return this
    }

    override fun getSize(cb: SizeReadyCallback) {
        sizeDeterminer.getSize(cb)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        sizeDeterminer.removeCallback(cb)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        maybeAddAttachStateListener()
        setResourceInternal(null)
        setDrawable(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setResourceInternal(null)
        setDrawable(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        sizeDeterminer.clearCallbacksAndListener()
        if (animatable != null) {
            animatable!!.stop()
        }
        setResourceInternal(null)
        setDrawable(placeholder)
        if (!isClearedByUs) {
            maybeRemoveAttachStateListener()
        }
    }

    override fun onResourceReady(resource: Z, transition: Transition<in Z>?) {
        if (transition == null || !transition.transition(resource, this)) {
            setResourceInternal(resource)
        } else {
            maybeUpdateAnimatable(resource)
        }
    }

    protected abstract fun setResource(resource: Z?)

    override fun setRequest(request: Request?) {
        tag = request
    }

    /** Returns any stored request using [android.view.View.getTag].  */
    override fun getRequest(): Request? {
        val tag = tag
        return if (tag != null) {
            if (tag is Request) {
                tag
            } else {
                throw IllegalArgumentException("You must not pass non-R.id ids to setTag(id)")
            }
        } else null
    }

    override fun toString(): String {
        return "Target for: $view"
    }

    @Synthetic
    fun resumeMyRequest() {
        val request = request
        if (request != null && request.isCleared) {
            request.begin()
        }
    }

    @Synthetic
    fun pauseMyRequest() {
        val request = request
        if (request != null) {
            isClearedByUs = true
            request.clear()
            isClearedByUs = false
        }
    }

    private var tag: Any?
        get() = view.getTag(VIEW_TAG_ID)
        private set(tag) {
            view.setTag(VIEW_TAG_ID, tag)
        }

    private fun maybeAddAttachStateListener() {
        if (attachStateListener == null || isAttachStateListenerAdded) {
            return
        }
        view.addOnAttachStateChangeListener(attachStateListener)
        isAttachStateListenerAdded = true
    }

    private fun maybeRemoveAttachStateListener() {
        if (attachStateListener == null || !isAttachStateListenerAdded) {
            return
        }
        view.removeOnAttachStateChangeListener(attachStateListener)
        isAttachStateListenerAdded = false
    }

    /**
     * @param sqrtRatio 内存缩放比例的开跟（因为内存是width * height，那么宽高的单独缩放比例就是开跟）
     */
    @VisibleForTesting
    internal class SizeDeterminer(private val view: View, private val sqrtRatio: Double) {
        private val cbs: MutableList<SizeReadyCallback> =
            ArrayList()

        @Synthetic
        var waitForLayout = false
        private var layoutListener: SizeDeterminerLayoutListener? =
            null

        private fun notifyCbs(width: Int, height: Int) {
            for (cb in ArrayList(cbs)) {
                cb.onSizeReady(width, height)
            }
        }

        @Synthetic
        fun checkCurrentDimens() {
            if (cbs.isEmpty()) {
                return
            }
            val currentWidth = targetWidth
            val currentHeight = targetHeight
            if (!isViewStateAndSizeValid(currentWidth, currentHeight)) {
                return
            }
            notifyCbs(currentWidth, currentHeight)
            clearCallbacksAndListener()
        }

        fun getSize(cb: SizeReadyCallback) {
            val currentWidth = targetWidth
            val currentHeight = targetHeight
            if (isViewStateAndSizeValid(currentWidth, currentHeight)) {
                cb.onSizeReady(currentWidth, currentHeight)
                return
            }

            if (!cbs.contains(cb)) {
                cbs.add(cb)
            }
            if (layoutListener == null) {
                val observer = view.viewTreeObserver
                layoutListener = SizeDeterminerLayoutListener(this)
                observer.addOnPreDrawListener(layoutListener)
            }
        }

        fun removeCallback(cb: SizeReadyCallback) {
            cbs.remove(cb)
        }

        fun clearCallbacksAndListener() {
            val observer = view.viewTreeObserver
            if (observer.isAlive) {
                observer.removeOnPreDrawListener(layoutListener)
            }
            layoutListener = null
            cbs.clear()
        }

        private fun isViewStateAndSizeValid(width: Int, height: Int): Boolean {
            return isDimensionValid(width) && isDimensionValid(height)
        }

        private val targetHeight: Int
            get() {
                val verticalPadding = view.paddingTop + view.paddingBottom
                val layoutParams = view.layoutParams
                val layoutParamSize = layoutParams?.height ?: PENDING_SIZE
                return (getTargetDimen(view.height, layoutParamSize, verticalPadding) * sqrtRatio).toInt()
            }

        private val targetWidth: Int
            get() {
                val horizontalPadding = view.paddingLeft + view.paddingRight
                val layoutParams = view.layoutParams
                val layoutParamSize = layoutParams?.width ?: PENDING_SIZE
                return (getTargetDimen(view.width, layoutParamSize, horizontalPadding) * sqrtRatio).toInt()
            }

        private fun getTargetDimen(viewSize: Int, paramSize: Int, paddingSize: Int): Int {
            val adjustedParamSize = paramSize - paddingSize
            if (adjustedParamSize > 0) {
                return adjustedParamSize
            }

            if (waitForLayout && view.isLayoutRequested) {
                return PENDING_SIZE
            }

            val adjustedViewSize = viewSize - paddingSize
            if (adjustedViewSize > 0) {
                return adjustedViewSize
            }

            if (!view.isLayoutRequested && paramSize == ViewGroup.LayoutParams.WRAP_CONTENT) {
                if (Log.isLoggable(
                        TAG,
                        Log.INFO
                    )
                ) {
                    Log.i(
                        TAG,
                        "Glide treats LayoutParams.WRAP_CONTENT as a request for an image the size of"
                                + " this device's screen dimensions. If you want to load the original image and"
                                + " are ok with the corresponding memory cost and OOMs (depending on the input"
                                + " size), use .override(Target.SIZE_ORIGINAL). Otherwise, use"
                                + " LayoutParams.MATCH_PARENT, set layout_width and layout_height to fixed"
                                + " dimension, or use .override() with fixed dimensions."
                    )
                }
                return getMaxDisplayLength(view.context)
            }

            return PENDING_SIZE
        }

        private fun isDimensionValid(size: Int): Boolean {
            return size > 0 || size == Target.SIZE_ORIGINAL
        }

        private class SizeDeterminerLayoutListener internal constructor(sizeDeterminer: SizeDeterminer) :
            OnPreDrawListener {
            private val sizeDeterminerRef: WeakReference<SizeDeterminer> by lazy { WeakReference(sizeDeterminer) }
            override fun onPreDraw(): Boolean {
                if (Log.isLoggable(
                        TAG,
                        Log.VERBOSE
                    )
                ) {
                    Log.v(
                        TAG,
                        "OnGlobalLayoutListener called attachStateListener=$this"
                    )
                }
                val sizeDeterminer = sizeDeterminerRef.get()
                sizeDeterminer?.checkCurrentDimens()
                return true
            }

        }

        companion object {
            private const val PENDING_SIZE = 0

            @VisibleForTesting
            var maxDisplayLength: Int = 0

            private fun getMaxDisplayLength(context: Context): Int {
                if (maxDisplayLength == 0) {
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = Preconditions.checkNotNull(windowManager).defaultDisplay
                    val displayDimensions = Point()
                    display.getSize(displayDimensions)
                    maxDisplayLength = Math.max(displayDimensions.x, displayDimensions.y)
                }
                return maxDisplayLength
            }
        }

    }

    companion object {
        private const val TAG = "CustomViewTarget"

        @IdRes
        private val VIEW_TAG_ID = R.id.glide_custom_view_target_tag
    }

    init {
        sizeDeterminer = SizeDeterminer(view, if (ratio > 1.0) {
            1.0
        } else {
            Math.sqrt(ratio)
        })
    }
}