# dashboard Specification

## Purpose
TBD - created by archiving change mimo-mvp. Update Purpose after archive.
## Requirements
### Requirement: Real-time vehicle status display
The system SHALL display vehicle real-time status on the Dashboard, polling TeslaMateApi `/status` endpoint every 5 seconds. Data includes battery level, range, vehicle state, location, tire pressure, and temperature.

#### Scenario: Dashboard shows live data
- **WHEN** user opens Dashboard tab
- **THEN** app displays current battery %, range (km), vehicle state badge, location, tire pressure (4 wheels), and cabin temperature

#### Scenario: Data auto-refreshes
- **WHEN** Dashboard is visible for 5 seconds
- **THEN** app automatically fetches latest status from API

### Requirement: Vehicle state badge
The system SHALL display a colored badge indicating vehicle state: online (green), driving (blue), charging (orange), asleep (gray), offline (dark gray).

#### Scenario: Vehicle is charging
- **WHEN** vehicle state is "charging"
- **THEN** badge shows orange "Charging" text with current power (kW) and ETA

### Requirement: 2D vehicle image with color matching
The system SHALL display a 2D vehicle image matching the car's exterior color. Image changes based on `exterior_color` field from API.

#### Scenario: Car color matches
- **WHEN** car exterior_color is "SolidBlack"
- **THEN** dashboard shows black vehicle image

