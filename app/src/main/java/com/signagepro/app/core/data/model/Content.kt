package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Content {
    abstract val id: String
    abstract val type: ContentType
    abstract val duration: Int // Duration in seconds, 0 for indefinite

    @Serializable
    data class Image(
        override val id: String,
        val url: String,
        val scaleType: ImageScaleType = ImageScaleType.FIT_CENTER,
        override val duration: Int
    ) : Content() {
        override val type: ContentType = ContentType.IMAGE
    }

    @Serializable
    data class Video(
        override val id: String,
        val url: String,
        val autoPlay: Boolean = true,
        val loop: Boolean = false,
        val muted: Boolean = false,
        override val duration: Int // Video duration might be determined by media itself, but this can be an override
    ) : Content() {
        override val type: ContentType = ContentType.VIDEO
    }

    @Serializable
    data class Html(
        override val id: String,
        val url: String? = null,
        val rawHtml: String? = null,
        override val duration: Int
    ) : Content() {
        override val type: ContentType = ContentType.HTML
    }

    @Serializable
    data class Carousel(
        override val id: String,
        val items: List<Content>,
        val transitionType: CarouselTransitionType = CarouselTransitionType.FADE,
        val itemDuration: Int, // Default duration for items if not specified individually
        override val duration: Int // Total duration for the carousel, if 0, it's sum of items
    ) : Content() {
        override val type: ContentType = ContentType.CAROUSEL
    }

    @Serializable
    data class WebPage(
        override val id: String,
        val url: String,
        override val duration: Int
    ) : Content() {
        override val type: ContentType = ContentType.WEBPAGE
    }

    @Serializable
    data class Playlist(
        override val id: String,
        val items: List<Content>,
        override val duration: Int // Total duration for the playlist, if 0, it's sum of items
    ) : Content() {
        override val type: ContentType = ContentType.PLAYLIST
    }
}

@Serializable
enum class ContentType {
    IMAGE,
    VIDEO,
    HTML,
    CAROUSEL,
    WEBPAGE,
    PLAYLIST
}

@Serializable
enum class ImageScaleType {
    FIT_CENTER,
    CENTER_CROP,
    FILL_BOUNDS
}

@Serializable
enum class CarouselTransitionType {
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL
}