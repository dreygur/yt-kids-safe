# YT Kids Safe - Design Document

## Overview
A parent-controlled YouTube app for kids with full content control.

## Tech Stack
| Component | Choice |
|-----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Local DB | Room |
| Settings | DataStore |
| Video | ExoPlayer + NewPipe Extractor |
| Async | Coroutines + Flow |
| Navigation | Compose Navigation |
| Backup | Kotlinx Serialization (JSON) |
| Min SDK | 26 (Android 8.0) |

---

## Color Palette (Soft Pastels)

| Name | Hex | Usage |
|------|-----|-------|
| Primary | #7EC8E3 | Headers, primary buttons, card accents |
| Secondary | #FF9999 | Bottom nav, secondary actions |
| Background | #FFF9E6 | Main background |
| Accent 1 | #98D89E | Success states, card variants |
| Accent 2 | #FFD166 | Highlights, warnings |
| Text | #4A4A4A | Primary text |
| Text Light | #7A7A7A | Secondary text |

---

## Typography

| Element | Font | Size | Weight |
|---------|------|------|--------|
| App Title | Quicksand | 24sp | Bold |
| Heading | Nunito | 20sp | SemiBold |
| Video Title | Nunito | 16sp | SemiBold |
| Body | Nunito | 14sp | Regular |
| Caption | Nunito | 12sp | Regular |

---

## UI Specs

| Element | Value |
|---------|-------|
| Card radius | 16dp |
| Button radius | 24dp |
| Thumbnail ratio | 16:9 |
| Spacing unit | 16dp |
| Shadow | Soft, 4dp blur, 10% opacity |

---

## Screens

### 1. Profile Selection (Launch)
- Grid of circular profile avatars
- Tap to enter kid mode
- Small settings gear → PIN → parent mode
- Animated background (clouds/stars)

### 2. Kid Home (Primary Screen)
- Top: Greeting + time remaining bar
- Category pills: [All] [Cartoons] [Learning] [Music]
- Single column video cards (full width)
- Bottom nav: Home, Channels, Profile, Settings (hidden)

### 3. Video Card Component
```
┌──────────────────────────────────┐
│                                  │
│          THUMBNAIL 16:9          │
│                         ┌──────┐ │
│                         │ 5:32 │ │
├──────────────────────────┴──────┤
│ Video Title Here                 │
│ 🔴 Channel Name                  │
└──────────────────────────────────┘
```
- Rounded corners (16dp)
- Duration badge bottom-right of thumbnail
- Channel color dot + name

### 4. Channels Screen
- Grid of channel thumbnails
- Tap to filter videos by channel

### 5. Video Player
- Fullscreen playback
- Minimal controls (play/pause, seek)
- No related videos
- Back button always visible
- Time tracking active

### 6. Time's Up Screen
- Friendly message: "Time to take a break!"
- Stars/moon animation
- Positive reinforcement
- Locked until next day or PIN override

### 7. Parent Dashboard (PIN Protected)
- Profile management
- Channel whitelist
- Playlist import
- Time limit settings
- PIN management
- Backup/restore

---

## Navigation

### Bottom Nav (Kid Mode)
| Icon | Label | Action |
|------|-------|--------|
| 🏠 | Home | Video feed |
| 📺 | Channels | Channel list |
| 👤 | Profile | Switch profile |
| ⚙️ | (hidden) | Long press → PIN → Parent mode |

### Parent Access
- Long press settings icon (3 sec)
- Or triple-tap top corner
- PIN prompt appears

---

## Avatars
Cartoon animal faces for kid profiles:
```
🐻 Bear    🦊 Fox     🐰 Bunny   🐼 Panda
🦁 Lion    🐸 Frog    🐱 Cat     🐶 Dog
🦄 Unicorn 🐧 Penguin 🦋 Butterfly 🐵 Monkey
```

---

## Data Models

### Profile
```kotlin
data class Profile(
    val id: String,
    val name: String,
    val avatar: String,
    val dailyLimitMinutes: Int,
    val usedTodayMinutes: Int,
    val lastResetDate: Long,
    val categoryFilters: List<String>
)
```

