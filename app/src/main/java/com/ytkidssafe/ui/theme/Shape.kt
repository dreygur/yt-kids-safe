package com.ytkidssafe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),  // Card radius
    large = RoundedCornerShape(24.dp),   // Button radius
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shape values
val CardRadius = 16.dp
val ButtonRadius = 24.dp
val AvatarRadius = 50.dp
val ThumbnailRadius = 12.dp
