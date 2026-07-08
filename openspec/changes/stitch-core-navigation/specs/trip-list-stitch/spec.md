## ADDED Requirements

### Requirement: Trip list grouped by month
The system SHALL display trips grouped by month with month headers (e.g., "2026年6月"). Each trip row shows: start address → end address, date, distance (km), duration (h:m), battery level change (start% → end%).

#### Scenario: Trip list loads
- **WHEN** user navigates to Trips tab
- **THEN** trips display grouped by month, most recent month first

#### Scenario: Empty state
- **WHEN** no trip data exists
- **THEN** show "No trips recorded" in body-lg, centered

### Requirement: Trip row style
Each trip row SHALL use Stitch card style: white background, 1px `#E5E5E5` border, 8px radius, with address on left (body-lg), date/distance/duration on right (body-sm, right-aligned).

#### Scenario: Trip row rendering
- **WHEN** trip list displays
- **THEN** each row has white background, 1px border, 8px radius, addresses in Inter body-lg, metrics in body-sm

### Requirement: Efficiency badge
The system SHALL display an efficiency badge (green) on trips where efficiency exceeds a threshold (e.g., >90% rated).

#### Scenario: High efficiency trip
- **WHEN** a trip has efficiency > 90%
- **THEN** row shows green "Efficient" badge

### Requirement: Tap to navigate
The system SHALL allow tapping a trip row to navigate to trip detail (detail page in Change #2).

#### Scenario: User taps trip
- **WHEN** user taps a trip row
- **THEN** navigation to trip detail screen triggers
