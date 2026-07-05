## 1. Shared Design Tokens

- [x] 1.1 Create `app_glm/shared/design-tokens.json` — color palette, typography scale, spacing grid, card spec, tab bar spec
- [x] 1.2 Verify JSON schema: all 28 named colors, 7 typography levels, 5 spacing steps, component specs

## 2. Android — Theme & Components

- [x] 2.1 Rewrite `ui/theme/Color.kt` — replace Material3 colors with Stitch palette (28 colors)
- [x] 2.2 Rewrite `ui/theme/Theme.kt` — Inter + JetBrains Mono typography, 8px grid, no shadows
- [x] 2.3 Create `ui/components/StitchCard.kt` — 1px #E5E5E5 border, 8px radius, 24px padding, white bg, no elevation
- [x] 2.4 Create `ui/components/StitchStatusChip.kt` — 4px radius, tinted bg, dark text (Online green / Offline gray / Charging orange)
- [x] 2.5 Create `ui/components/StitchDataRow.kt` — label-caps label + data-md (JetBrains Mono) value, right-aligned
- [x] 2.6 Bundle JetBrains Mono Regular + Medium (.ttf) into `res/font/`, declare in Typeface

## 3. Android — Bottom Navigation

- [x] 3.1 Rewrite `ui/navigation/NavGraph.kt` bottom bar: white bg, 1px #E5E5E5 top border, 4 tabs (Dashboard/Trips/Charging/More)
- [x] 3.2 Tab icons: 24dp stroke-based (1.5px weight), active=#A16207, inactive=#747878
- [x] 3.3 Tab labels: label-caps style (Inter 12px 700, 0.05em letter-spacing)

## 4. Android — Dashboard Screen

- [x] 4.1 Rewrite `DashboardScreen.kt` Composable — white bg, status header, battery card, charging card, info grid, 7-day chart
- [x] 4.2 Vehicle status header: car name (headline-md), Online/Offline chip, last-seen timestamp
- [x] 4.3 Battery card (StitchCard): battery% (data-lg JetBrains Mono), range km, charge limit indicator
- [x] 4.4 Charging card (StitchCard): only visible when pluggedIn=true; shows kW, kWh added, time-to-full
- [x] 4.5 Info grid (2-col): odometer, version, FL/FR/RL/RR tire psi, inside/outside temp — using StitchDataRow
- [x] 4.6 7-day trend chart: simple line/bar chart, gold #A16207 line, from battery history data
- [x] 4.7 Wire up existing DashboardViewModel (CarRepository injection preserved, no data layer changes)

## 5. Android — Trip List Screen

- [x] 5.1 Rewrite `DriveListScreen.kt` — month-grouped list, Stitch card rows
- [x] 5.2 Month header: "2026年6月" in headline-md
- [x] 5.3 Trip row: start→end address (body-lg), date/distance/duration (body-sm right-aligned), efficiency badge
- [x] 5.4 Wire up existing DriveViewModel (data layer preserved)
- [x] 5.5 Tap row navigates to DriveDetailScreen (existing, restyle in Change #2)

## 6. Android — Charge List Screen

- [x] 6.1 Rewrite `ChargeListScreen.kt` — active charging card + month-grouped history
- [x] 6.2 Active charging card: only when isCharging=true; SoC%, kW, kWh, time-to-full
- [x] 6.3 Charge row: location (body-lg), date/duration/energy/cost (body-sm), AC/DC label
- [x] 6.4 Header summary: "Total: X kWh · ¥Y" for current month
- [x] 6.5 Wire up existing ChargeViewModel (data layer preserved)
- [x] 6.6 Tap row navigates to ChargeDetailScreen (existing, restyle in Change #2)

## 7. Android — More Menu Screen

- [x] 7.1 Create `MoreScreen.kt` — scrollable list of analysis entries + report/export section
- [x] 7.2 Analysis entries: 7 items (Statistics/Heatmap/Efficiency/Range/Energy/Battery/Timeline), icon + title + description + chevron
- [x] 7.3 Report section: 年度报告 PDF, CSV/JSON 导出, 固件版本
- [x] 7.4 Tap entries navigate to existing screens or stub placeholders (for pages not yet restyled)

## 8. iOS — Theme & Components

- [x] 8.1 Rewrite `Core/Theme/AppTheme.swift` — Stitch colors, Inter+JetBrains Mono fonts, no shadows
- [x] 8.2 Add JetBrains Mono .ttf to iOS bundle, declare in Info.plist
- [x] 8.3 Create reusable StitchCard, StitchStatusChip, StitchDataRow SwiftUI components

## 9. iOS — Bottom Navigation

- [x] 9.1 Rewrite `App/ContentView.swift` TabView — white bg, 1px top border, 4 tabs, gold active / gray inactive
- [x] 9.2 Tab icons: SF Symbols stroke-based equivalents, 24pt, active=#A16207, inactive=#747878

## 10. iOS — Dashboard View

- [x] 10.1 Rewrite `Features/Dashboard/DashboardView.swift` — white bg, status header, battery card, charging card, info grid, trend
- [x] 10.2 Same card layout as Android (4.2–4.6), adapted for SwiftUI

## 11. iOS — Trip List View

- [x] 11.1 Rewrite `Features/Drives/DriveListView.swift` — month-grouped, Stitch card rows
- [x] 11.2 Match Android trip row spec (5.2–5.5)

## 12. iOS — Charge List View

- [x] 12.1 Rewrite `Features/Charges/ChargeListView.swift` — active charging card + history
- [x] 12.2 Match Android charge row spec (6.2–6.6)

## 13. iOS — More View

- [x] 13.1 Create `Features/More/MoreView.swift` — analysis entries + report section
- [x] 13.2 Match Android More spec (7.2–7.4)

## 14. Verification

- [x] 14.1 Android: assembleDebug passes, no compile errors
- [x] 14.2 Android: visual comparison against Stitch screenshots (Dashboard, Trips, Charging, More)
- [x] 14.3 iOS: Xcode build passes (verify by file structure review if no Mac)
- [x] 14.4 Double-check: no shadows/elevation anywhere, all cards use 1px border
- [x] 14.5 Double-check: JetBrains Mono used for ALL numerical values, Inter for ALL text
