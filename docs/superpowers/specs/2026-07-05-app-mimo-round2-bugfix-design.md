# app_mimo 二轮修复设计

> **日期**: 2026-07-05
> **分支**: `codex/app-mimo-stitch-1to1`
> **目标**: 修复二轮审查的 P0 阻断 + P1 数据层问题（10 项）

---

## 修复清单

### P0 — 阻断级 (3 项)

| # | 平台 | 文件 | 问题 | 修复 |
|---|------|------|------|------|
| 1 | iOS | `mock_data.json` | drive/charge 条目缺 `car_id`，启动崩溃 | 每个条目加 `"car_id": 1` |
| 2 | Android | `EfficiencyViewModel.kt` L53 | `Pair<String,Double>` 赋给 `Pair<Int,Double>` | 改 UiState 为 `List<Pair<String,Double>>`，图表适配 |
| 3 | Android | `CostScreen.kt` | 无 LaunchedEffect 调 load()，永远加载 | 加 `LaunchedEffect(carId) { viewModel.load(carId) }` |

### P1 — 数据层 (7 项)

| # | 平台 | 文件 | 问题 | 修复 |
|---|------|------|------|------|
| 4 | iOS | `CarStatus.swift` Charge | range_rated/range_ideal 映射反了 | 交换映射：`range_rated`→`ratedRangeKm`，`range_ideal`→`idealRangeKm` |
| 5 | iOS | `ApiClient.swift` L83 | battery 端点路径错 | `/battery` → `/battery-health` |
| 6 | iOS | `ApiClient.swift` L91 | settings 端点路径错 | `/settings` → `/globalsettings` |
| 7 | iOS | `ApiClient.swift` GlobalSettings | 响应结构与 Android 不一致 | 改为嵌套解码 `{data: {settings: {teslamate_units: {...}}}}` |
| 8 | iOS | `CarStatus.swift` Charge | `charging_type` 从 JSON 被忽略 | 先读 `charging_type`，fallback 到 `charger_phases` 推导 |
| 9 | iOS | `CarStatus.swift` Drive/Charge/BatteryHealth | `encode(to:)` 丢失字段 | 补完整 encode 或改 `Codable` 为 `Decodable` |
| 10 | iOS | `ApiClient.swift` getCar | 拉全部车再过滤 | 改为直接调 `/api/v1/cars/{carId}` |

---

## 不在范围

- UI 规范偏差 (字体/颜色/卡片样式) — 推迟
- Medium Bug (RangePage accuracy / Timer capture / ApiClient race) — 推迟
- Low Bug — 推迟
