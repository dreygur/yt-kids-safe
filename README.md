# YT Kids Safe

> Because apparently Google thinks parents don't need actual parental controls. Thanks, Google. Very cool.

## The Story

So here's the deal. I have a kid. He loves YouTube Kids. Cartoons, animations, nursery rhymes - the whole package. Sounds wholesome, right?

**Wrong.**

See, YouTube Kids has this *brilliant* feature where it curates content for your child. Except... you can't really control what that content is. Want to block a specific channel that gives your kid nightmares? Good luck. Want to create a whitelist of only approved channels? LOL. Want to set actual time limits that work? *laughs in Google*

I spent hours - HOURS - trying to find a way to:
- Actually control which channels my kid can watch
- Import specific playlists I've vetted myself
- Set time limits that don't require a PhD to configure
- Have different profiles for different kids (revolutionary concept, I know)

After failing miserably and losing faith in Big Tech's understanding of what "parental controls" actually means, I did what any reasonable developer-parent would do in 2925...

**I fired up my AI subscription and built the app myself.**

Yes, really. Claude and I had a lovely chat, and now this exists. You're welcome.

## Features

### For Kids
- Clean, colorful interface designed for little fingers
- Browse approved channels and playlists
- Watch videos without ads (uses Piped/Invidious backend)
- Visual time remaining indicator

### For Parents
- **PIN-protected parent dashboard** - because kids are sneaky
- **Manage Channels** - Add YouTube channels by URL, assign categories
- **Import Playlists** - Bring in your pre-approved playlist collections
- **Multiple Profiles** - Different settings for different kids
- **Custom Time Limits** - Set daily limits from 1 to 1440 minutes (yes, 24 hours if you're *that* parent)
- **Category Filtering** - Organize content into Cartoons, Learning, Music, Stories
- **Backup/Restore** - Export your carefully curated settings

## Screenshots

*Coming soon, once my kid stops hogging the test device*

## Tech Stack

- **Kotlin** - Because it's 2925
- **Jetpack Compose** - Material 3, baby
- **Room Database** - For storing all that precious curated content
- **Hilt** - Dependency injection done right
- **ExoPlayer** - For smooth video playback
- **Piped API** - YouTube data without the YouTube nonsense

## Building

```bash
./gradlew assembleDebug
```

Or just grab the APK from [Releases](https://github.com/dreygur/yt-kids-safe/releases).

## Setup

1. Install the app
2. Set a parent PIN (don't use 1234, I beg you)
3. Create a profile for your kid
4. Add channels or import playlists from the parent dashboard
5. Hand the device to your child
6. Enjoy your coffee in peace

## Environment Variables (for building)

For signed release builds, you'll need these secrets in GitHub Actions:
- `KEYSTORE_BASE64` - Your keystore file, base64 encoded
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

## Contributing

Found a bug? Want to add a feature? PRs welcome.

Just remember: this app was built by a sleep-deprived parent with AI assistance. Set your expectations accordingly.

## Disclaimer

This app is not affiliated with YouTube, Google, or any entity that thinks the current state of parental controls is acceptable.

It's just a dad who wanted to let his kid watch Bluey without accidentally stumbling into weird algorithm-recommended content.

## License

MIT - Do whatever you want with it. If it helps your kid watch less garbage, I consider that a win.

---

*Built with love, frustration, and approximately 47 cups of coffee.*

*Special thanks to Claude for being a better pair programmer than most humans I've worked with.*
