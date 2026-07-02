package com.android.purebilibili.core.ui.transition.native

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeVideoCardTransitionControllerStructureTest {

    @Test
    fun nativeTransitionNoLongerBlocksOpenNavigation() {
        val source = loadSource()

        assertFalse(source.contains("fun startOpen("))
        assertFalse(source.contains("NativeVideoCardTransitionOpenRequest"))
        assertFalse(source.contains("OPEN_NAVIGATION_HANDOFF_DELAY_MS"))
    }

    @Test
    fun predictiveCloseCanPreviewFinishAndCancelWithoutStartingFromZero() {
        val source = loadSource()

        assertTrue(source.contains("fun previewClose("))
        assertTrue(source.contains("fun finishPreviewClose("))
        assertTrue(source.contains("fun cancelPreviewClose("))
        assertTrue(source.contains("startProgress = previewCloseProgress"))
    }

    @Test
    fun nativeTransitionRendersOnlyBackgroundEffects() {
        val controllerSource = loadSource()
        val overlaySource = loadOverlaySource()

        assertFalse(controllerSource.contains("coverUrl"))
        assertFalse(controllerSource.contains("loadCoverBitmap("))
        assertFalse(controllerSource.contains("setCoverBitmap("))
        assertFalse(overlaySource.contains("coverBitmap"))
        assertFalse(overlaySource.contains("drawCover("))
        assertFalse(overlaySource.contains("coverAlpha"))
        assertFalse(overlaySource.contains("cardPaint"))
        assertFalse(overlaySource.contains("drawRoundRect"))
        assertFalse(overlaySource.contains("cardRect"))
    }

    private fun loadSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionController.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionController.kt")
        ).first { it.exists() }.readText()
    }

    private fun loadOverlaySource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionOverlayView.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionOverlayView.kt")
        ).first { it.exists() }.readText()
    }
}
