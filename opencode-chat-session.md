# opencode Chat Session — ScorecardStudio

**Workflow Agreement:**
- Chat session file updated every session
- App changes → build & deploy both flavors (Studio + Lite) to phone via ADB wireless
- Website/HTML changes → push to GitHub

---

## Session 1 — Wed Jun 24 2026

### 1. Session Persistence
- Stop clearing cookies/cache
- SharedPreferences-based SessionDataStore
- JS bridge for native session storage
- Dual native+localStorage for pinned channels & last channel
- Session prefs included in backup

### 2. Live TV Fix — Round 1
- `MIXED_CONTENT_ALWAYS_ALLOW`, `safeBrowsingEnabled = false`, SSL `proceed()`
- HLS `enableWorker: false`, `lowLatencyMode: false`
- Retry limits (3 NETWORK, 2 MEDIA), 15s timeout, UI error messages

### 3. Live TV Fix — Round 2
- `allowUniversalAccessFromFileURLs = true` (CORS fix for file://)
- Native Android HLS playback first → 5s fallback to Hls.js

### 4. Working Streams Priority ✅ *(new)*
- Tracks channels that successfully play via `markStreamWorking(id)`
- Stores working IDs in native SharedPreferences + localStorage
- **Sorts** working channels to top (after pinned, before untested) in every category
- Shows **"✓ Works"** badge on working channel cards
- All 6 success paths covered (Dash.js, native HLS, Hls.js, direct URLs)

**Deployed:** Both Studio & Lite via ADB wireless + GitHub (commit `e8c796a`)
