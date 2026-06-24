# opencode Chat Session — ScorecardStudio

**Workflow:** Session chat saved → App changes built & deployed (both flavors) → HTML changes pushed to GitHub

---

## Session 1 — Wed Jun 24 2026

### 1. Session Persistence
- Stop clearing cookies/cache, SharedPreferences SessionDataStore, JS bridge, dual native+localStorage

### 2. Live TV Fix — Round 1
- Mixed content allow, safe browsing off, SSL proceed, HLS config fixes, retry limits, 15s timeout

### 3. Live TV Fix — Round 2
- `allowUniversalAccessFromFileURLs=true` (CORS), native HLS first → Hls.js fallback

### 4. Working Streams Priority
- Track successful plays, sort working to top (after pinned), "✓ Works" badge

### 5. 🌐 Open in Browser on Every Card *(latest)*
- **🌐 button** on each channel card → tap to instantly open stream in external browser
- **"OPEN IN BROWSER" button** inside loading overlay on any error (timeout, DRM, unavailable)
- Works for all failure paths: timeout, network error, media error, DRM error, DASH missing

**Deployed:** Both Studio & Lite (force reinstalled) + GitHub (commit `fa91650`)
