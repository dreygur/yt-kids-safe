package com.ytkidssafe.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ytkidssafe.ui.theme.Accent1
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Secondary
import com.ytkidssafe.ui.theme.Surface as SurfaceColor
import com.ytkidssafe.ui.theme.TextLight

enum class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val color: Color,
    val visible: Boolean = true
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, Primary),
    CHANNELS("Channels", Icons.Filled.Tv, Icons.Outlined.Tv, Accent1),
    PLAYLISTS("Playlists", Icons.AutoMirrored.Filled.PlaylistPlay, Icons.AutoMirrored.Outlined.PlaylistPlay, Secondary),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, Accent1),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Outlined.Settings, TextLight, visible = false)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavBar(
    currentItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onSettingsLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(SurfaceColor)
            .combinedClickable(
                onClick = { },
                onLongClick = onSettingsLongPress
            )
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main nav items - icons only, evenly spaced with proper touch targets
        NavItem.entries.filter { it.visible }.forEach { item ->
            val selected = currentItem == item
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp) // Ensure minimum touch target
                    .selectable(
                        selected = selected,
                        onClick = { onItemSelected(item) },
                        role = Role.Tab
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) item.color.copy(alpha = 0.2f) else Color.Transparent)
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp),
                        tint = if (selected) item.color else TextLight
                    )
                }
            }
        }
    }
}
