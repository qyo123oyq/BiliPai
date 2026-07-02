package com.android.purebilibili.core.ui.transition.native

import kotlin.test.Test
import kotlin.test.assertTrue

class NativeVideoCardTransitionPolicyTest {

    private val spec = NativeVideoCardTransitionSpec(maxBlurRadiusPx = 28f)

    @Test
    fun api31ReturnBackgroundBlurClearsAsHomeBecomesVisible() {
        val start = resolveNativeVideoCardTransitionFrame(
            spec = spec,
            progress = 0f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 31
        )
        val middle = resolveNativeVideoCardTransitionFrame(
            spec = spec,
            progress = 0.5f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 31
        )
        val end = resolveNativeVideoCardTransitionFrame(
            spec = spec,
            progress = 1f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 31
        )

        assertTrue(start.blurRadiusPx > middle.blurRadiusPx)
        assertTrue(middle.blurRadiusPx > end.blurRadiusPx)
        assertTrue(start.scrimAlpha > middle.scrimAlpha)
        assertTrue(middle.scrimAlpha > end.scrimAlpha)
    }

    @Test
    fun nativeFrameOnlyDescribesBlurAndScrim() {
        val middle = resolveNativeVideoCardTransitionFrame(
            spec = spec,
            progress = 0.5f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 31
        )

        assertTrue(middle.blurRadiusPx > 0f)
        assertTrue(middle.scrimAlpha > 0f)
    }

    @Test
    fun api30KeepsBlurDisabledButStillAppliesScrim() {
        val middle = resolveNativeVideoCardTransitionFrame(
            spec = spec,
            progress = 0.5f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 30
        )

        kotlin.test.assertEquals(0f, middle.blurRadiusPx)
        assertTrue(middle.scrimAlpha > 0f)
    }

    @Test
    fun defaultMidProgressKeepsBackgroundBlurAndScrimVisible() {
        val middle = resolveNativeVideoCardTransitionFrame(
            spec = NativeVideoCardTransitionSpec(
                maxBlurRadiusPx = NATIVE_VIDEO_CARD_TRANSITION_MAX_BLUR_RADIUS_PX
            ),
            progress = 0.5f,
            phase = NativeVideoCardTransitionPhase.Closing,
            sdkInt = 35
        )

        assertTrue(middle.blurRadiusPx > 0f)
        assertTrue(middle.scrimAlpha > 0f)
    }
}
