package com.zjh.imageloader.target

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.Transition.ViewAdapter

/**
 * 带动画的View
 */
abstract  class BaseCompAnimatableViewTarget<T : View, Z>(private val v: T, ratio: Double)
    : BaseCompressViewTarget<T, Z>(v, ratio), ViewAdapter {

    private var animatable: Animatable? = null

    override fun getView(): View {
        return v
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        super.onLoadStarted(placeholder)
        setResourceInternal(null)
        setDrawable(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        setResourceInternal(null)
        setDrawable(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        super.onLoadCleared(placeholder)
        animatable?.stop()
        setResourceInternal(null)
        setDrawable(placeholder)
    }

    override fun onResourceReady(resource: Z, transition: Transition<in Z?>?) {
        if (transition == null || !transition.transition(resource, this)) {
            setResourceInternal(resource)
        } else {
            maybeUpdateAnimatable(resource)
        }
    }

    override fun onStart() {
        animatable?.start()
    }

    override fun onStop() {
        animatable?.stop()
    }

    private fun setResourceInternal(resource: Z?) {
        // Order matters here. Set the resource first to make sure that the Drawable has a valid and
        // non-null Callback before starting it.
        setResource(resource)
        maybeUpdateAnimatable(resource)
    }

    private fun maybeUpdateAnimatable(resource: Z?) {
        if (resource != null && resource is Animatable) {
            animatable = resource
            animatable!!.start()
        } else {
            animatable = null
        }
    }

    protected abstract fun setResource(resource: Z?)
}