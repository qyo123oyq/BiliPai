package com.android.purebilibili.core.ui.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.videoCardShellSharedBoundsOrEmpty(
    enabled: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    bvid: String,
    sourceRoute: String?,
    motionSpec: VideoSharedTransitionMotionSpec,
    clipShape: Shape
): Modifier {
    if (!enabled || sharedTransitionScope == null || animatedVisibilityScope == null || bvid.isBlank()) {
        return this
    }
    return then(
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = videoCardShellSharedElementKey(
                        bvid = bvid,
                        sourceRoute = sourceRoute
                    )
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    if (motionSpec.enabled) {
                        tween(
                            durationMillis = motionSpec.durationMillis,
                            easing = motionSpec.easing
                        )
                    } else {
                        com.android.purebilibili.core.ui.motion.AppMotionTokens.spatialSpec()
                    }
                },
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
        }
    )
}
