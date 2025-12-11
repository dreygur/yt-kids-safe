package com.ytkidssafe.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Secondary
import com.ytkidssafe.ui.theme.Surface
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.theme.TextPrimary

enum class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CHANNELS("Channels", Icons.Filled.Tv, Icons.Outlined.Tv),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavBar(
    currentItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onSettingsLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Surface,
        modifier = modifier.fillMaxWidth()
    ) {
        NavItem.entries.forEach { item ->
            if (item == NavItem.SETTINGS) {
                // Settings is hidden but accessible via long press
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(24.dp)
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = onSettingsLongPress
                                ),
                            tint = TextLight.copy(alpha = 0.3f)
                        )
                    },
                    label = { },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Surface
                    )
                )
            } else {
                NavigationBarItem(
                    selected = currentItem == item,
                    onClick = { onItemSelected(item) },
                    icon = {
                        Icon(
                            imageVector = if (currentItem == item) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = TextLight,
                        unselectedTextColor = TextLight,
                        indicatorColor = Secondary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
