## ADDED Requirements

### Requirement: Bottom tab bar with 4 tabs
The system SHALL display a fixed bottom navigation bar with 4 tabs: 仪表盘 (Dashboard), 行程 (Trips), 充电 (Charging), 更多 (More). Active tab icon SHALL render in `#A16207` (gold), inactive tabs in `#747878` (outline).

#### Scenario: App launches
- **WHEN** app starts
- **THEN** Dashboard tab is selected by default with gold icon color

#### Scenario: Tab switch
- **WHEN** user taps "行程" tab
- **THEN** Trip list page displays, "行程" icon turns gold, previous tab icon turns gray

### Requirement: Tab bar visual style
The tab bar SHALL have white `#fdf8f8` background, 1px `#E5E5E5` top border, 24px stroke-based icons (1.5px weight), label-caps text below icons.

#### Scenario: Tab bar rendering
- **WHEN** any screen with bottom nav displays
- **THEN** tab bar has white background, 1px top border #E5E5E5, no shadow

### Requirement: Tab bar persistence
The tab bar SHALL remain visible and fixed at the bottom across all 4 main tab pages.

#### Scenario: Scrolling within a tab
- **WHEN** user scrolls trip list
- **THEN** bottom tab bar stays fixed, does not scroll away