### Channel
```kotlin
data class Channel(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val category: String,
    val profileIds: List<String>
)
```

### Playlist
```kotlin
data class Playlist(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val profileIds: List<String>
)
```

### Video (Cached)
```kotlin
data class Video(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelId: String,
    val duration: String,
    val cachedAt: Long
)
```

---

## Project Structure
```
app/src/main/java/com/ytkidssafe/
├── YtKidsApp.kt                 # Application class
├── MainActivity.kt
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── VideoModule.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   ├── ProfileEntity.kt
│   │   │   ├── ChannelEntity.kt
│   │   │   ├── PlaylistEntity.kt
│   │   │   └── VideoEntity.kt
│   │   ├── dao/
│   │   │   ├── ProfileDao.kt
│   │   │   ├── ChannelDao.kt
│   │   │   ├── PlaylistDao.kt
│   │   │   └── VideoDao.kt
│   │   └── converter/
│   │       └── Converters.kt
│   ├── datastore/
│   │   └── AppPreferences.kt
│   └── repository/
│       ├── ProfileRepository.kt
│       ├── ChannelRepository.kt
│       ├── VideoRepository.kt
│       └── SettingsRepository.kt
├── domain/
│   ├── model/
│   │   ├── Profile.kt
│   │   ├── Channel.kt
│   │   ├── Playlist.kt
│   │   ├── Video.kt
│   │   └── TimeStatus.kt
│   └── usecase/
│       ├── profile/
│       ├── channel/
│       ├── video/
│       └── settings/
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Theme.kt
│   │   └── Shape.kt
│   ├── components/
│   │   ├── VideoCard.kt
│   │   ├── ChannelTile.kt
│   │   ├── ProfileAvatar.kt
│   │   ├── TimeBar.kt
│   │   ├── CategoryPills.kt
│   │   ├── PinDialog.kt
│   │   └── BottomNavBar.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   ├── screens/
│   │   ├── ProfileSelectScreen.kt
│   │   ├── kid/
│   │   │   ├── KidHomeScreen.kt
│   │   │   ├── KidChannelsScreen.kt
│   │   │   ├── VideoPlayerScreen.kt
│   │   │   └── TimesUpScreen.kt
│   │   └── parent/
│   │       ├── ParentDashboardScreen.kt
│   │       ├── ProfilesScreen.kt
│   │       ├── ChannelsScreen.kt
│   │       ├── PlaylistsScreen.kt
│   │       └── SettingsScreen.kt
│   └── viewmodel/
│       ├── ProfileSelectViewModel.kt
│       ├── KidHomeViewModel.kt
│       ├── VideoPlayerViewModel.kt
│       └── ParentDashboardViewModel.kt
├── video/
│   ├── extractor/
│   │   └── StreamExtractor.kt
│   └── player/
│       ├── KidsPlayerFactory.kt
│       └── KidsPlayerView.kt
├── service/
│   ├── TimeTracker.kt
│   └── TimeResetWorker.kt
└── security/
    └── PinManager.kt
```

---

## App Flow
```
App Launch
    │
    ▼
Profile Select ───────────────────┐
    │                             │
    ▼                             │
Kid Home (Primary) ◄──────────────┤
    │                             │
    ├── Tap video → Player        │
    ├── Channels → Channel list   │
    ├── Profile → Switch profile ─┘
    └── Long press ⚙️ → PIN → Parent Dashboard
```

---

## Time Tracking Logic
1. On video play: start timer
2. On video pause/exit: stop timer, save elapsed
3. Check remaining time before playing
4. If time exhausted: show Time's Up screen
5. Reset daily at midnight (via WorkManager)
6. Parent can override via PIN

---

## Backup Format (JSON)
```json
{
  "version": 1,
  "exportedAt": "2024-01-15T10:30:00Z",
  "profiles": [...],
  "channels": [...],
  "playlists": [...],
  "settings": {
    "pinHash": "...",
    "defaultDailyLimit": 60
  }
}
```
