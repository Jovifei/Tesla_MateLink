## ADDED Requirements

### Requirement: Battery health overview
The system SHALL display battery health percentage, current capacity vs original capacity, range loss, and total mileage.

#### Scenario: View battery health
- **WHEN** user navigates to Battery Health from More tab
- **THEN** app shows health %, capacity comparison, and degradation info

### Requirement: Battery degradation chart
The system SHALL display a line chart showing battery capacity degradation over time.

#### Scenario: View degradation trend
- **WHEN** user views Battery Health page
- **THEN** app shows line chart with capacity (Y) vs time (X)
