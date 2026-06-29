# settings Specification

## Purpose
TBD - created by archiving change mimo-mvp. Update Purpose after archive.
## Requirements
### Requirement: Server configuration
The system SHALL allow users to configure TeslaMateApi server URL and API token, with a "Test Connection" button that validates connectivity.

#### Scenario: Configure server
- **WHEN** user enters server URL and token, taps "Test Connection"
- **THEN** app calls `/api/v1/cars` and shows success/failure result

### Requirement: Unit preferences
The system SHALL support km/mile and Celsius/Fahrenheit unit preferences.

#### Scenario: Switch to miles
- **WHEN** user selects "Miles" in settings
- **THEN** all distances display in miles, all speeds in mph

### Requirement: Multi-car switching
The system SHALL allow switching between multiple vehicles when the TeslaMate account has more than one car.

#### Scenario: Switch vehicle
- **WHEN** user taps vehicle name on Dashboard and selects another car
- **THEN** Dashboard refreshes with selected car's data

