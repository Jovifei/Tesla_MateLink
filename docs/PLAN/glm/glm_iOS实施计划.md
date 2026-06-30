---
status: final
archived-with: 2026-06-24-glm-mvp-release
---
# Tesla_MateLink iOS 实施计划

> 日期：2026-06-23 | 技术栈：Swift 5.10 + SwiftUI + SwiftData + Swift Charts
> 借鉴：matedroid (Kotlin Clean Architecture) × 10 → Swift 对照实现
> 工程：`E:\project\tesla_master\app_glm\ios/MateLink/` (13 文件已就绪)

## 零、开发环境

| 需求 | 说明 |
|---|---|
| **硬件** | Mac (M1+), 16GB+ RAM |
| **Xcode** | 16.0+ (App Store 下载) |
| **iOS 目标** | iOS 17.0+ |
| **Swift 版本** | 5.10 |
| **模拟器** | iPhone 16 Pro (默认) |
| **真机** | iPhone 12+ (iOS 17+) |
| **账户** | Apple Developer $99/年 |

## 一、Checklist 总览

```
Phase 1: ✅ 工程骨架 (已完成 — 13 Swift 文件)
Phase 2: 📋 Detail Pages (T1-T2)
Phase 3: 📋 Web-Only Pages → iOS (T3-T5)
Phase 4: 📋 中国本地化 (T6-T7)
Phase 5: 📋 集成测试 + 上架 (T8-T9)
```

## 二、Phase 2: Detail Pages 详细实施

### T1 — Drive Detail 页 (借鉴 Web `DriveDetail.tsx` + matedroid)

**数据**：`GET /api/v1/cars/{CarID}/drives/{DriveID}` → Drive
**技术**：SwiftUI NavigationStack + ScrollView + Swift Charts

```
页面布局:
┌───────────────────────────────┐
│ ← Back to Drives              │
├───────────────────────────────┤
│ 📍 Home → Office              │
│ Jun 22, 08:30 — 09:15 · 45min│
├───────────────────────────────┤
│ ┌─────┬─────┬─────┬─────┐    │
│ │Dist │Speed│Max  │Energy│    │ ← HStack 4 stat cards
│ │23.5 │45   │82   │12.3  │    │
│ └─────┴─────┴─────┴─────┘    │
│ ┌─────┬─────┐                 │
│ │85% →│77%  │-8%              │ ← Battery change bar
│ └─────┴─────┘                 │
├───────────────────────────────┤
│ [Speed|Power|Alt|Temp|Tires]  │ ← Picker segment
│                               │
│     📈 Swift Charts Line      │
│     with Brush + Tooltip      │
│                               │
└───────────────────────────────┘
```

**文件**：`Features/Drives/DriveDetailView.swift` (新建, ~120 lines)
**工时**：1d

```swift
// Features/Drives/DriveDetailView.swift
import SwiftUI
import Charts

enum DriveCurveTab: String, CaseIterable { case speed, power, altitude, temp, tires }

struct DriveDetailView: View {
    let drive: Drive
    @State private var tab: DriveCurveTab = .speed

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header: address + time
                // Stat grid: 5 cards
                // Battery bar
                // Picker for curves
                // Chart
            }
        }.navigationTitle("Drive Detail")
    }
}
```

### T2 — Charge Detail 页 (借鉴 Web `ChargeDetail.tsx` + matedroid)

**数据**：`GET /api/v1/cars/{CarID}/charges/{ChargeID}` → Charge
**文件**：`Features/Charges/ChargeDetailView.swift` (新建, ~100 lines)
**工时**：1d

## 三、Phase 3: Web-Only Pages → iOS

### T3 — Statistics 钻取 (借鉴 Web Statistics.tsx + matedroid MileageScreen)

**数据**：`GET /api/v1/cars/{CarID}/drives` → 聚合
**技术**：NavigationStack 链式 push + Swift Charts Bar

```
Year View (默认):
  ┌─────────────────┐
  │ Total: 4,520 km │
  │ 12 month cards   │
  │ Jan .. Dec       │  ← LazyVGrid(flexible:4)
  │ click Mar ▼      │
  └─────────────────┘
       ↓ push
Month View (March):
  ┌─────────────────┐
  │ March 2026       │
  │ 30 day heatmap   │  ← LazyVGrid(flexible:7)
  │ click 15 ▼       │
  └─────────────────┘
       ↓ push
Day View (Mar 15):
  ┌─────────────────┐
  │ 3 drives         │
  │ 08:30 Home→Office│  ← List { ForEach }
  │ 18:00 Office→Home│
  │ click ▼          │
  └─────────────────┘
       ↓ push
Drive Detail (T1)
```

