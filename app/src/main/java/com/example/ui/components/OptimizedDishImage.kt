package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale

/**
 * High-performance optimized image loader with:
 * - Dynamic URL image compression (Unsplash auto-formatting & query optimization)
 * - Strict In-memory & Disk cache policies to prevent redundant network hits in high traffic
 * - Subcompose lazy loading with low-overhead shimmering placeholders
 * - Fallback to dish emoji on network failure to avoid crashes
 */
@Composable
fun OptimizedDishImage(
    imageUrl: String,
    fallbackEmoji: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    targetWidthPx: Int = 300,
    targetHeightPx: Int = 300,
    shape: Shape? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // Compress Unsplash & standard image URLs by optimizing quality and width
    val optimizedUrl = remember(imageUrl, targetWidthPx) {
        compressImageUrl(imageUrl, targetWidthPx)
    }

    val imageRequest = remember(optimizedUrl, targetWidthPx, targetHeightPx) {
        ImageRequest.Builder(context)
            .data(optimizedUrl)
            .crossfade(200)
            .precision(Precision.INEXACT)
            .scale(Scale.FILL)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    val boxModifier = if (shape != null) modifier.clip(shape) else modifier

    Box(
        modifier = boxModifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (optimizedUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                },
                error = {
                    // Fallback gracefully to dish emoji on connection failure or bad URL
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fallbackEmoji.ifBlank { "🍲" },
                            fontSize = (targetHeightPx / 10).coerceIn(18, 36).sp
                        )
                    }
                }
            )
        } else {
            // Default emoji display when no URL is provided
            Text(
                text = fallbackEmoji.ifBlank { "🍲" },
                fontSize = (targetHeightPx / 10).coerceIn(18, 36).sp
            )
        }
    }
}

/**
 * Optimizes image URLs by appending compression and dynamic resizing parameters
 * to reduce bandwidth consumption under high concurrent traffic.
 */
fun compressImageUrl(rawUrl: String, targetWidth: Int): String {
    if (rawUrl.isBlank()) return ""
    return if (rawUrl.contains("images.unsplash.com")) {
        // Strip out existing dimension parameters and insert compressed params
        val base = rawUrl.substringBefore("?")
        "$base?w=$targetWidth&auto=format&fit=crop&q=70"
    } else {
        rawUrl
    }
}
