# opencode Chat Session — ScorecardStudio

**Workflow Agreement:**
- Chat session file updated every session
- App changes → build & deploy both flavors (Studio + Lite) to phone via ADB wireless
- Website/HTML changes → push to GitHub

---

## Session 1 — Wed Jun 24 2026

### Request: App close hole jeno session same thake

**Changes:**

1. **`MainScreen.kt`** — Removed `removeAllCookies(null)` and `clearCache(true)`, changed cache mode to `LOAD_DEFAULT`, enabled form data saving, enabled third-party cookies
2. **`SessionDataStore.kt`** (new) — Native SharedPreferences-based storage for session data
3. **`WebAppInterface.kt`** — Added `getSessionData`, `setSessionData`, `removeSessionData`, `clearSession` bridge methods for JS ↔ native persistence
4. **`index.html`** — `getPinnedIds()`, `setPinnedIds()`, and `lastTVChannel` now use native bridge + localStorage dual storage
5. **`backup_rules.xml`** — Session preferences included in Android backup

**Deployed:** Both Studio & Lite via ADB wireless + pushed to GitHub

---

### Request: Live TV stuck on "CONNECTING TO LIVE FEED..."

**Round 1 Fixes:**

| File | Change |
|------|--------|
| `MainScreen.kt` | `MIXED_CONTENT_ALWAYS_ALLOW`, `safeBrowsingEnabled = false`, SSL `handler?.proceed()` |
| `index.html` | HLS `enableWorker: false`, `lowLatencyMode: false`, retry limits (3 NETWORK, 2 MEDIA), 15s timeout, UI error messages |

**Round 2 Fixes (CORS + Native HLS):**

| File | Change |
|------|--------|
| `MainScreen.kt` | `allowUniversalAccessFromFileURLs=true` (fixes CORS for all stream requests) |
| `index.html` | Native Android HLS playback first → 5s fallback to Hls.js |

**Deployed:** Both Studio & Lite via ADB wireless + pushed to GitHub (commit `b6d04d5`)
