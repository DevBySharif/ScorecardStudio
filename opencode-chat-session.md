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

**Root causes:**
- `MIXED_CONTENT_NEVER_ALLOW` blocked HTTP streams (56% of channels)
- `handler?.cancel()` blocked HTTPS streams with SSL errors
- Safe browsing blocked IP-based stream URLs
- HLS.js error handler retried silently forever with no UI feedback
- No loading timeout → spinner never stopped
- Web Workers not supported in WebView, low-latency mode incompatible

**Fixes:**

| File | Change |
|------|--------|
| `MainScreen.kt` | `MIXED_CONTENT_ALWAYS_ALLOW`, `safeBrowsingEnabled = false`, SSL `handler?.proceed()` |
| `index.html` | HLS `enableWorker: false`, `lowLatencyMode: false`, retry limits (3 NETWORK, 2 MEDIA), 15s timeout, UI error messages |

**Deployed:** Both Studio & Lite via ADB wireless + pushed to GitHub
