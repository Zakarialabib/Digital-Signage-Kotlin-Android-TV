package com.signagepro.app.core.data.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing content that can be displayed in the player.
 */
@Serializable
sealed class Content {
    abstract val id: String
    abstract val name: String
    abstract val description: String?
    abstract val type: ContentType
    abstract val duration: Int // Duration in seconds, 0 for indefinite
    abstract val lastAccessed: Long

    @Serializable
    data class Image(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String,
        val localPath: String? = null,
        val scaleType: ImageScaleType = ImageScaleType.FIT_CENTER,
        override val duration: Int = 10,
        val aspectRatio: Float? = null,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.IMAGE
    }

    @Serializable
    data class Video(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String,
        val localPath: String? = null,
        val autoPlay: Boolean = true,
        val loop: Boolean = false,
        val muted: Boolean = false,
        override val duration: Int = 0, // 0 means use video's actual duration
        val aspectRatio: Float? = null,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.VIDEO
    }

    @Serializable
    data class Audio(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String,
        val localPath: String? = null,
        val autoPlay: Boolean = true,
        val loop: Boolean = false,
        override val duration: Int = 0, // 0 means use audio's actual duration
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.AUDIO
    }

    @Serializable
    data class Web(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String,
        override val duration: Int = 30,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.WEB
    }

    @Serializable
    data class Text(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val text: String,
        val textStyle: TextStyle = TextStyle(),
        override val duration: Int = 10,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.TEXT
    }

    @Serializable
    data class LiveStream(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String,
        override val duration: Int = 0, // 0 means indefinite
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.LIVE_STREAM
    }

    @Serializable
    data class Html(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val htmlContent: String, // Can be inline HTML or a URL to a .html file
        val baseUrl: String? = null, // For resolving relative paths if htmlContent is inline
        override val duration: Int = 30,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.HTML
    }

    @Serializable
    data class WebPage(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val url: String, // Similar to Web, but explicitly for a full webpage render
        override val duration: Int = 30,
        val enableJavaScript: Boolean = true,
        val userAgent: String? = null,
        val metadata: Map<String, String> = emptyMap(),
        override val lastAccessed: Long = System.currentTimeMillis()
    ) : Content() {
        override val type: ContentType = ContentType.WEBPAGE
    }

    @Serializable
    data class Playlist(
        override val id: String,
        override val name: String,
        override val description: String? = null,
        val items: List<Content>,
        override val duration: Int = 0, // Duration might be sum of items or fixed, 0 for dynamic
        val currentItemIndex: Int = 0,
        val loopMode: PlaylistLoopMode = PlaylistLoopMode.NONE,
        override val lastAccessed: Long = System.currentTimeMillis(),
        val metadata: Map<String, String> = emptyMap()
    ) : Content() {
        override val type: ContentType = ContentType.PLAYLIST
    }
}

@Serializable
enum class ContentType {
    IMAGE,
    VIDEO,
    AUDIO,
    WEB,
    TEXT,
    HTML,
    CAROUSEL,
    WEBPAGE,
    PLAYLIST,
    LIVE_STREAM,
    UNKNOWN
}

@Serializable
enum class CacheStatus {
    NOT_CACHED,
    CACHING,
    CACHED,
    ERROR
}

@Serializable
data class TextStyle(
    val fontSize: Int = 16,
    val fontColor: String = "#000000",
    val backgroundColor: String = "#FFFFFF",
    val alignment: TextAlignment = TextAlignment.CENTER,
    val fontWeight: FontWeight = FontWeight.NORMAL
)

@Serializable
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

@Serializable
enum class FontWeight {
    NORMAL,
    BOLD,
    LIGHT
}

@Serializable
enum class ImageScaleType {
    FIT_CENTER,
    CENTER_CROP,
    FILL_BOUNDS
}

@Serializable
enum class PlaylistLoopMode {
    NONE, // Play once
    LOOP_LIST, // Loop entire list
    LOOP_ITEM // Loop current item (less common for playlist, more for single content)
}

@Serializable
enum class CarouselTransitionType {
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL
}