package com.ytkidssafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ytkidssafe.ui.screens.ProfileSelectScreen
import com.ytkidssafe.ui.screens.kid.KidChannelsScreen
import com.ytkidssafe.ui.screens.kid.KidHomeScreen
import com.ytkidssafe.ui.screens.kid.TimesUpScreen
import com.ytkidssafe.ui.screens.kid.VideoPlayerScreen
import com.ytkidssafe.ui.screens.parent.ChannelsScreen
import com.ytkidssafe.ui.screens.parent.ParentDashboardScreen
import com.ytkidssafe.ui.screens.parent.PlaylistsScreen
import com.ytkidssafe.ui.screens.parent.ProfilesScreen
import com.ytkidssafe.ui.screens.parent.SettingsScreen

object Routes {
    const val PROFILE_SELECT = "profile_select"
    const val KID_HOME = "kid_home/{profileId}"
    const val KID_CHANNELS = "kid_channels/{profileId}"
    const val VIDEO_PLAYER = "video_player/{profileId}/{videoId}"
    const val TIMES_UP = "times_up/{profileId}"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val PARENT_PROFILES = "parent_profiles"
    const val PARENT_CHANNELS = "parent_channels"
    const val PARENT_PLAYLISTS = "parent_playlists"
    const val PARENT_SETTINGS = "parent_settings"

    fun kidHome(profileId: String) = "kid_home/$profileId"
    fun kidChannels(profileId: String) = "kid_channels/$profileId"
    fun videoPlayer(profileId: String, videoId: String) = "video_player/$profileId/$videoId"
    fun timesUp(profileId: String) = "times_up/$profileId"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.PROFILE_SELECT
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Profile Selection
        composable(Routes.PROFILE_SELECT) {
            ProfileSelectScreen(
                onProfileSelected = { profileId ->
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo(Routes.PROFILE_SELECT)
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                }
            )
        }

        // Kid Home
        composable(
            route = Routes.KID_HOME,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            KidHomeScreen(
                profileId = profileId,
                onVideoClick = { videoId ->
                    navController.navigate(Routes.videoPlayer(profileId, videoId))
                },
                onChannelsClick = {
                    navController.navigate(Routes.kidChannels(profileId))
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT) {
                        popUpTo(Routes.PROFILE_SELECT) { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                },
                onTimeUp = {
                    navController.navigate(Routes.timesUp(profileId)) {
                        popUpTo(Routes.kidHome(profileId)) { inclusive = true }
                    }
                }
            )
        }

        // Kid Channels
        composable(
            route = Routes.KID_CHANNELS,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            KidChannelsScreen(
                profileId = profileId,
                onChannelClick = { channelId ->
                    // TODO: Filter videos by channel
                },
                onHomeClick = {
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo(Routes.kidHome(profileId)) { inclusive = true }
                    }
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT) {
                        popUpTo(Routes.PROFILE_SELECT) { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                }
            )
        }

        // Video Player
        composable(
            route = Routes.VIDEO_PLAYER,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("videoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
            VideoPlayerScreen(
                profileId = profileId,
                videoId = videoId,
                onBack = { navController.popBackStack() },
                onTimeUp = {
                    navController.navigate(Routes.timesUp(profileId)) {
                        popUpTo(Routes.kidHome(profileId)) { inclusive = true }
                    }
                }
            )
        }

        // Times Up
        composable(
            route = Routes.TIMES_UP,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            TimesUpScreen(
                profileId = profileId,
                onParentOverride = {
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo(Routes.PROFILE_SELECT)
                    }
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT) {
                        popUpTo(Routes.PROFILE_SELECT) { inclusive = true }
                    }
                }
            )
        }

        // Parent Dashboard
        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                onNavigateToProfiles = { navController.navigate(Routes.PARENT_PROFILES) },
                onNavigateToChannels = { navController.navigate(Routes.PARENT_CHANNELS) },
                onNavigateToPlaylists = { navController.navigate(Routes.PARENT_PLAYLISTS) },
                onNavigateToSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                onBack = { navController.popBackStack() }
            )
        }

        // Parent Profiles
        composable(Routes.PARENT_PROFILES) {
            ProfilesScreen(onBack = { navController.popBackStack() })
        }

        // Parent Channels
        composable(Routes.PARENT_CHANNELS) {
            ChannelsScreen(onBack = { navController.popBackStack() })
        }

        // Parent Playlists
        composable(Routes.PARENT_PLAYLISTS) {
            PlaylistsScreen(onBack = { navController.popBackStack() })
        }

        // Parent Settings
        composable(Routes.PARENT_SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
