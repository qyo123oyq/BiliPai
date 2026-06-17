package com.android.purebilibili.feature.home

internal const val HOME_HERO_CAROUSEL_MAX_ITEMS = 8

internal data class HomeHeroCarouselCardTransform(
    val rotationY: Float,
    val scale: Float,
    val alpha: Float,
    val cameraDistanceMultiplier: Float
)

internal fun <T> selectHomeHeroCarouselItems(
    items: List<T>,
    maxItems: Int = HOME_HERO_CAROUSEL_MAX_ITEMS
): List<T> {
    if (maxItems <= 0) return emptyList()
    return items.take(maxItems)
}

internal fun shouldShowHomeHeroCarousel(
    enabled: Boolean,
    category: HomeCategory,
    itemCount: Int
): Boolean {
    return enabled && category == HomeCategory.RECOMMEND && itemCount > 0
}

internal fun resolveHomeHeroCarouselCardTransform(
    pageOffset: Float
): HomeHeroCarouselCardTransform {
    val clampedOffset = pageOffset.coerceIn(-1f, 1f)
    val distance = kotlin.math.abs(clampedOffset)
    return HomeHeroCarouselCardTransform(
        rotationY = -clampedOffset * 46f,
        scale = 1f - distance * 0.08f,
        alpha = 1f - distance * 0.24f,
        cameraDistanceMultiplier = 18f
    )
}
