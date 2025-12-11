package com.ytkidssafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.ui.theme.Accent1
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Surface
import com.ytkidssafe.ui.theme.TextLight

@Composable
fun PlaylistTile(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Thumbnail with rounded corners
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .background(Accent1.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .border(3.dp, Accent1, RoundedCornerShape(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = playlist.thumbnailUrl,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(140.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface),
                loading = {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .background(Surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = "Playlist",
                            tint = Accent1.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(140.dp)
        )

        if (playlist.videoCount > 0) {
            Text(
                text = "${playlist.videoCount} videos",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight,
                textAlign = TextAlign.Center
            )
        }
    }
}
