## ADDED Requirements

### Requirement: Analysis entry list
The system SHALL display a scrollable list of analysis page entries. Each entry shows: icon, title (body-lg), brief description (body-sm). Entries: 统计 (Statistics), 热力图 (Heatmap), 效率分析 (Efficiency), 续航分析 (Range), 能耗分析 (Energy), 电池健康 (Battery Health), 时间线 (Timeline).

#### Scenario: More menu displays
- **WHEN** user navigates to More tab
- **THEN** all 7 analysis entries display with icons and descriptions

### Requirement: Report & export section
The system SHALL display entries for: 年度报告 PDF, CSV/JSON 导出, 固件版本.

#### Scenario: Export entries
- **WHEN** More menu displays
- **THEN** report and export entries appear below analysis entries in a separated section

### Requirement: Entry tap navigation
The system SHALL navigate to the corresponding detail/analysis page when an entry is tapped. Pages not yet implemented SHALL show placeholder or navigate to stub.

#### Scenario: Tap existing page
- **WHEN** user taps "Statistics" entry
- **THEN** navigation to Statistics screen triggers

### Requirement: Entry row style
Each entry row SHALL use Stitch card style within the list: white background, 1px `#E5E5E5` bottom border (not full card border, since entries are in a grouped list).

#### Scenario: Entry rendering
- **WHEN** More menu displays
- **THEN** entries render with body-lg title, body-sm description, right chevron icon
