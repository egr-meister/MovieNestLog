# MovieNest Log

MovieNest Log is a native Android personal watchlist and viewing diary for movies and TV series. You manually create entries for titles you want to watch, are currently watching, or have already watched, and record your own ratings, genres, dates, series progress, favorites, and notes. Everything is stored only on your device.

## Manual tracking disclaimer

> MovieNest Log is a manual personal watchlist and viewing diary. Titles, genres, dates, ratings, progress, favorites, and notes are entered by the user. The app does not provide movies, episodes, posters, streaming links, downloads, trailers, or official entertainment metadata.

> MovieNest Log is not affiliated with any movie studio, television network, streaming platform, cinema chain, or entertainment database.

## What MovieNest Log does and does not do

- **No movie content** — it never provides movies or episodes.
- **No streaming links** — it does not find, list, or open places to stream anything.
- **No downloads** — it cannot download movies, episodes, or media of any kind.
- **No trailers** — there is no trailer playback or browsing.
- **No posters or official artwork** — the interface is drawn with Compose shapes, text, and local vector graphics only.
- **No entertainment metadata API** — there is no TMDB/IMDb/JustWatch-style integration; nothing is fetched from the internet.
- **No affiliation** — it is not connected to any studio, network, streaming service, cinema chain, or database.

## Main features

- Add, edit, and delete movies and series (full CRUD).
- Three watch statuses: Want to Watch, Watching, Watched.
- Personal half-star ratings (0.5–5.0) with a clear "Not rated" state.
- Personal notes with a live character counter.
- Manual genre entry plus a custom genre manager.
- Viewing dates: start date, watched date, completion date.
- Favorite marking, independent of status, usable as a filter.
- Series progress: seasons and episodes, with a safe percentage and descriptive fallbacks.
- Monthly statistics based on the watched date.
- Local search, filters (status, type, genre, favorites), and sorting.
- Entry detail view and a viewing history/timeline.
- Full onboarding, reset, and data-management flows.
- Works completely offline.

## Privacy

> MovieNest Log stores titles, content types, statuses, ratings, genres, dates, progress, favorites, notes, and settings locally on this device. The app has no account, no cloud sync, no internet access, no ads, no analytics, no payments, no streaming links, and no movie database integration.

There is **no account, no backend, no cloud sync, no Firebase, no ads, no analytics, no payments, no subscriptions, no external APIs, no INTERNET permission, and no runtime permissions**. The app declares no permissions at all.

## Offline-only architecture & local storage

All data lives on the device in **DataStore Preferences**, serialized as JSON strings via **Kotlinx Serialization**:

- `entries_json` — the list of `MovieEntry` records.
- `genres_json` — custom genre labels.
- `settings_json` — app settings, filters, onboarding state.

A single repository (`MovieNestRepository`) is the source of truth. It decodes defensively: empty storage, missing keys, empty strings, corrupted JSON, and missing/newer fields all fall back to safe defaults instead of crashing. Room is intentionally **not** used — DataStore JSON is sufficient here.

## Data model behavior

- **Movie entries** and **series entries** share one `MovieEntry` model; series-only fields (seasons/episodes) are only shown and persisted for series.
- **Watch statuses** are controlled manually and never silently erase data. Moving Watched → Want to Watch keeps your rating and notes. Movie ↔ Series conversions are confirmed when progress would be affected.
- **Series progress** shows a clamped 0–100% only when a positive total episode count exists; otherwise it shows a descriptive label (e.g. "Season 2 in progress").
- **Personal rating** is subjective and labeled "Your rating" / "Not rated" — never a critic or official score. Monthly averages include only rated, watched titles in the selected month and show "No rated titles" when none exist.
- **Favorites** are stored locally and toggled from tickets or the detail screen; removing a favorite never deletes the entry.
- **Monthly statistics** use the watched date. Entries marked Watched without a watched date are not counted, and a note is shown on the detail screen. Missing durations are never treated as zero in totals.

## Search and filters

Search matches title, original title, and notes, case-insensitively, entirely on device. Library filters cover status, content type, genre, and favorites, with sort options: Recently Updated, Title A–Z, Title Z–A, Newest Watched, Oldest Watched, Highest Rated, Lowest Rated, Recently Added.

## Data reset behavior

- **Delete all titles** removes every entry but keeps genres and settings.
- **Reset all local data** clears the entire library, custom genres, filters, preferences, and onboarding state (onboarding will show again).
- Both destructive actions require explicit confirmation.

