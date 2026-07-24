# Quick Start Guide - Phase 1

## Build Commands

### Build all modules
```bash
./gradlew build
```

### Build shared module only
```bash
./gradlew :shared:build
```

### Build Android app only
```bash
./gradlew :androidApp:build
```

## Run on Android

### Install and run on emulator/device
```bash
./gradlew :androidApp:installDebug
# Then open the app manually from your device, or use:
./gradlew :androidApp:run
```

### Launch from Android Studio
1. Open Android Studio
2. Open the project root directory
3. Select `:androidApp` as the run configuration
4. Click Run or press Shift+F10

## Testing Navigation

After app launches, you should see:

1. **Splash Screen** (2 seconds)
   - Shows "WMS" text centered on primary color
   - Auto-navigates to Login screen

2. **Login Screen**
   - Username field (placeholder text: "Username")
   - Password field (placeholder text: "Password")
   - Login button
   - Click Login to proceed to Dashboard (no validation, placeholder only)

3. **Dashboard Screen**
   - 5 navigation cards:
     - Operator → navigates to Operator screen
     - Diagnostics → navigates to Diagnostics screen
     - Maintenance → navigates to Maintenance screen
     - Reports → navigates to Reports screen
     - Settings → navigates to Settings screen
   - Each card includes title and description

4. **Feature Screens** (Operator, Diagnostics, Maintenance, Reports, Settings)
   - Display feature title
   - Show "Back to Dashboard" button
   - Currently show placeholder UI only

5. **Other Screens** (Shuttle, UserManagement)
   - Not shown on Dashboard but accessible via navigation routes
   - Can be navigated to from AppNavHost when Screen.Shuttle or Screen.UserManagement are set

## Project Structure

- **shared/**: Kotlin Multiplatform module (Android + JVM targets)
  - commonMain: Shared code for all platforms
  - androidMain: Android-specific code
  - jvmMain: JVM/Desktop-specific code

- **androidApp/**: Android application module
  - Depends on shared module
  - Entry point: MainActivity.kt
  - Configures Koin and Logger at startup

- **desktopApp/**: JVM application module (configured but not implemented)

- **gradle/**: Gradle configuration
  - libs.versions.toml: Version catalog for all dependencies

## Debugging

### View Koin dependencies
The app initializes Koin with `sharedModule` which includes all feature modules.
To debug DI, add logging or inspection in:
- `shared/src/commonMain/kotlin/.../core/di/Modules.kt`

### View Navigation state
The Navigator uses StateFlow, so you can observe current screen:
- `shared/src/commonMain/kotlin/.../core/navigation/Navigator.kt`

### Inspect Theme
All theme constants are in:
- `shared/src/commonMain/kotlin/.../presentation/theme/`

## Dependencies

### Core Libraries
- Kotlin: 2.4.10
- Kotlin Multiplatform: 2.4.10
- Compose Multiplatform: 1.11.1
- Koin: 3.4.0 (DI)
- Napier: 2.6.1 (Logging)
- kotlinx-serialization: 1.6.0
- kotlinx-coroutines: 1.11.0

### Android-specific
- androidx.activity:activity-compose: 1.13.0
- androidx.lifecycle: 2.11.0-beta01
- androidx.core:core-ktx: 1.19.0
- material3: 1.11.0-alpha07

## Common Issues

### "Unresolved reference: Platform"
- Make sure `shared/src/commonMain/kotlin/.../Platform.kt` exists
- It defines the Platform interface expected by platform-specific actuals

### "Module conflict" or "Unresolved DI binding"
- Each feature has its own DI module (e.g., `authModule`, `dashboardModule`)
- These are aggregated in `sharedModule` in `core/di/Modules.kt`
- When Phase 2 adds concrete implementations, bind them in the appropriate feature module

### Screens not navigating
- Check that `AppNavHost` includes the desired screen route
- Ensure `Navigator.navigateTo(Screen.SomeScreen)` is called
- Verify screen is imported in AppNavHost

---

## What to Test

✅ **Manual Testing Checklist:**
- [ ] App launches and shows Splash screen
- [ ] Splash auto-navigates to Login after 2 seconds
- [ ] Login screen renders with username and password fields
- [ ] Click Login button navigates to Dashboard
- [ ] Dashboard shows 5 navigation cards
- [ ] Click each card navigates to correct screen
- [ ] Each feature screen has a "Back to Dashboard" button that works
- [ ] App theme applies Material3 colors and typography consistently
- [ ] Buttons use correct primary/secondary colors

---

For Phase 2 development, start implementing:
1. UseCase classes in each feature's `domain/usecase/` package
2. Repository implementations in `data/repository/`
3. ViewModel logic with state management
4. Real screen UI with data display

---

**Phase 1 Ready for Development** ✅

