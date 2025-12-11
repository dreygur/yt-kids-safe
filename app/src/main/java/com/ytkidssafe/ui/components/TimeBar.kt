package com.ytkidssafe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ytkidssafe.domain.model.TimeStatus
import com.ytkidssafe.ui.theme.Accent1
import com.ytkidssafe.ui.theme.Accent2
import com.ytkidssafe.ui.theme.Error
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight

@Composable
fun TimeBar(
    timeStatus: TimeStatus,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = 1f - timeStatus.percentage,
        label = "progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            timeStatus.isTimeUp -> Error
            timeStatus.isLowTime -> Accent2
            timeStatus.percentage > 0.7f -> Accent2
            else -> Accent1
        },
        label = "progressColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = "${timeStatus.formattedRemaining} remaining",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${timeStatus.usedMinutes}/${timeStatus.totalMinutes}m",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(progressColor)
            )
        }
    }
}
