package com.android.purebilibili.core.ui.transition.native

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.animation.PathInterpolator
import androidx.compose.runtime.staticCompositionLocalOf

internal data class NativeVideoCardTransitionCloseRequest(
    val videoKey: String
)

private data class ActiveCloseTransition(
    val videoKey: String,
    val spec: NativeVideoCardTransitionSpec
)

internal val LocalNativeVideoCardTransitionController =
    staticCompositionLocalOf<NativeVideoCardTransitionController?> { null }

internal class NativeVideoCardTransitionController(
    private val contentView: View,
    private val overlayView: NativeVideoCardTransitionOverlayView
) {
    private val interpolator = PathInterpolator(0.18f, 0.76f, 0.22f, 1f)
    private var animator: ValueAnimator? = null
    private var isRunning = false
    private var runningPhase: NativeVideoCardTransitionPhase? = null
    private var activeCloseTransition: ActiveCloseTransition? = null
    private var previewCloseProgress = 0f

    fun startClose(
        request: NativeVideoCardTransitionCloseRequest,
        popAction: () -> Unit
    ) {
        val activeClose = activeCloseTransition
        if (activeClose?.videoKey == request.videoKey) {
            finishPreviewClose(popAction)
            return
        }
        val spec = resolveCloseSpec(request)
        if (spec == null) {
            popAction()
            return
        }
        when (runningPhase) {
            NativeVideoCardTransitionPhase.Closing -> return
            null -> Unit
        }

        isRunning = true
        runningPhase = NativeVideoCardTransitionPhase.Closing
        cancelAnimatorOnly()
        activeCloseTransition = ActiveCloseTransition(request.videoKey, spec)
        previewCloseProgress = 0f
        renderFrame(
            resolveNativeVideoCardTransitionFrame(
                spec = spec,
                progress = 0f,
                phase = NativeVideoCardTransitionPhase.Closing
            )
        )
        popAction()
        overlayView.post {
            animate(
                spec = spec,
                phase = NativeVideoCardTransitionPhase.Closing,
                startProgress = 0f,
                onEnd = ::clearTransitionState
            )
        }
    }

    fun previewClose(
        request: NativeVideoCardTransitionCloseRequest,
        progress: Float
    ): Boolean {
        val spec = resolveCloseSpec(request) ?: return false
        val clampedProgress = progress.coerceIn(0f, 1f)
        val activeClose = activeCloseTransition
        if (activeClose?.videoKey != request.videoKey) {
            cancelAnimatorOnly()
            activeCloseTransition = ActiveCloseTransition(request.videoKey, spec)
        }
        isRunning = true
        runningPhase = NativeVideoCardTransitionPhase.Closing
        previewCloseProgress = clampedProgress
        renderFrame(
            resolveNativeVideoCardTransitionFrame(
                spec = spec,
                progress = clampedProgress,
                phase = NativeVideoCardTransitionPhase.Closing
            )
        )
        return true
    }

    fun finishPreviewClose(popAction: () -> Unit): Boolean {
        val activeClose = activeCloseTransition ?: return false
        popAction()
        overlayView.post {
            animate(
                spec = activeClose.spec,
                phase = NativeVideoCardTransitionPhase.Closing,
                startProgress = previewCloseProgress.coerceIn(0f, 1f),
                onEnd = ::clearTransitionState
            )
        }
        return true
    }

    fun cancelPreviewClose(): Boolean {
        val activeClose = activeCloseTransition ?: return false
        val startProgress = previewCloseProgress.coerceIn(0f, 1f)
        animate(
            spec = activeClose.spec,
            phase = NativeVideoCardTransitionPhase.Closing,
            startProgress = startProgress,
            endProgress = 0f,
            onEnd = ::clearTransitionState
        )
        return true
    }

    fun cancel() {
        cancelAnimatorOnly()
        clearTransitionState()
    }

    private fun animate(
        spec: NativeVideoCardTransitionSpec,
        phase: NativeVideoCardTransitionPhase,
        startProgress: Float = 0f,
        endProgress: Float = 1f,
        onEnd: () -> Unit
    ) {
        cancelAnimatorOnly()
        val clampedStart = startProgress.coerceIn(0f, 1f)
        val clampedEnd = endProgress.coerceIn(0f, 1f)
        animator = ValueAnimator.ofFloat(clampedStart, clampedEnd).apply {
            duration = resolveRemainingDurationMillis(clampedStart, clampedEnd)
            interpolator = this@NativeVideoCardTransitionController.interpolator
            addUpdateListener { valueAnimator ->
                val progress = valueAnimator.animatedValue as Float
                renderFrame(
                    resolveNativeVideoCardTransitionFrame(
                        spec = spec,
                        progress = progress,
                        phase = phase
                    )
                )
            }
            doOnFinish {
                animator = null
                onEnd()
            }
            start()
        }
    }

    private fun ValueAnimator.doOnFinish(action: () -> Unit) {
        addListener(
            object : AnimatorListenerAdapter() {
                private var canceled = false
                private var finished = false

                override fun onAnimationCancel(animation: Animator) {
                    canceled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (finished || canceled) return
                    finished = true
                    action()
                }
            }
        )
    }

    private fun renderFrame(frame: NativeVideoCardTransitionFrame) {
        overlayView.showFrame(frame)
        applyRenderEffect(frame.blurRadiusPx)
    }

    @SuppressLint("NewApi")
    private fun applyRenderEffect(blurRadiusPx: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        contentView.setRenderEffect(
            if (blurRadiusPx > 0.5f) {
                RenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP)
            } else {
                null
            }
        )
    }

    private fun clearTransitionState() {
        isRunning = false
        runningPhase = null
        activeCloseTransition = null
        previewCloseProgress = 0f
        overlayView.clearFrame()
        clearRenderEffect()
    }

    @SuppressLint("NewApi")
    private fun clearRenderEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentView.setRenderEffect(null)
        }
    }

    private fun cancelAnimatorOnly() {
        animator?.cancel()
        animator = null
    }

    private fun resolveCloseSpec(request: NativeVideoCardTransitionCloseRequest): NativeVideoCardTransitionSpec? {
        if (request.videoKey.isBlank()) return null
        return NativeVideoCardTransitionSpec()
    }

    private fun resolveRemainingDurationMillis(startProgress: Float, endProgress: Float): Long {
        val distance = kotlin.math.abs(endProgress - startProgress).coerceIn(0f, 1f)
        return (NATIVE_VIDEO_CARD_TRANSITION_DURATION_MILLIS * distance)
            .toLong()
            .coerceAtLeast(MIN_TRANSITION_DURATION_MILLIS)
    }

    companion object {
        private const val MIN_TRANSITION_DURATION_MILLIS = 80L
    }
}
