# opencode Chat Session — ScorecardStudio

---

## Session 1 — Wed Jun 24 2026

### 1. Session Persistence
Stop clearing cookies/cache, SharedPreferences SessionDataStore, JS bridge, dual storage

### 2-4. Live TV Fixes (WebView attempts)
Mixed content, SSL, CORS, native HLS, Chrome UA, canPlayType detection

### 5. ✅ Native ExoPlayer — The Real Fix *(latest)*
**Problem:** WebView's `<video>` element + Hls.js can't reliably play most streams due to CORS, codec, and WebView limitations — even though they work in Chrome.

**Solution:** Replaced in-WebView player with **Android ExoPlayer** (Google's native media player):

| File | What |
|------|------|
| `VideoPlayerState.kt` | Singleton state holder for play requests |
| `VideoPlayerOverlay.kt` | Full-screen ExoPlayer in Compose with close button |
| `WebAppInterface.kt` | `playNativeStream(url, title)` JS bridge method |
| `MainScreen.kt` | Shows ExoPlayer overlay when state changes |
| `index.html` | `onTVChannelClick` now calls native player first |
| `build.gradle.kts` | Added Media3 ExoPlayer + HLS dependencies |

**Why this works:** ExoPlayer runs natively on Android's media stack — no CORS, no WebView limits, no User-Agent blocking. The same engine that Chrome uses.

**Deployed:** Both Studio & Lite (force install) + GitHub (`adfe998`)
