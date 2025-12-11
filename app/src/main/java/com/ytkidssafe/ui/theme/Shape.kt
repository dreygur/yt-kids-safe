package com.ytkidssafe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),  // Card radius - YouTube Kids style
    large = RoundedCornerShape(32.dp),   // Button radius
    extraLarge = RoundedCornerShape(40.dp)
)

// Custom shape values - YouTube Kids inspired (rounder, more playful)
val CardRadius = 24.dp
val ButtonRadius = 32.dp
val AvatarRadius = 50.dp
val ThumbnailRadius = 20.dp
val BadgeRadius = 12.dp
