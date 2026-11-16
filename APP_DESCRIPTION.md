# MMSI Lookup — Google Play Store Listing & App Description

## App Details
- **App Name:** MMSI Lookup
- **Package ID:** `com.aistudio.mmsilookup.qkvd`
- **Current Version:** `5.0` (Version Code: `5`)
- **Category:** Maps & Navigation *(Secondary: Tools)*
- **Content Rating:** Everyone (3+)
- **Developer Contact:** Karsten Thygesen (`karthy@gmail.com`)
- **Privacy Policy:** `PRIVACY_POLICY.md`

---

## What's New in Version 5.0 (Release Notes / Release Changelog)
- **Play Store Optimization & Security**: Enabled R8 code optimization, obfuscation, and resource shrinking with bundled crash deobfuscation mapping (reducing bundle size to ~3.3 MB).
- **Updated Dependencies**: Removed unused legacy dependencies (eliminating outdated `androidx.fragment` 1.1.0 warnings).
- **Android 15+ Edge-to-Edge Polish**: Dynamic system bar styling across Light, Dark, and Bridge Red Night Mode, plus full display-cutout and keyboard inset support.
- **Daytime Launch Fix**: Fixed an issue where launching during daylight hours could trigger Bridge Night Mode; auto-sundown now relies strictly on astronomical solar calculations.
- **Bridge Night Mode & Solar Auto-Detection**: Automatic sundown-to-sunrise red night vision without location permissions, plus Red/Black monochrome flags and weekly offline-friendly database sync.

---

## Store Tags
1. Marine & Navigation
2. Boating
3. Sailing
4. Tools
5. Transportation

---

## Short Description (80 characters maximum)
> Offline MMSI & MID country lookup with Coast Guard bridge red night mode.

*(Character count: 79 / 80)*

---

## Full Description (Google Play Store format)

Identify any vessel's country of registration instantly with MMSI Lookup — designed specifically for bridge watchstanders, coast guard operators, maritime officers, and recreational mariners.

Whether monitoring AIS targets, checking VHF DSC distress alerts, or identifying approaching traffic at sea, MMSI Lookup decodes 3-digit Maritime Identification Digits (MID) and full 9-digit MMSI numbers instantly and 100% offline.

### KEY FEATURES:

• **100% Offline at Sea**
Operates completely without cellular reception or satellite internet. The entire ITU-R M.585 maritime database is stored locally on your device in high-speed SQLite storage.

• **Coast Guard Bridge Night Mode with Auto-Sundown Activation**
Built for navigation bridges and low-light night watches. Switches the entire UI into a monochromatic deep red palette (~650nm) on pure pitch-black AMOLED backgrounds to preserve scotopic night vision and protect horizon scanning. Automatically activates when launched between sundown and sunrise using zero-permission astronomical solar calculation from your device's timezone. Manual override is always available with a single tap.

• **Instant Tactile Keypad**
Large, gloved-hand friendly keypad designed for rolling seas. Just enter the first 3 digits of any MMSI or radio callsign to reveal the flag state immediately.

• **Complete Maritime Information**
Displays country name, national flag, ISO-3166 country codes, ITU regional allocation blocks, and vessel station type decoding (merchant vessels, coast stations, SAR aircraft, AIS aids to navigation).

• **Comprehensive ITU Directory**
Browse or search the full global registry of over 200 maritime jurisdictions, filtered by region (Europe, Americas, Asia, Africa, Oceania).

• **Search History**
Quickly access your recent lookups with single-tap recall and one-tap clear controls.

• **Automatic Weekly ITU Database Updates**
Keeps your maritime registry current completely hands-free. Automatically detects internet connectivity (Wi-Fi or cellular) and synchronizes the latest ITU allocation changes in the background — strictly limited to once per week to protect limited offshore satellite bandwidth while guaranteeing vessel registration data never goes stale. Manual on-demand sync is also available at any time.

• **Private & Zero Tracking**
No account required, no ads, no trackers, and no GPS location data collected. Built by a mariner for mariners.

---

### TECHNICAL SPECIFICATIONS:
- **Standards Compliance:** ITU-R Recommendation M.585 (Assignment and use of identities in the maritime mobile service)
- **Station Types Supported:**
  - Individual Ships (`MIDxxxxxx`)
  - Coastal Stations & Port Operations (`00MIDxxxx`)
  - Group Ship Stations (`0MIDxxxxx`)
  - SAR Aircraft (`111MIDxxx`)
  - AIS Aids to Navigation / AtoN (`99MIDxxxx`)
  - Craft Associated with Parent Ship (`98MIDxxxx`)
- **Display Modes:** Standard Maritime Light/Dark & High-Contrast Scotopic Red Night Mode
- **Storage:** Local Room (SQLite) database, zero cloud dependency

---

## Graphic Assets Checklist
- **App Launcher Icon:** `playstore_icon_512.png` (512x512 PNG, 32-bit)
- **Feature Graphic:** `playstore_feature_graphic_1024x500.png` (1024x500 PNG, 24-bit)
