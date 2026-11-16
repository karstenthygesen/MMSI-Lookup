package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BridgeRedLuminous
import com.example.ui.theme.BridgeRedOutline
import com.example.ui.theme.BridgeRedSurface
import com.example.ui.theme.BridgeRedSurfaceVariant
import com.example.ui.theme.LocalBridgeNightMode
import com.example.ui.theme.MaritimeCyan

val RedMonochromeMatrix = ColorMatrix(
    floatArrayOf(
        0.30f, 0.59f, 0.11f, 0f, 0f,
        0f,    0f,    0f,    0f, 0f,
        0f,    0f,    0f,    0f, 0f,
        0f,    0f,    0f,    1f, 0f
    )
)

/**
 * Large, high-visibility maritime flag badge.
 * Displays the country flag in both day and night mode to allow mariners
 * to visually validate vessel ensigns at sea.
 * In night mode, defaults to deep Red / Black monochrome to protect night vision.
 */
@Composable
fun FlagBadge(
    flagEmoji: String,
    isoCode: String,
    badgeSize: Dp = 96.dp,
    allowMonochromeToggle: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isBridgeNight = LocalBridgeNightMode.current
    // In night mode, default is Red / Black mode
    var isRedMonochrome by remember(isBridgeNight) { mutableStateOf(isBridgeNight) }

    val redPaint = remember {
        Paint().apply {
            colorFilter = ColorFilter.colorMatrix(RedMonochromeMatrix)
        }
    }

    Column(
        modifier = modifier.width(badgeSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .shadow(if (isBridgeNight) 4.dp else 6.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isBridgeNight) {
                            listOf(
                                BridgeRedSurfaceVariant,
                                BridgeRedSurface
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = if (isBridgeNight) {
                            listOf(
                                BridgeRedLuminous.copy(alpha = 0.85f),
                                BridgeRedOutline
                            )
                        } else {
                            listOf(
                                MaritimeCyan.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(enabled = isBridgeNight && allowMonochromeToggle) {
                    isRedMonochrome = !isRedMonochrome
                }
                .testTag("flag_badge"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (isBridgeNight && isRedMonochrome) {
                            Modifier.drawWithContent {
                                drawIntoCanvas { canvas ->
                                    canvas.saveLayer(Rect(-4f, -4f, size.width + 4f, size.height + 4f), redPaint)
                                    drawContent()
                                    canvas.restore()
                                }
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Large flag emoji displayed in BOTH day and night modes
                Text(
                    text = flagEmoji.ifEmpty { "🏳️" },
                    fontSize = (badgeSize.value * 0.65).sp,
                    lineHeight = (badgeSize.value * 0.65).sp,
                    modifier = Modifier.testTag("flag_emoji_text")
                )
            }
        }

        // In night mode: toggle indicator under the badge
        if (isBridgeNight && allowMonochromeToggle) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isRedMonochrome) BridgeRedLuminous.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRedMonochrome) BridgeRedLuminous.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isRedMonochrome = !isRedMonochrome }
                    .testTag("monochrome_flag_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isRedMonochrome) BridgeRedLuminous else MaritimeCyan,
                                shape = CircleShape
                            )
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (isRedMonochrome) "RED / BLACK" else "TRUE COLOR",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = if (isRedMonochrome) BridgeRedLuminous else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Small flag display component for lists, recent lookups, and chips.
 * In Bridge Night Mode, automatically applies the red/black monochrome filter
 * to maintain strict bridge night vision compliance.
 */
@Composable
fun SmallFlag(
    flagEmoji: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    forceColor: Boolean = false
) {
    val isBridgeNight = LocalBridgeNightMode.current
    val shouldFilter = isBridgeNight && !forceColor

    val redPaint = remember {
        Paint().apply {
            colorFilter = ColorFilter.colorMatrix(RedMonochromeMatrix)
        }
    }

    Box(
        modifier = modifier.then(
            if (shouldFilter) {
                Modifier.drawWithContent {
                    drawIntoCanvas { canvas ->
                        canvas.saveLayer(Rect(-4f, -4f, size.width + 4f, size.height + 4f), redPaint)
                        drawContent()
                        canvas.restore()
                    }
                }
            } else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flagEmoji.ifEmpty { "🏳️" },
            fontSize = fontSize,
            lineHeight = fontSize
        )
    }
}

