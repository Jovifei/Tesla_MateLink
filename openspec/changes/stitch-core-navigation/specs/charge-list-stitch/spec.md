## ADDED Requirements

### Requirement: Active charging card
The system SHALL display a prominent card at the top of the charge list when the vehicle is currently charging. Card shows: "Charging" status, current SoC%, charger power (kW), energy added (kWh), estimated completion time.

#### Scenario: Vehicle is charging
- **WHEN** vehicle is actively charging
- **THEN** active charging card displays at top with live data

#### Scenario: Vehicle not charging
- **WHEN** vehicle is not plugged in
- **THEN** active charging card is hidden

### Requirement: Charge history list
The system SHALL display past charging sessions grouped by month. Each row shows: location/address, date, duration, energy added (kWh), cost (¥). AC and DC sessions SHALL be visually distinguished.

#### Scenario: Charge list with mixed types
- **WHEN** charge history contains both AC and DC sessions
- **THEN** DC sessions show "DC" label, AC sessions show "AC" label

### Requirement: Charge summary stats
The system SHALL display total energy charged (kWh) and total cost (¥) for the visible period at the top of the list.

#### Scenario: Summary display
- **WHEN** charge list loads
- **THEN** header shows "Total: X kWh · ¥Y" for current month

### Requirement: Charge row style
Each charge row SHALL use Stitch card style: white background, 1px border, 8px radius. Location in body-lg, date/duration/energy/cost in body-sm.

#### Scenario: Charge row rendering
- **WHEN** charge list displays
- **THEN** each row matches Stitch card component spec

### Requirement: Tap to charge detail
The system SHALL allow tapping a charge row to navigate to charge detail (detail page in Change #2).

#### Scenario: User taps charge session
- **WHEN** user taps a charge row
- **THEN** navigation to charge detail screen triggers
