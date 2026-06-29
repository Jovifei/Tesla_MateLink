## ADDED Requirements

### Requirement: Charge history list
The system SHALL display a paginated list of charging sessions with date, duration, energy added, cost, charging type (AC/DC), and start/end battery level.

#### Scenario: View charge list
- **WHEN** user taps Charges tab
- **THEN** app displays recent charging sessions in reverse chronological order

#### Scenario: Load more charges
- **WHEN** user scrolls to bottom of list
- **THEN** app loads next page of 20 charging sessions

### Requirement: Charge detail view
The system SHALL display detailed charge information including location map, power/voltage/temperature curves over time, and cost breakdown.

#### Scenario: View charge detail
- **WHEN** user taps a charge session in the list
- **THEN** app shows detail page with map, charts, and statistics

### Requirement: Charge filtering
The system SHALL allow filtering charges by AC/DC type and date range.

#### Scenario: Filter by DC only
- **WHEN** user selects "DC" filter
- **THEN** list shows only DC fast charging sessions
