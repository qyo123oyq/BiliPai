package com.android.purebilibili.core.ui.transition.native

import android.os.Build

internal data class NativeVideoTransitionRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float
        get() = right - left

    val height: Float
        get() = bottom - top

    fun isUsable(): Boolean {
        return width > 1f && height > 1f
    }
}

internal enum class NativeVideoCardTransitionPhase {
    Closing
}

internal data class NativeVideoCardTransitionSpec(
    val maxBlurRadiusPx: Float = NATIVE_VIDEO_CARD_TRANSITION_MAX_BLUR_RADIUS_PX,
    val maxScrimAlpha: Float = NATIVE_VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA
)

internal data class NativeVideoCardTransitionFrame(
    val blurRadiusPx: Float,
    val scrimAlpha: Float
)

internal const val NATIVE_VIDEO_CARD_TRANSITION_DURATION_MILLIS = 420L
internal const val NATIVE_VIDEO_CARD_TRANSITION_MAX_BLUR_RADIUS_PX = 48f
private const val NATIVE_VIDEO_CARD_TRANSITION_MAX_SCRIM_ALPHA = 0.34f

internal fun resolveNativeVideoCardTransitionFrame(
    spec: NativeVideoCardTransitionSpec,
    progress: Float,
    phase: NativeVideoCardTransitionPhase,
    sdkInt: Int = Build.VERSION.SDK_INT
): NativeVideoCardTransitionFrame {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val effectStrength = resolveNativeVideoCardTransitionEffectStrength(clampedProgress, phase)
    val blurRadiusPx = if (sdkInt >= Build.VERSION_CODES.S) {
        spec.maxBlurRadiusPx.coerceAtLeast(0f) * effectStrength
    } else {
        0f
    }

    return NativeVideoCardTransitionFrame(
        blurRadiusPx = blurRadiusPx,
        scrimAlpha = spec.maxScrimAlpha.coerceIn(0f, 1f) * effectStrength
    )
}

private fun resolveNativeVideoCardTransitionEffectStrength(
    progress: Float,
    phase: NativeVideoCardTransitionPhase
): Float {
    val easedProgress = smoothStep(progress.coerceIn(0f, 1f))
    return when (phase) {
        NativeVideoCardTransitionPhase.Closing -> 1f - easedProgress
    }
}

private fun smoothStep(progress: Float): Float {
    return progress * progress * (3f - 2f * progress)
}
