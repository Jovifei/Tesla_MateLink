## ADDED Requirements

### Requirement: Vehicle status header
The system SHALL display vehicle name, online/offline status chip, and last-seen timestamp at the top of the dashboard.

#### Scenario: Vehicle online
- **WHEN** vehicle is online
- **THEN** status chip shows "Online" with green tint background

#### Scenario: Vehicle offline
- **WHEN** vehicle is offline
- **THEN** status chip shows "Offline" with gray tint background

### Requirement: Battery status card
The system SHALL display battery percentage (large JetBrains Mono number), estimated range in km, and charge limit percentage. Card uses Stitch card style (white, 1px border, 8px radius, 24px padding).

#### Scenario: Battery display
- **WHEN** dashboard loads with car data
- **THEN** battery card shows "78%" in data-lg (JetBrains Mono 24px), "312 km" range in data-md, charge limit "80%" indicator

### Requirement: Charging status card
The system SHALL display charging state: plugged in / charging / DC charging, charger power in kW, energy added in kWh, time to full charge. Only visible when vehicle is plugged in.

#### Scenario: DC fast charging
- **WHEN** vehicle is DC charging at 120kW
- **THEN** charging card shows "DC Fast Charging", "120 kW", energy added, estimated completion time

### Requirement: Info grid
The system SHALL display a 2-column grid showing: odometer, software version, tire pressures (4 wheels), inside/outside temperatures.

#### Scenario: Info grid rendering
- **WHEN** dashboard loads
- **THEN** grid shows odometer (km), version, FL/FR/RL/RR tire pressures in psi, inside temp, outside temp, each with label-caps label and data-md value

### Requirement: 7-day trend chart
The system SHALL display a 7-day battery level trend as a simple line or bar chart at the bottom of the dashboard.

#### Scenario: Trend data available
- **WHEN** 7 days of battery data exists
- **THEN** trend chart renders with 7 data points, gold `#A16207` line color
