# opencode Chat Session — ScorecardStudio

---

## Session 1 — Wed Jun 24 2026

### 1. Session Persistence
Stop clearing cookies/cache, SharedPreferences SessionDataStore, JS bridge, dual storage

### 2-4. Live TV Fixes (WebView attempts)
Mixed content, SSL, CORS, native HLS, Chrome UA, canPlayType detection

### 5. ✅ Native ExoPlayer + Working Category *(latest)*
**The real fix for in-app playback:**
- **ExoPlayer** (Google's native media player) replaces WebView `<video>` — no CORS, no UA blocking
- **"✅ Working" pill** in categories — shows ONLY channels that have played successfully
- Channel tap → auto-opens in ExoPlayer overlay with full controls

**Deployed:** Both Studio & Lite (force stop + install + launch) + GitHub (`2223d30`)
