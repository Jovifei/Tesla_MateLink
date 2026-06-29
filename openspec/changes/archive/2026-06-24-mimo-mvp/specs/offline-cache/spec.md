## ADDED Requirements

### Requirement: Offline data caching
The system SHALL cache the last 30 days of drives and charges data locally for offline viewing.

#### Scenario: View cached data offline
- **WHEN** device has no network connection
- **THEN** app displays cached drives/charges list with "Offline" banner

### Requirement: Stale data indicator
The system SHALL mark cached data as "stale" after 24 hours TTL, while still displaying it.

#### Scenario: Stale data display
- **WHEN** cached data is older than 24 hours
- **THEN** app shows data with "Last updated: X hours ago" indicator

### Requirement: Offline dashboard
The system SHALL display last known vehicle status from cache when offline.

#### Scenario: Dashboard offline
- **WHEN** device is offline and user opens Dashboard
- **THEN** app shows "Offline - showing last known status" banner with cached data
