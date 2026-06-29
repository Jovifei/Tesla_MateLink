## ADDED Requirements

### Requirement: Project skeleton with native navigation
The system SHALL provide a native app skeleton for both Android (Kotlin + Jetpack Compose) and iOS (Swift + SwiftUI) with bottom tab navigation containing 4 tabs: Dashboard, Drives, Charges, More.

#### Scenario: App launches with navigation
- **WHEN** user opens the app
- **THEN** app displays bottom tab bar with 4 tabs and defaults to Dashboard tab

### Requirement: API Client for TeslaMateApi
The system SHALL provide an HTTP API client that communicates with TeslaMateApi v1.21+ endpoints, supporting authentication via Bearer token and automatic JSON parsing.

#### Scenario: Successful API call
- **WHEN** app calls `/api/v1/cars` with valid token
- **THEN** app receives and parses car list JSON response

#### Scenario: API error handling
- **WHEN** API returns non-2xx status code
- **THEN** app displays appropriate error message (network error / 401 / timeout)

### Requirement: Theme support
The system SHALL support light and dark themes, following system preference by default with manual override option.

#### Scenario: System theme change
- **WHEN** user changes system theme from light to dark
- **THEN** app automatically switches to dark theme

#### Scenario: Manual theme override
- **WHEN** user selects "Dark" in settings
- **THEN** app uses dark theme regardless of system setting