**文件**：`Features/Statistics/StatisticsView.swift` (新建, ~180 lines)
**工时**：1.5d

### T4 — Heatmap 页 (借鉴 Heatmap.tsx)

**数据**：GET /drives 聚合
**技术**：LazyVGrid + SwiftUI Rectangle 颜色映射

```
15d × 24h grid:
  ┌──────────────────────┐
  │ 00 01 .. 23          │ ← hours as columns
  │──────────────────────│
  │ ░▒▓█ (day -14)       │ ← rows as days
  │ ...                  │
  │ ░▒▓█ (today)         │
  └──────────────────────┘
  hover → "14:00 · 23.5km"
  click → 跳转当日
```

**文件**：`Features/Heatmap/HeatmapView.swift` (新建, ~100 lines)
**工时**：0.5d

### T5 — Efficiency + Vampire + Range + Cost + TopDest (简化版)

每个页面 ~80 lines, Swift Charts 实现。
**工时**：5 页 × 0.5d = 2.5d

## 四、Phase 4: 中国本地化

### T6 — 高德地图集成

**文件**：`Core/Map/AmapView.swift`
**逻辑**：`zh-CN` locale → 高德 AMap SDK, GCJ-02 纠偏
**工时**：2d

### T7 — 分时电价

**文件**：`Features/Cost/TariffConfigView.swift`
**逻辑**：峰平谷电价配置 + 充电成本重算
**工时**：1.5d

## 五、Phase 5: 测试 + 上架

### T8 — 真机测试

| 测试项 | 设备 | 验收 |
|---|---|---|
| 启动 < 2s | iPhone 14+ | 冷启动计时 |
| Dashboard 数据加载 | Mock | < 1s |
| 列表滚动 60fps | iPhone 14+ | Instruments |
| 内存 < 200MB | iPhone 12 | Memory Graph |

**工时**：2d

### T9 — App Store 上架

| 步骤 | 说明 |
|---|---|
| App Store Connect 创建 App | bundle id: com.matelink.ios |
| App 预览截图 | 6.7" + 5.5" 各 5 张 |
| App 描述 | 含免责声明 |
| IAP 配置 | PRO 解锁 (一次性 $9.99) |
| 提交审核 | — |

**工时**：3d

## 六、总工时

| Phase | 任务 | 工时 |
|---|---|---|
| 1 | ✅ 工程骨架 | 已完成 |
| 2 | T1 Drive Detail + T2 Charge Detail | 2d |
| 3 | T3 Statistics + T4 Heatmap + T5 简化5页 | 4.5d |
| 4 | T6 高德 + T7 分时电价 | 3.5d |
| 5 | T8 测试 + T9 上架 | 5d |
| — | **总计** | **15d (~3 周)** |

## 七、借鉴对照表 (Kotlin → Swift)

| matedroid 源码 | iOS 实现 | 文件 |
|---|---|---|
| `DashboardScreen.kt` | `DashboardView.swift` | ✅ 已完成 |
| `ChargeListView.kt` | `ChargeListView.swift` | ✅ 已完成 |
| `DriveListView.kt` | `DriveListView.swift` | ✅ 已完成 |
| `BatteryHealthScreen.kt` | `BatteryHealthView.swift` | ✅ 已完成 |
| `SettingsScreen.kt` | `SettingsView.swift` | ✅ 已完成 |
| `ChargeDetailScreen.kt` | `ChargeDetailView.swift` | 📋 T2 |
| `DriveDetailScreen.kt` | `DriveDetailView.swift` | 📋 T1 |
| `MileageScreen.kt` | `StatisticsView.swift` | 📋 T3 |
| `RouteSimplifier.kt` | `RouteSimplifier.swift` | 📋 Utils |
| `TripAggregator.kt` | `TripAggregator.swift` | 📋 Utils |
| `ui/theme/Color.kt` | `AppTheme.swift` | ✅ 已完成 |

## 八、下一步

1. Jovi 确认本计划
2. Mac 就绪后开始 T1—T9
3. 完成后走 comet-verify + comet-archive
