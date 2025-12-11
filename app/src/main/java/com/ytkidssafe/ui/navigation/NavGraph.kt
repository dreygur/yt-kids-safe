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
import com.ytkidssafe.ui.screens.kid.KidChannelVideosScreen
import com.ytkidssafe.ui.screens.kid.KidHomeScreen
import com.ytkidssafe.ui.screens.kid.KidPlaylistsScreen
import com.ytkidssafe.ui.screens.kid.KidPlaylistVideosScreen
import com.ytkidssafe.ui.screens.kid.TimesUpScreen
import com.ytkidssafe.ui.screens.kid.VideoPlayerScreen
import com.ytkidssafe.ui.screens.parent.CategoriesScreen
import com.ytkidssafe.ui.screens.parent.ChannelsScreen
import com.ytkidssafe.ui.screens.parent.ParentDashboardScreen
import com.ytkidssafe.ui.screens.parent.PlaylistsScreen
import com.ytkidssafe.ui.screens.parent.ProfilesScreen
import com.ytkidssafe.ui.screens.parent.SettingsScreen

object Routes {
    const val PROFILE_SELECT = "profile_select?autoSkip={autoSkip}"
    const val PROFILE_SELECT_MANUAL = "profile_select?autoSkip=false"
    const val KID_HOME = "kid_home/{profileId}"
    const val KID_CHANNELS = "kid_channels/{profileId}"
    const val KID_CHANNEL_VIDEOS = "kid_channel_videos/{profileId}/{channelId}"
    const val KID_PLAYLISTS = "kid_playlists/{profileId}"
    const val KID_PLAYLIST_VIDEOS = "kid_playlist_videos/{profileId}/{playlistId}"
    const val VIDEO_PLAYER = "video_player/{profileId}/{videoId}"
    const val TIMES_UP = "times_up/{profileId}"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val PARENT_PROFILES = "parent_profiles"
    const val PARENT_CHANNELS = "parent_channels"
    const val PARENT_PLAYLISTS = "parent_playlists"
    const val PARENT_CATEGORIES = "parent_categories"
    const val PARENT_SETTINGS = "parent_settings"

    fun kidHome(profileId: String) = "kid_home/$profileId"
    fun kidChannels(profileId: String) = "kid_channels/$profileId"
    fun kidChannelVideos(profileId: String, channelId: String) = "kid_channel_videos/$profileId/$channelId"
    fun kidPlaylists(profileId: String) = "kid_playlists/$profileId"
    fun kidPlaylistVideos(profileId: String, playlistId: String) = "kid_playlist_videos/$profileId/$playlistId"
    fun videoPlayer(profileId: String, videoId: String) = "video_player/$profileId/$videoId"
    fun timesUp(profileId: String) = "times_up/$profileId"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "profile_select?autoSkip=true"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Profile Selection
        composable(
            route = Routes.PROFILE_SELECT,
            arguments = listOf(
                navArgument("autoSkip") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val autoSkip = backStackEntry.arguments?.getBoolean("autoSkip") ?: true
            ProfileSelectScreen(
                onProfileSelected = { profileId ->
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo("profile_select?autoSkip=true") { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                },
                onAddProfile = {
                    navController.navigate(Routes.PARENT_PROFILES)
                },
                autoSkipIfSingle = autoSkip
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
                onPlaylistsClick = {
                    navController.navigate(Routes.kidPlaylists(profileId))
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT_MANUAL) {
                        popUpTo(0) { inclusive = true }
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
                    navController.navigate(Routes.kidChannelVideos(profileId, channelId))
                },
                onHomeClick = {
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo(Routes.kidHome(profileId)) { inclusive = true }
                    }
                },
                onPlaylistsClick = {
                    navController.navigate(Routes.kidPlaylists(profileId)) {
                        popUpTo(Routes.kidHome(profileId))
                    }
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT_MANUAL) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                }
            )
        }

        // Kid Channel Videos
        composable(
            route = Routes.KID_CHANNEL_VIDEOS,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("channelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            val channelId = backStackEntry.arguments?.getString("channelId") ?: return@composable
            KidChannelVideosScreen(
                profileId = profileId,
                channelId = channelId,
                onVideoClick = { videoId ->
                    navController.navigate(Routes.videoPlayer(profileId, videoId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Kid Playlists
        composable(
            route = Routes.KID_PLAYLISTS,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            KidPlaylistsScreen(
                profileId = profileId,
                onPlaylistClick = { playlistId ->
                    navController.navigate(Routes.kidPlaylistVideos(profileId, playlistId))
                },
                onHomeClick = {
                    navController.navigate(Routes.kidHome(profileId)) {
                        popUpTo(Routes.kidHome(profileId)) { inclusive = true }
                    }
                },
                onChannelsClick = {
                    navController.navigate(Routes.kidChannels(profileId)) {
                        popUpTo(Routes.kidHome(profileId))
                    }
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT_MANUAL) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD)
                }
            )
        }

        // Kid Playlist Videos
        composable(
            route = Routes.KID_PLAYLIST_VIDEOS,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
                navArgument("playlistId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            KidPlaylistVideosScreen(
                profileId = profileId,
                playlistId = playlistId,
                onVideoClick = { videoId ->
                    navController.navigate(Routes.videoPlayer(profileId, videoId))
                },
                onBack = { navController.popBackStack() }
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
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchProfile = {
                    navController.navigate(Routes.PROFILE_SELECT_MANUAL) {
                        popUpTo(0) { inclusive = true }
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
                onNavigateToCategories = { navController.navigate(Routes.PARENT_CATEGORIES) },
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

        // Parent Categories
        composable(Routes.PARENT_CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }

        // Parent Settings
        composable(Routes.PARENT_SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
