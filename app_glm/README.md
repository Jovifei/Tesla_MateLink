# tesla-master-glm

<p align="center">
  <img src="https://img.shields.io/badge/platform-iOS%20%7C%20Android-blue.svg" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%7C%20Swift-orange.svg" alt="Language">
</p>

**tesla-master-glm** is a cross-platform mobile application for Tesla vehicle monitoring via TeslaMate API. provides Bluetooth connectivity and real-time vehicle data tracking for iOS (including Watch App) and Android.

## Features

### 🚗 Vehicle Monitoring
- Real-time vehicle status tracking
- Battery health monitoring
- Tire pressure monitoring (TPMS)
- Charge session tracking
- Drive history and statistics

### 💰 Cost Calculation
- Time-of-Use (ToU) tariff support
- Multiple pricing tiers (peak / flat / valley)
- Charging cost estimation
- Monthly/annual energy cost statistics

### 🗺️ Navigation & Mapping
- GCJ-02 coordinate conversion for China map compatibility
- Drive route heatmap visualization
- Geocoded destination tracking
- Amap integration

### 📊 Efficiency Analysis
- Energy efficiency tracking by trip
- Historical efficiency comparison
- Vampire discharge monitoring ("vampire mode")

### 🔔 Notifications & Widgets
- Android 12+ widgets for vehicle status
- iOS Widget support
- Charging completion notifications
- Sentry monitoring alerts

### 🌐 Multi-language Support
- English (en)
- Simplified Chinese (zh-Hans)
- German (de)
- French (fr)
- Japanese (ja)

## Project Structure

```
app_glm/
├── android/                    # Android application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/teslamatelink/
│   │       │   ├── data/          # Data layer (API, local DB, repository)
│   │       │   ├── domain/        # Business logic
│   │       │   ├── ui/            # UI screens (Compose)
│   │       │   └── notification/ # Notifications & workers
│   │       └── res/              # Resources & localization
│   └── build.gradle.kts
├── ios/                        # iOS application (Swift + SwiftUI)
│   ├── MateLink/
│   │   ├── App/                 # App entry point
│   │   ├── Core/                # Core models & utilities
│   │   ├── Features/            # Feature modules
│   │   └── Widget/              # iOS Widget
│   └── MateLink Watch App/      # watchOS Companion App
├── shared/                     # Shared types & mock data
│   ├── api-types.ts             # API type definitions
│   └── mock_data.json          # Mock data for development
└── web_matelink/               # Web (shared localization messages)
    └── src/messages/           # i18n message files
```

## Tech Stack

### Android
- **Kotlin** + **Jetpack Compose** - Modern declarative UI
- **Hilt** - Dependency injection
- **Room** - Local database
- **WorkManager** - Background sync
- **Paging 3** - Pagination
- **Jetpack Navigation** - In-app navigation

### iOS
- **Swift** + **SwiftUI** - declarative UI framework
- **WatchConnectivity** - iOS-watchOS communication
- **Core Location** - Location services

## Architecture

Clean architecture with separation of concerns:
- **Data Layer**: API sources, local database, repositories
- **Domain Layer**: Use cases, business logic, models
- **Presentation Layer**: UI, view models, state management

## Building

### Prerequisites
- Android: Android Studio Electric Eel+, JDK 17
- iOS: Xcode 16+, Swift 6
- Android `minSdk = 28`, `targetSdk = 35`

### Android
```bash
cd android
./gradlew assembleDebug
```

### iOS
```bash
# Open with Xcode
open ios/MateLink.xcodeproj
```

## Configuration

### AMap API Key
For map functionality, obtain an API key from [AMAP](https://lbs.amap.com/) and set it in `local.properties`:
```properties
AMAP_API_KEY=your_api_key_here
```

## Credits

Based on the original [TeslaMate](https://github.com/adriankumpf/teslamate) project for vehicle data logging.

## License

Copyright © 2024 Jovi. All rights reserved.
