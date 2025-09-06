# HTTP Client Plus (IntelliJ Plugin)

A lightweight Postman-like tool window that lets you compose HTTP requests and saves them as IntelliJ HTTP Client (.http) files.

Available in Intellij Marketplace: https://plugins.jetbrains.com/plugin/28340-http-client-plus

Features (v1.0)
- Method dropdown, URL field, and inline Send button
- Mandatory Name field used as the .http file name (sanitized) and request label (### Name)
- **Saved Requests management** with improved UI organization
- Tabs for Params and Headers with key–value tables
- Body tab with Content-Type selector and overflow (⋮) menu for Beautify JSON
- Multipart form-data builder: boundary input and parts table (text or file parts) with per-part content types and file pickers
- For POST with a body (non-multipart), auto-adds `Content-Type: application/json` if missing
- Every Send saves/updates the .http file; first Send opens it. Execution is manual from the editor

Requirements
- IntelliJ IDEA 2024.2+ (Ultimate)
- Built-in HTTP Client enabled (default)
- JDK 17+ for building

Usage Video

[![Video Title](https://img.youtube.com/vi/q7APwIfE7Og/hqdefault.jpg)](https://www.youtube.com/watch?v=q7APwIfE7Og)

Build and Run
```bash
./gradlew build
./gradlew runIde
```

Build plugin ZIPs for specific IntelliJ versions
- This project supports building against multiple IDE lines using a Gradle property `ideLine` that maps to product versions:
  - 252 → 2025.2 (default)
  - 251 → 2025.1
  - 243 → 2024.3
  - 242 → 2024.2
- Build a ZIP for a given line (artifact goes to `build/distributions/` and is also copied to `dist/<version>/`):
```bash
# Default 2025.2 baseline
./gradlew clean build -x test

# 2025.1
./gradlew clean build -PideLine=251 -x test

# 2024.3
./gradlew clean build -PideLine=243 -x test

# 2024.2
./gradlew clean build -PideLine=242 -x test

```
- ZIP naming: each ZIP includes the IDE line suffix, e.g. `http-client-plus-1.0.0-243.zip`, so ZIPs won’t collide.
- Copies also appear under `dist/1.0.0/` (e.g., `dist/1.0.0/http-client-plus-1.0.0-243.zip`).
- Optional: verify the plugin against the selected IDE version:
```bash
./gradlew runPluginVerifier -PideLine=243
```

Targeting lower IDE versions
- The build baseline is controlled by `ideLine` and applied to `<sinceBuild>` via Gradle. To run on an older IDE:
  - Rebuild with the corresponding `-PideLine` value (see list above)
  - Ensure your feature set and APIs are compatible with that IDE release
  - Keep Java toolchain at 17 unless the IDE requires a different target

Usage
1) Open the tool window: View > Tool Windows > HTTP Client Plus
2) Enter Name, Method, and URL
3) Add Params, Headers, and Body as needed (or switch to Multipart)
4) Click Send to create/open or update the .http file
5) Run the request manually from the editor (Ctrl+Enter)

Notes
- Requests are saved under: `<project>/http-client-plus/collections` by default, or to a custom folder if provided
- Each request is a standard `.http` file; you can edit and run it anytime from the editor


