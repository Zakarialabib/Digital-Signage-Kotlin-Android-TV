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
enum class CarouselTransitionType {
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL
}