## Missing metadata behavior

Because nothing is fetched online, absent optional values are shown honestly: "Not set", "Not rated", "No genre data", "Progress total not set", etc. No value is ever invented.

## Export

Data export is **not implemented** in this version.

---

## Visual concept

**Cinema Shelf Ticket Journal** — the Home screen combines a compact cinema shelf with perforated cinema-ticket cards.

- The **cinema shelf** (wood rails, brass edges) represents your personal library.
- **Ticket cards** (rounded shape, side notches, dashed perforation, status stamps, small star/heart symbols) represent individual titles.
- The visual identity is built entirely from Compose shapes, borders, typography, and local vector resources — **no posters, no streaming grid, no branded artwork**.

The Home layout is intentionally distinctive: a cinema marquee header, a segmented Want to Watch / Watching / Watched shelf selector connected to the content rail, a "Now Showing on My Shelf" horizontal rail, a narrow monthly admission strip, an "Up Next" ticket queue, a recent viewing timeline, and a prominent ticket-shaped Add Title action.

### App icon concept

A custom adaptive icon: a warm cream cinema ticket with two side perforation notches, a burgundy shelf line, and a single five-point star, with a subtle dark outline. No text, no poster, no play button, no camera reel, no studio or streaming logo. Adaptive foreground, adaptive background, and a monochrome layer are provided.

### Splash screen concept

A static splash on a warm paper background with a centered ticket resting above a short shelf line and a burgundy accent. No posters, film clips, branding, or heavy animation.

## Technology stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose, AndroidX ViewModel, Lifecycle Compose integration
- Kotlin Coroutines, Flow, StateFlow
- DataStore Preferences, Kotlinx Serialization
- `java.time` APIs
- Gradle Kotlin DSL, JDK 17

## Architecture

Simple MVVM with one local repository and immutable `StateFlow`-backed UI state:

- `data/model` — `MovieEntry`, `AppSettings`, `AppData`, and enums.
- `data/repository` — `MovieNestRepository` (single source of truth over DataStore).
- `ui/theme` — colors, typography, shapes, and the extended cinema palette.
- `ui/navigation` — routes and the `NavHost` graph with bottom navigation.
- `ui/state` — immutable UI state and the shared entry form state.
- `ui/viewmodel` — Home, Library, Watching, Favorites, Statistics, Settings, Genre, Onboarding, EntryDetail, EntryEditor.
- `ui/components` — ticket cards, status stamps, rating input, date input, progress, empty states, dialogs.
- `ui/screens` — all screens listed below.
- `util` — date, formatting, query/sort, and statistics utilities.
- `validation` — field-level validation.

ViewModels collect repository flows, expose immutable state, validate input, run CRUD, survive configuration changes, and handle missing IDs. Composables contain no persistence logic.

## Project structure

```
MovieNestLog/
├─ settings.gradle.kts
├─ build.gradle.kts
├─ gradle.properties
├─ gradle/libs.versions.toml
├─ app/
│  ├─ build.gradle.kts
│  ├─ proguard-rules.pro
│  └─ src/
│     ├─ main/
│     │  ├─ AndroidManifest.xml
│     │  ├─ java/com/movienest/log/...   (models, repository, ui, util, validation)
│     │  └─ res/                          (icon, splash, themes, strings)
│     └─ test/java/com/movienest/log/...  (unit tests)
└─ .github/workflows/android-build.yml
```

### Screens

Onboarding, Home, Add Entry, Entry Detail, Edit Entry, Library, Watching, Favorites, Monthly Statistics, Genre Management, Settings. Watched history is presented within the Library and the Home/Statistics timelines.

## Open in Android Studio

