# Privacy Policy for MMSI Lookup

**Last Updated:** November 16, 2026

This Privacy Policy describes how **MMSI Lookup** ("the Application"), developed by **Karsten Thygesen** ("we", "us", or "our"), handles information when you install and use the Application.

---

## 1. Overview & Core Philosophy

**MMSI Lookup** is designed with an offline-first, privacy-by-default architecture. The primary purpose of the Application is to identify maritime flag states and countries based on Maritime Mobile Service Identity (MMSI) / Maritime Identification Digits (MID) codes. 

We do **not** collect, sell, rent, or trade your personal information.

---

## 2. Information We Collect

### A. Personal Information
The Application does **not** require any user registration, account creation, name, email address, phone number, physical address, or payment details. We do not collect or store any personally identifiable information (PII).

### B. Device Permissions & Hardware Data
- **INTERNET & ACCESS_NETWORK_STATE**: 
  - Used exclusively to check internet availability and perform periodic (weekly) or user-initiated database synchronization checks.
- We do **not** access your device location (GPS), camera, microphone, contacts, photos, media files, or external storage.

### C. Local On-Device Data Storage
The Application utilizes local SQLite storage (Room database) stored strictly on your local device:
- **Search History (Recent Lookups):** Recent MMSI codes and lookup timestamps are stored locally on your device for your convenience. This data never leaves your device and can be cleared at any time directly within the app by tapping the "Clear" button.
- **Offline Maritime Registry Database:** A pre-packaged, offline copy of the ITU-R M.585 registry is stored locally so lookups function completely without an internet connection.
- **App Configuration:** Local preferences such as Coast Guard Bridge Night Mode, auto-sundown toggle, and the last synchronized database timestamp and version string.

None of this locally stored data is transmitted to external servers or accessible by third parties.

---

## 3. Network Connections & Third-Party Services

The Application is fully functional without an active internet connection. Network activity is limited to:

1. **Connectivity Verification & Database Updates:** When you tap "Update Database Online" (or during the optional weekly connectivity check), the Application performs a lightweight HTTPS connectivity check (`generate_204`) and synchronizes the local ITU maritime MID dataset. No user identifiers, analytics, advertising IDs, or MMSI search terms are ever transmitted.
2. **No Advertising or Analytics SDKs:** The Application contains no third-party advertising networks, analytics trackers, or social media SDKs.

---

## 4. Children’s Privacy

The Application does not address anyone under the age of 13. We do not knowingly collect personal identifiable information from children under 13.

---

## 5. Security of Your Data

Because the Application operates offline and stores recent lookups strictly in the private sandbox storage of your Android device, your search queries and usage are protected by Android's built-in application sandboxing.

---

## 6. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. Any updates will be reflected with a revised "Last Updated" date at the top of this page. You are advised to review this page periodically for any changes.

---

## 7. Contact Information & Developer Feedback

If you have any questions, feedback, or suggestions regarding this Privacy Policy or the Application, please contact:

- **Developer:** Karsten Thygesen
- **Email:** [karthy@gmail.com](mailto:karthy@gmail.com)
- **Application:** MMSI Lookup (Android)
