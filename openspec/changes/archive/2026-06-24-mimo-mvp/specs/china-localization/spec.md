## ADDED Requirements

### Requirement: Amap integration for China
The system SHALL use Amap (高德地图) SDK for map display when locale is zh-CN, and system default maps otherwise.

#### Scenario: Chinese locale uses Amap
- **WHEN** device locale is zh-CN
- **THEN** all maps render using Amap SDK

#### Scenario: Non-Chinese locale uses system maps
- **WHEN** device locale is en-US
- **THEN** maps render using Apple Maps (iOS) or Google Maps (Android)

### Requirement: GCJ-02 coordinate correction
The system SHALL convert WGS-84 coordinates (from TeslaMate) to GCJ-02 for display on Amap, using standard offset algorithm.

#### Scenario: Coordinates align on Amap
- **WHEN** TeslaMate reports position at WGS-84 coordinates
- **THEN** marker appears at correct location on Amap (not offset)

### Requirement: Time-of-use tariff calculation
The system SHALL calculate charging cost based on configurable peak/flat/valley electricity rates and time periods.

#### Scenario: Default tariff rates
- **WHEN** user has not configured custom rates
- **THEN** app uses defaults: peak ¥1.0/kWh (10-15, 18-21), flat ¥0.7/kWh (7-10, 15-18, 21-23), valley ¥0.3/kWh (23-7)

#### Scenario: Custom tariff rates
- **WHEN** user configures custom peak rate to ¥1.2/kWh
- **THEN** all subsequent cost calculations use ¥1.2 for peak period

### Requirement: Chinese UI
The system SHALL display all UI text in Simplified Chinese when locale is zh-CN.

#### Scenario: Chinese text display
- **WHEN** device locale is zh-CN
- **THEN** all labels, buttons, and messages display in Chinese
