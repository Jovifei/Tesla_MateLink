## ADDED Requirements

### Requirement: Mock mode toggle
The system SHALL provide a "Mock Mode" toggle in Settings that switches the app to use built-in mock data instead of live API calls.

#### Scenario: Enable mock mode
- **WHEN** user enables Mock Mode in Settings
- **THEN** app displays confirmation dialog, then switches to mock data

#### Scenario: Mock mode indicator
- **WHEN** Mock Mode is active
- **THEN** app shows "Mock Mode" banner at top of Dashboard

### Requirement: Mock data content
The system SHALL include built-in mock data with 1 virtual vehicle and 30 days of history (drives, charges, battery health).

#### Scenario: Mock dashboard displays
- **WHEN** Mock Mode is enabled
- **THEN** Dashboard shows virtual car "Demo Car" with realistic status data
