package com.ytkidssafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytkidssafe.domain.model.Avatars
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Surface

@Composable
fun ProfileAvatar(
    avatar: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(if (selected) 8.dp else 4.dp, CircleShape)
                .clip(CircleShape)
                .background(Surface)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) Primary else Surface,
                    shape = CircleShape
                )
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
        ) {
            Text(
                text = Avatars.getEmoji(avatar),
                fontSize = (size.value * 0.5f).sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