1. Requirements: **JDK 17** and a recent Android Studio with the **Android 16 / API 36** platform and **Build Tools 35.0.0** installed.
2. `File → Open…` and select the `MovieNestLog` folder.
3. Android Studio will generate the Gradle wrapper on first sync. If you use the command line first, run `gradle wrapper --gradle-version 8.9` once to create `gradlew`.
4. Configuration: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26` (satisfies "24 or higher"; supports Android 16 and newer). Portrait only. Edge-to-edge with safe insets and visible system bars.

### 16 KB memory page-size compatibility

The app is pure Kotlin/Compose with **no native (.so) libraries**, so it is inherently compatible with 16 KB memory page sizes. Verify the final AAB with Android Studio's *Analyze APK/Bundle* (there should be no native libraries) or `bundletool`.

## Build & run

### Debug build

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Non-minified release build (staged verification)

R8 and resource shrinking start **disabled** in `app/build.gradle.kts`:

```kotlin
isMinifyEnabled = false
isShrinkResources = false
```

First build, sign, install, and test the non-minified release. Only after it passes, enable R8:

```kotlin
isMinifyEnabled = true
isShrinkResources = true
proguardFiles(
    getDefaultProguardFile("proguard-android-optimize.txt"),
    "proguard-rules.pro"
)
```

Then repeat the critical tests (launch, DataStore, Kotlinx Serialization, Navigation Compose, entry detail routes, filters, enum serialization, monthly statistics, missing entries, corrupted JSON fallback). `proguard-rules.pro` already keeps the `@Serializable` models and their generated serializers.

## Signing

Release APK and AAB must be signed with a real **PKCS12** keystore. The debug certificate is never used for release — if release signing credentials are absent, the build logs a clear warning and does not silently fall back to debug signing.

### Generate a keystore

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore movienest-log-release-key.p12 -alias movienest_log_key -keyalg RSA -keysize 2048 -validity 10000
```

### Local signing configuration

Create `keystore.properties` in the project root (already git-ignored):

```properties
storeFile=/absolute/path/to/movienest-log-release-key.p12
storePassword=your_store_password
keyAlias=movienest_log_key
keyPassword=your_key_password
```

Alternatively, provide the same values via environment variables: `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

Never commit keystores, passwords, `keystore.properties`, decoded keystores, or Base64 values.

### Build signed artifacts

```bash
./gradlew :app:assembleRelease   # signed release APK (local install/testing)
./gradlew :app:bundleRelease     # signed release AAB (Google Play upload)
```

Only the **AAB** should be uploaded to Google Play as the production artifact.

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and via `workflow_dispatch`. It checks out the repo, sets up JDK 17 and the Android SDK (Platform 36, Build Tools 35.0.0), decodes the keystore, builds the signed release APK and AAB, verifies the certificate with `apksigner`, fails if the output contains `CN=Android Debug`, and uploads the APK and AAB as artifacts. Passwords and the Base64 keystore are never printed, and the decoded keystore stays only on the disposable runner. No mandatory emulator smoke test runs on free runners.

### Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

> CI proves compilation, signing, certificate verification, and artifact creation — it does **not** prove the app launches. Always do local device verification too.

### Verify signature locally

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

The certificate must **not** contain `CN=Android Debug`.

## Local release verification & logcat

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat
```

Check logcat for `ClassNotFoundException`, `NoSuchMethodError`, serialization exceptions, DataStore corruption crashes, `IllegalArgumentException`, `NumberFormatException`, date-parsing crashes, navigation-argument crashes, null-entry errors, enum-decoding errors, Compose state exceptions, R8-related errors, and signing errors.

## Functional test checklist

Verify first launch with empty storage; onboarding display, complete, skip, and replay from Settings; empty Home; adding first movie and series; duplicate titles; required-only and all-optional entries; each status; editing every field; ratings (add, remove, every half-star); multiline notes and the character limit; favorites and favorite filters; status transitions preserve data; dates (start, watched, completion, invalid, and start-after-completion); series with and without totals; updating seasons/episodes; +1 Episode; progress beyond total; marking a series watched; series↔movie conversion with confirmation; search by title and original title; all filters and combinations; clearing filters; all sort options; open/edit/delete from detail with confirm/cancel; opening a deleted entry route (missing fallback); genre add/rename/delete and in-use handling; statistics with no data, one entry, mixed types, and missing ratings; month navigation; delete all and reset all; relaunch and onboarding reset; airplane mode; no INTERNET permission; no runtime permission dialog; no poster or streaming behavior. Repeat the critical items after enabling R8.

## Testing

Unit tests cover title/rating/duration validation, half-star rules, series percentage and division-by-zero guards, watched-exceeds-total, monthly counts and averages, unrated exclusion, most-used-genre grouping (case-insensitive), search across title/original/notes, status/type/favorite filtering, title and rating sorting, newest/oldest watched, invalid date handling, JSON round-trips, default merging for missing fields, and unknown-key tolerance.

```bash
./gradlew :app:testDebugUnitTest
```
