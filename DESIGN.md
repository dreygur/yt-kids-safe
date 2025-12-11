# YT Kids Safe - Design Document

## Overview
A parent-controlled YouTube app for kids with full content control.

## Tech Stack
| Component | Choice |
|-----------|--------|
| Framework | Flutter |
| Local DB | Hive |
| Settings | SharedPreferences |
| Video | YouTube Data API + youtube_player_flutter |
| Backup | JSON export/import |

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
| Card radius | 16px |
| Button radius | 24px |
| Thumbnail ratio | 16:9 |
| Spacing unit | 16px |
| Shadow | Soft, 4px blur, 10% opacity |

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
- Rounded corners (16px)
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
```dart
class Profile {
  String id;
  String name;
  String avatar;
  int dailyLimitMinutes;
  int usedTodayMinutes;
  DateTime lastResetDate;
  List<String> categoryFilters;
}
```

### Channel
```dart
class Channel {
  String id;
  String youtubeId;
  String title;
  String thumbnail;
  String category;
  List<String> profileIds;
}
```

### Playlist
```dart
class Playlist {
  String id;
  String youtubeId;
  String title;
  String thumbnail;
  List<String> profileIds;
}
```

### Video (Cached)
```dart
class Video {
  String id;
  String youtubeId;
  String title;
  String thumbnail;
  String channelId;
  String duration;
  DateTime cachedAt;
}
```

---

## Project Structure
```
lib/
├── main.dart
├── app.dart
├── config/
│   ├── theme.dart
│   └── constants.dart
├── models/
│   ├── profile.dart
│   ├── channel.dart
│   ├── playlist.dart
│   └── video.dart
├── services/
│   ├── youtube_service.dart
│   ├── storage_service.dart
│   └── time_tracker.dart
├── screens/
│   ├── profile_select_screen.dart
│   ├── kid/
│   │   ├── home_screen.dart
│   │   ├── channels_screen.dart
│   │   ├── player_screen.dart
│   │   └── times_up_screen.dart
│   └── parent/
│       ├── dashboard_screen.dart
│       ├── profiles_screen.dart
│       ├── channels_screen.dart
│       ├── playlists_screen.dart
│       └── settings_screen.dart
├── widgets/
│   ├── video_card.dart
│   ├── channel_tile.dart
│   ├── profile_avatar.dart
│   ├── time_bar.dart
│   ├── category_pills.dart
│   └── pin_dialog.dart
└── utils/
    ├── youtube_parser.dart
    └── backup_helper.dart
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
5. Reset daily at midnight (or configurable time)
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
