# 🧪 07. Testing Strategy

A comprehensive testing strategy is crucial for ensuring the reliability and stability of the digital signage application, especially given its potential for 24/7 operation.

## 1. Unit Tests (JUnit, MockK/Mockito)

*   **Focus:** Test individual classes and functions in isolation.
*   **Targets:**
    *   **ViewModels:** Mock repositories and other dependencies. Verify state updates, logic for handling user inputs (if any), and data transformation. Use `kotlinx-coroutines-test` for testing coroutines (`runTest`, `TestCoroutineDispatcher`).
    *   **Repositories:** Mock `ApiService`, `SharedPreferencesManager`, and DAOs. Test logic for fetching data from network vs. cache, error handling, and data mapping.
    *   **Managers (`PlaylistManager`, `ContentCacheManager`):** Test their core logic (sequencing, caching decisions, eviction policies) by mocking dependencies.
    *   **Utility Classes:** Test helper functions for correctness.
    *   **Parsers/Mappers:** Ensure DTOs are correctly mapped to domain models or `DisplayableItem`.
*   **Tools:** JUnit 4/5, MockK (idiomatic for Kotlin) or Mockito.
*   **Location:** `app/src/test/java/com/yourcompany/digitalsignage/...`

**Rule:** Aim for high unit test coverage for business logic and critical utility functions.

## 2. Integration Tests

*   **Focus:** Test interactions between different components of your app, but still within the local environment (not full E2E with backend).
*   **Targets:**
    *   **Repository <> DAO/Room:** Verify database operations (insert, query, delete) work correctly through the repository layer. Use an in-memory Room database for testing (`Room.inMemoryDatabaseBuilder`).
    *   **ViewModel <> Repository:** Test how ViewModels interact with repositories to fetch and process data, including observing data flows.
    *   **`ContentCacheManager` <> Room DB & File System:** Test file download, storage, retrieval, and eviction logic, mocking network calls but interacting with a real (in-memory or test) database and temporary files.
*   **Tools:** AndroidX Test libraries (Robolectric for running tests on JVM, or instrumented tests on emulator/device), Hilt testing utilities (`@HiltAndroidTest`).
*   **Location:** `app/src/androidTest/java/com/yourcompany/digitalsignage/...` (for instrumented tests) or `app/src/test/java/...` (for Robolectric tests).

## 3. UI Tests (Jetpack Compose UI Tests / Espresso)

*   **Focus:** Test UI behavior and interactions from a user's perspective. For a signage app, this mainly involves verifying content display.
*   **Targets:**
    *   **Screen Navigation:** Ensure navigation between Splash, Registration, and Display screens works as expected.
    *   **Registration Screen:** Verify QR code display, text code visibility, and status updates.
    *   **Display Screen:**
        *   Verify that different content types (image, video, web) are rendered. This might involve checking if specific Composable elements are present.
        *   Verify basic transitions (e.g., content changes after a delay).
        *   Check display of error messages or offline indicators.
    *   **Focus Handling (D-Pad):** Critical for any settings or debug screens. Ensure all interactive elements are focusable and navigable.
*   **Tools:** Jetpack Compose Test APIs (`createComposeRule`, `onNodeWithText`, `performClick`, etc.). Espresso for testing View-based components if any remain or for inter-app interactions (less likely here).
*   **Location:** `app/src/androidTest/java/com/yourcompany/digitalsignage/...`

**Rule:** UI tests can be slower and more brittle. Focus on critical user flows and core display functionalities. Use `testTags` (`Modifier.testTag("myElement")`) to reliably find Composables in tests.

## 4. Manual Testing (Critical for TV)

Manual testing is indispensable for Android TV apps due to hardware variations and the specific user interaction model.

*   **Registration Flow:**
    *   Scan QR code from various distances and angles.
    *   Manually enter registration code on the backend.
    *   Test registration with network on/off.
*   **Content Playback:**
    *   Test all supported media types (images, videos of different formats/codecs, HTML content, carousels).
    *   Verify correct durations and looping.
    *   Test transitions between content items.
    *   Check aspect ratio handling.
*   **Network Interruption & Offline Mode:**
    *   Disconnect Wi-Fi/Ethernet during playback. Verify cached content continues.
    *   Observe offline indicator.
    *   Reconnect network and verify recovery and content sync.
*   **Long-Running Stability:**
    *   Leave the app running for extended periods (24-72 hours).
    *   Monitor for memory leaks (Android Studio Profiler), crashes (Firebase Crashlytics), performance degradation (jank, slow transitions).
*   **Reboot Test:**
    *   Power cycle the TV device. Ensure the app auto-starts and resumes playback correctly.
*   **Remote Control Navigation:**
    *   Thoroughly test all UI interactions (if any, like debug menus) using a physical D-Pad remote.
*   **Different TV Hardware/OS Versions:**
    *   Test on as many target Android TV devices and OS versions as possible. Emulators are good, but real hardware reveals unique issues.
*   **FCM Updates:** Test content updates triggered by FCM.
*   **Heartbeat & Backend Sync:** Verify `last_seen_at` updates correctly in the backend.

## 5. API Testing (Postman/Insomnia)

*   Though not part of Android app testing, ensure the Laravel backend API endpoints are thoroughly tested independently. This helps isolate issues between frontend and backend.
*   Verify request/response DTOs match the contract.

## Mocking Strategy during Testing

*   **Unit/Integration Tests:** Use `FakeApiService` (from `06_02`) or MockK/Mockito to mock network responses. Provide mock SharedPreferences or in-memory Room DB.
*   **UI Tests:**
    *   Can use Hilt's testing features to provide `FakeApiService` to test UI states based on mock data.
    *   For Compose Previews (`@Preview`), pass mock `DisplayableItem` data directly to individual renderer Composables.

**Tools & Libraries for Testing:**
*   JUnit 4 or 5
*   MockK or Mockito
*   Turbine (for testing Kotlin Flows)
*   AndroidX Test (Core, Runner, Rules, Espresso, UI Automator)
*   Jetpack Compose Test APIs
*   Hilt Testing (`@HiltAndroidTest`, `@BindValue`, etc.)
*   Robolectric (optional, for running instrumented-like tests on JVM)

**Prompt:** Establish a CI/CD pipeline that runs unit tests and potentially integration/UI tests on every commit or pull request.
