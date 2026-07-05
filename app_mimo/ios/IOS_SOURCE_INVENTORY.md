# app_mimo iOS Source Inventory

## App Target Inputs
- `MateLink/App/MateLinkApp.swift`
- `MateLink/App/ContentView.swift`
- `MateLink/App/AppState.swift`

## Core Inputs
- `MateLink/Core/API/ApiClient.swift`
- `MateLink/Core/Map/AmapView.swift`
- `MateLink/Core/Map/MapUtils.swift`
- `MateLink/Core/Models/Car.swift`
- `MateLink/Core/Models/CarStatus.swift`
- `MateLink/Core/Theme/AppTheme.swift`
- `MateLink/Core/Utils/GCJ02Converter.swift`
- `MateLink/Core/Utils/RouteSimplifier.swift`

## Feature Inputs
- `MateLink/Features/Battery/BatteryHealthView.swift`
- `MateLink/Features/Charges/ChargeDetailView.swift`
- `MateLink/Features/Charges/ChargeListView.swift`
- `MateLink/Features/Cost/CostView.swift`
- `MateLink/Features/Cost/TariffConfigView.swift`
- `MateLink/Features/Dashboard/DashboardView.swift`
- `MateLink/Features/Destinations/DestinationsView.swift`
- `MateLink/Features/Drives/DriveDetailView.swift`
- `MateLink/Features/Drives/DriveListView.swift`
- `MateLink/Features/Efficiency/EfficiencyView.swift`
- `MateLink/Features/Heatmap/HeatmapView.swift`
- `MateLink/Features/More/MoreView.swift`
- `MateLink/Features/Onboarding/OnboardingView.swift`
- `MateLink/Features/Range/RangeView.swift`
- `MateLink/Features/Reports/AnnualReportPDFView.swift`
- `MateLink/Features/Reports/ExportView.swift`
- `MateLink/Features/Settings/SettingsView.swift`
- `MateLink/Features/Statistics/StatisticsView.swift`
- `MateLink/Features/Timeline/TimelineView.swift`
- `MateLink/Features/Updates/UpdatesView.swift`
- `MateLink/Features/Vampire/VampireView.swift`

## Resources
- `MateLink/Info.plist`
- `MateLink/Resources/mock_data.json`
- `MateLink/Resources/de.lproj/Localizable.strings`
- `MateLink/Resources/en.lproj/Localizable.strings`
- `MateLink/Resources/fr.lproj/Localizable.strings`
- `MateLink/Resources/ja.lproj/Localizable.strings`
- `MateLink/Resources/zh-Hans.lproj/Localizable.strings`

## Launch Acceptance Path
- App entry is `MateLinkApp`, which shows `OnboardingView` until `AppState.onboardingDone` is true.
- The first acceptable proof is launch into Onboarding or the main `TabView` shell in `ContentView`.
- Mock-mode launch is acceptable for the first proof because `AppState` loads local mock data on startup.

## Deferred Target Inputs
- `MateLink/Widget/MateLinkWidget.swift` is inventoried, but the initial XcodeGen setup verifies the app target first.
- Widget target setup is deferred until the app target launches on Mac/Xcode or a later follow-up proves the extension setup is trivial.

Verification instructions live in `README.md` and `VERIFY_IOS.md`.
