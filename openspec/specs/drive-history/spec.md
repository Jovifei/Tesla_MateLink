# drive-history Specification

## Purpose
TBD - created by archiving change mimo-mvp. Update Purpose after archive.
## Requirements
### Requirement: Drive history list
The system SHALL display a paginated list of drives with date, distance, duration, start/end addresses, and efficiency (Wh/km).

#### Scenario: View drive list
- **WHEN** user taps Drives tab
- **THEN** app displays recent drives in reverse chronological order

#### Scenario: Load more drives
- **WHEN** user scrolls to bottom of list
- **THEN** app loads next page of 20 drives

### Requirement: Drive detail with route map
The system SHALL display drive detail with full route on map (polyline), start/end markers, and speed/power/altitude curves.

#### Scenario: View drive route
- **WHEN** user taps a drive in the list
- **THEN** app shows detail page with route map, statistics, and switchable charts

### Requirement: Route simplification
The system SHALL apply route point simplification to avoid rendering thousands of points on mobile map, using Douglas-Peucker or similar algorithm.

#### Scenario: Long drive renders smoothly
- **WHEN** drive has 5000+ position points
- **THEN** map renders route without frame drops (≥ 30fps)

