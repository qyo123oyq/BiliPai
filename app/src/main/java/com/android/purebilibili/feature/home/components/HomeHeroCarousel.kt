package com.android.purebilibili.feature.home.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.HomeHeroCarouselCardTransform
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselCardTransform
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
internal fun HomeHeroCarousel(
    videos: List<VideoItem>,
    autoplayEnabled: Boolean,
    onVideoClick: (VideoItem) -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    val pagerState = rememberPagerState { videos.size }
    val scope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> videos[page].bvid.ifBlank { "hero_$page" } },
            pageSpacing = 18.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).coerceIn(-1f, 1f)
            val transform = resolveHomeHeroCarouselCardTransform(pageOffset)
            val activeForPlayback = autoplayEnabled &&
                pagerState.currentPage == page &&
                pageOffset.absoluteValue < 0.12f
            HomeHeroCarouselCard(
                video = videos[page],
                transform = transform,
                activeForPlayback = activeForPlayback,
                onVideoClick = { onVideoClick(videos[page]) },
                onGetPreviewUrl = onGetPreviewUrl
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            videos.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 11.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.46f)
                            }
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHeroCarouselArrowButton(
                onClick = {
                    val target = if (pagerState.currentPage == 0) videos.lastIndex else pagerState.currentPage - 1
                    scope.launch { pagerState.animateScrollToPage(target) }
                },
                isPrevious = true
            )
            HomeHeroCarouselArrowButton(
                onClick = {
                    val target = if (pagerState.currentPage == videos.lastIndex) 0 else pagerState.currentPage + 1
                    scope.launch { pagerState.animateScrollToPage(target) }
                },
                isPrevious = false
            )
        }
    }
}

@Composable
private fun HomeHeroCarouselCard(
    video: VideoItem,
    transform: HomeHeroCarouselCardTransform,
    activeForPlayback: Boolean,
    onVideoClick: () -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?
) {
    val density = LocalDensity.current
    var previewUrl by remember(video.bvid, video.cid) { mutableStateOf<String?>(null) }
    LaunchedEffect(activeForPlayback, video.bvid, video.cid) {
        if (activeForPlayback && previewUrl == null && video.bvid.isNotBlank() && video.cid > 0L) {
            previewUrl = onGetPreviewUrl(video.bvid, video.cid)
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.38f)
            .graphicsLayer {
                rotationY = transform.rotationY
                scaleX = transform.scale
                scaleY = transform.scale
                alpha = transform.alpha
                cameraDistance = transform.cameraDistanceMultiplier * density.density * 1000f
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onVideoClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val normalizedCoverUrl = remember(video.pic) { FormatUtils.fixImageUrl(video.pic) }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(normalizedCoverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (activeForPlayback && previewUrl != null) {
                MutedHeroVideoPlayer(url = previewUrl.orEmpty())
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.54f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.76f)
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, end = 120.dp, bottom = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeForPlayback) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCarouselArrowButton(
    onClick: () -> Unit,
    isPrevious: Boolean
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.34f))
    ) {
        Icon(
            imageVector = if (isPrevious) {
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft
            } else {
                Icons.AutoMirrored.Rounded.KeyboardArrowRight
            },
            contentDescription = if (isPrevious) "上一个" else "下一个",
            tint = Color.White
        )
    }
}

@Composable
private fun MutedHeroVideoPlayer(url: String) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }
    LaunchedEffect(url) {
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.prepare()
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
