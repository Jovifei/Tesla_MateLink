# app_mimo iOS BLOCKER 编译修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 app_mimo/ios 的 5 个 BLOCKER 编译错误，使 iOS target 能通过编译。

**Architecture:** 5 个独立的编译阻塞，互不依赖，可按任意顺序修复。每个修复让一处模型/View 字段引用对齐。不引入新功能，仅消除编译错误。

**Tech Stack:** Swift 5 / SwiftUI / Codable struct

**验证限制（重要）:** 当前环境为 Windows，无 Xcode，无法运行 `xcodebuild`。每个任务的验证步骤为**静态一致性检查**（grep 确认字段名匹配 + 语法审查）。最终编译验证需 Jovi 在 Mac 上执行 `xcodebuild -scheme MateLink build`。sonnet 执行时按"静态检查"步骤验证即可。

**工作目录:** `E:/project/tesla_master/app_mimo/ios`

---

## File Structure

| 文件 | 责任 | 修改类型 |
|---|---|---|
| `MateLink/Features/Settings/SettingsView.swift` | B1: 删除重复 MoreView，保留 SettingsView + AboutView | 删除 line 3-22 |
| `MateLink/Core/Models/CarStatus.swift` | B3/B4/B5: 加 TirePressure 计算属性、BatteryHealth 扩展字段、Drive.consumptionKwh 计算属性 | 新增代码 |
| `MateLink/Core/API/ApiClient.swift` | B2: 重写 CarStatus 默认 init（2处）匹配模型33字段 | 替换 line 72, 78 |
| `MateLink/Features/Dashboard/DashboardView.swift` | B3: tirePressure 改用4独立字段 | 替换 line 81-89 |
| `MateLink/Features/Battery/BatteryHealthView.swift` | B4: 4处可选字段加 `?? 默认值` | 替换 line 17,18,20,21,26,28,34 |
| `MateLink/Features/Drives/DriveDetailView.swift` | B5: 删除 mock init 的 `consumptionKwh: 7.8` | 删除 line 552 |

---

### Task 1: B1 — 删除 SettingsView.swift 中重复的 MoreView

**Files:**
- Modify: `MateLink/Features/Settings/SettingsView.swift:1-22`（删除重复 MoreView，保留文件其余 SettingsView + AboutView）

**背景:** `SettingsView.swift` line 3-22 定义了 `struct MoreView`，与 `Features/More/MoreView.swift` 完全重复，导致 Swift `invalid redeclaration` 编译错误。`MoreView.swift` 是 MoreView 的正确位置（Features/More/ 目录）。需删除 SettingsView.swift 里的 MoreView，保留同文件的 SettingsView 和 AboutView。

- [ ] **Step 1: 读取文件确认结构**

Run: `cat MateLink/Features/Settings/SettingsView.swift`
Expected: 文件含 3 个 struct：MoreView(line 3-22)、SettingsView(line 24-43)、AboutView(line 45-55)

- [ ] **Step 2: 删除重复的 MoreView**

将 `MateLink/Features/Settings/SettingsView.swift` line 1-22（从 `import SwiftUI` 到 MoreView 闭合 `}` 即 line 22）替换为：

```swift
import SwiftUI

```

即只保留 `import SwiftUI` 和一个空行，删除整个 `struct MoreView: View { ... }`（line 3-22）。保留下面的 `struct SettingsView` 和 `struct AboutView` 不变。

替换后文件开头应为：
```swift
import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var state: AppState
    ...
```

- [ ] **Step 3: 静态验证 — 确认 MoreView 不再重复**

Run: `grep -rn "struct MoreView" MateLink/`
Expected: 仅输出 `MateLink/Features/More/MoreView.swift:3:struct MoreView: View {`（唯一一处）

Run: `grep -n "struct SettingsView\|struct AboutView" MateLink/Features/Settings/SettingsView.swift`
Expected: 两行输出，确认 SettingsView 和 AboutView 仍在该文件

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/Features/Settings/SettingsView.swift
git commit -m "fix(mimo-ios): remove duplicate MoreView in SettingsView.swift (B1)"
```

---

### Task 2: B5 — 给 Drive 模型加 consumptionKwh 计算属性

**Files:**
- Modify: `MateLink/Core/Models/CarStatus.swift:35-58`（Drive struct）
- Modify: `MateLink/Features/Drives/DriveDetailView.swift:552`（删除 mock init 的 consumptionKwh 参数）

**背景:** `Drive` struct 无 `consumptionKwh` 字段，但 7 处代码引用 `drive.consumptionKwh`（AnnualReportPDFView、StatisticsView、DriveDetailView）。`Drive` 有 `distanceKm: Double` 和 `efficiency: Double`（Wh/km），可计算 `consumptionKwh = distanceKm * efficiency / 1000.0`。加计算属性即可。DriveDetailView.swift:552 的 mock Drive init 传了 `consumptionKwh: 7.8`，加计算属性后成员 init 不含此参数，需删除该行。

- [ ] **Step 1: 给 Drive 加计算属性**

在 `MateLink/Core/Models/CarStatus.swift` 的 `struct Drive` 内，`CodingKeys` enum 之前（即 line 44 `let elevationLoss: Double` 之后、line 46 `enum CodingKeys` 之前）插入：

```swift

    /// 行程能耗 (kWh) = 距离(km) × 效率(Wh/km) / 1000
    var consumptionKwh: Double { distanceKm * efficiency / 1000.0 }
```

- [ ] **Step 2: 删除 DriveDetailView mock init 的 consumptionKwh 参数**

在 `MateLink/Features/Drives/DriveDetailView.swift` line 552，删除整行：

```swift
            consumptionKwh: 7.8,
```

删除后 line 551 `durationMin: 45,` 直接接 line 553 `efficiency: 185,`。

- [ ] **Step 3: 静态验证**

Run: `grep -n "var consumptionKwh" MateLink/Core/Models/CarStatus.swift`
Expected: 输出计算属性定义行

Run: `grep -n "consumptionKwh: 7.8" MateLink/Features/Drives/DriveDetailView.swift`
Expected: 无输出（已删除）

Run: `grep -rn "consumptionKwh" MateLink/`
Expected: 计算属性定义 1 处 + 读取用法 7 处（DriveDetailView:137, AnnualReportPDFView:218,273, StatisticsView:188,338,426），无 `consumptionKwh:` 赋值参数

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/Core/Models/CarStatus.swift app_mimo/ios/MateLink/Features/Drives/DriveDetailView.swift
git commit -m "fix(mimo-ios): add Drive.consumptionKwh computed property (B5)"
```

---

### Task 3: B3 — DashboardView tirePressure 改用4独立字段

**Files:**
- Modify: `MateLink/Features/Dashboard/DashboardView.swift:81-89`

**背景:** DashboardView line 82 `if let t = s.tirePressure` 用 `t.frontLeft/frontRight/rearLeft/rearRight`，但 CarStatus 模型无 `tirePressure` 复合属性，只有4个独立字段 `tirePressureFrontLeft/tirePressureFrontRight/tirePressureRearLeft/tirePressureRearRight`。直接改用4字段，无需新结构。

- [ ] **Step 1: 替换 tire pressure 区块**

将 `MateLink/Features/Dashboard/DashboardView.swift` line 81-89：

```swift
                        // Tire pressure
                        if let t = s.tirePressure {
                            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 10) {
                                MiniCard(icon: "circle.circle", label: "FL", value: "\(String(format:"%.1f",t.frontLeft)) bar")
                                MiniCard(icon: "circle.circle", label: "FR", value: "\(String(format:"%.1f",t.frontRight)) bar")
                                MiniCard(icon: "circle.circle", label: "RL", value: "\(String(format:"%.1f",t.rearLeft)) bar")
                                MiniCard(icon: "circle.circle", label: "RR", value: "\(String(format:"%.1f",t.rearRight)) bar")
                            }.padding(.horizontal)
                        }
```

替换为：

```swift
                        // Tire pressure
                        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 10) {
                            MiniCard(icon: "circle.circle", label: "FL", value: "\(String(format:"%.1f", s.tirePressureFrontLeft)) bar")
                            MiniCard(icon: "circle.circle", label: "FR", value: "\(String(format:"%.1f", s.tirePressureFrontRight)) bar")
                            MiniCard(icon: "circle.circle", label: "RL", value: "\(String(format:"%.1f", s.tirePressureRearLeft)) bar")
                            MiniCard(icon: "circle.circle", label: "RR", value: "\(String(format:"%.1f", s.tirePressureRearRight)) bar")
                        }.padding(.horizontal)
```

- [ ] **Step 2: 静态验证**

Run: `grep -n "tirePressure" MateLink/Features/Dashboard/DashboardView.swift`
Expected: 4 行，均为 `s.tirePressureFrontLeft/Right/RearLeft/RearRight`，无 `s.tirePressure`（无后缀）或 `t.frontLeft`

- [ ] **Step 3: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/Features/Dashboard/DashboardView.swift
git commit -m "fix(mimo-ios): DashboardView use 4 tire pressure fields (B3)"
```

---

### Task 4: B2 — 重写 CarStatus 默认 init 匹配模型33字段

**Files:**
- Modify: `MateLink/Core/API/ApiClient.swift:72` 和 `:78`

**背景:** ApiClient.swift line 72 和 78 两处构造默认 CarStatus（offline），用了模型不存在的字段 `estBatteryRangeKm`、`carVersion`、`tirePressure`，且缺失 `usableBatteryRangeKm`、`pluggedIn`、`tirePressureFrontLeft/Right/RearLeft/RearRight`、`elevation`、`power`。需用模型实际33字段重写。

- [ ] **Step 1: 替换 line 72 的 init**

`MateLink/Core/API/ApiClient.swift` line 72 当前：

```swift
        var s = data.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, idealBatteryRangeKm: 0, estBatteryRangeKm: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, latitude: 0, longitude: 0, heading: 0, speed: 0, shiftState: "", locked: false, sentryMode: false, carVersion: "", tirePressure: nil)
```

替换为：

```swift
        var s = data.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, usableBatteryRangeKm: 0, idealBatteryRangeKm: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, locked: false, sentryMode: false, pluggedIn: false, tirePressureFrontLeft: 0, tirePressureFrontRight: 0, tirePressureRearLeft: 0, tirePressureRearRight: 0, latitude: 0, longitude: 0, elevation: 0, speed: 0, power: 0, heading: 0, shiftState: nil)
```

- [ ] **Step 2: 替换 line 78 的 init**

line 78 当前：

```swift
    func getCarStatus(_ carId: Int) -> CarStatus { data.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, idealBatteryRangeKm: 0, estBatteryRangeKm: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, latitude: 0, longitude: 0, heading: 0, speed: 0, shiftState: "", locked: false, sentryMode: false, carVersion: "", tirePressure: nil) }
```

替换为：

```swift
    func getCarStatus(_ carId: Int) -> CarStatus { data.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, usableBatteryRangeKm: 0, idealBatteryRangeKm: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, locked: false, sentryMode: false, pluggedIn: false, tirePressureFrontLeft: 0, tirePressureFrontRight: 0, tirePressureRearLeft: 0, tirePressureRearRight: 0, latitude: 0, longitude: 0, elevation: 0, speed: 0, power: 0, heading: 0, shiftState: nil) }
```

- [ ] **Step 3: 静态验证**

Run: `grep -n "estBatteryRangeKm\|carVersion\|tirePressure:" MateLink/Core/API/ApiClient.swift`
Expected: 无输出（旧字段已清除）

Run: `grep -c "usableBatteryRangeKm: 0" MateLink/Core/API/ApiClient.swift`
Expected: `2`（两处 init 都含新字段）

Run: `grep -c "shiftState: nil" MateLink/Core/API/ApiClient.swift`
Expected: `2`

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/Core/API/ApiClient.swift
git commit -m "fix(mimo-ios): rewrite CarStatus default init to match 33-field model (B2)"
```

---

### Task 5: B4 — BatteryHealth 模型扩展 + BatteryHealthView 可选兜底

**Files:**
- Modify: `MateLink/Core/Models/CarStatus.swift:83-93`（BatteryHealth struct）
- Modify: `MateLink/Features/Battery/BatteryHealthView.swift:17,18,20,21,26,28,34`

**背景:** BatteryHealthView 用 `d.capacityDegradationPercent`、`d.originalCapacityKwh`、`d.currentCapacityKwh`、`d.mileageKm`、`d.history`（数组，元素含 `date`/`capacityKwh`），但 `BatteryHealth` 模型只有 8 个基础字段（carId/date/batteryLevel/ratedRangeKm/idealRangeKm/odometer/outsideTemp/usableBatteryLevel），mock_data.json 的 `battery_health` 也只有这8字段。

修复策略：给 BatteryHealth 加 4 个**可选**字段（Codable 缺键自动 nil，无需改 mock_data.json）+ `mileageKm` 计算属性（=odometer）+ 定义 `BatteryHealthPoint` 历史点结构。BatteryHealthView 的引用处加 `?? 默认值` 兜底。这样编译通过；mock 模式下这些可选字段为 nil，View 显示默认值（功能不完整但不崩溃，符合 BLOCKER 修复目标；后续可补 mock 数据）。

- [ ] **Step 1: 扩展 BatteryHealth 模型**

将 `MateLink/Core/Models/CarStatus.swift` line 83-93 的 `struct BatteryHealth`：

```swift
struct BatteryHealth: Codable {
    let carId: Int; let date: String; let batteryLevel: Int
    let ratedRangeKm: Double; let idealRangeKm: Double
    let odometer: Double; let outsideTemp: Double; let usableBatteryLevel: Int

    enum CodingKeys: String, CodingKey {
        case carId = "car_id"; case date; case batteryLevel = "battery_level"
        case ratedRangeKm = "rated_range_km"; case idealRangeKm = "ideal_range_km"
        case odometer; case outsideTemp = "outside_temp"; case usableBatteryLevel = "usable_battery_level"
    }
}
```

替换为：

```swift
struct BatteryHealthPoint: Codable, Identifiable {
    var id: String { date }
    let date: String
    let capacityKwh: Double
}

struct BatteryHealth: Codable {
    let carId: Int; let date: String; let batteryLevel: Int
    let ratedRangeKm: Double; let idealRangeKm: Double
    let odometer: Double; let outsideTemp: Double; let usableBatteryLevel: Int
    // 可选扩展字段：API/mock 未提供时为 nil，View 用默认值兜底
    let capacityDegradationPercent: Double?
    let originalCapacityKwh: Double?
    let currentCapacityKwh: Double?
    let history: [BatteryHealthPoint]?

    /// 里程 (km)，复用 odometer
    var mileageKm: Double { odometer }

    enum CodingKeys: String, CodingKey {
        case carId = "car_id"; case date; case batteryLevel = "battery_level"
        case ratedRangeKm = "rated_range_km"; case idealRangeKm = "ideal_range_km"
        case odometer; case outsideTemp = "outside_temp"; case usableBatteryLevel = "usable_battery_level"
        case capacityDegradationPercent = "capacity_degradation_percent"
        case originalCapacityKwh = "original_capacity_kwh"
        case currentCapacityKwh = "current_capacity_kwh"
        case history
    }
}
```

- [ ] **Step 2: BatteryHealthView 引用处加兜底**

`MateLink/Features/Battery/BatteryHealthView.swift` 修改以下行：

line 17，将：
```swift
                                Circle().trim(from: 0, to: (100-d.capacityDegradationPercent)/100).stroke(Color.blue, style: StrokeStyle(lineWidth: 10, lineCap: .round)).rotationEffect(.degrees(-90)).frame(width: 120, height: 120)
```
替换为：
```swift
                                Circle().trim(from: 0, to: (100-(d.capacityDegradationPercent ?? 0))/100).stroke(Color.blue, style: StrokeStyle(lineWidth: 10, lineCap: .round)).rotationEffect(.degrees(-90)).frame(width: 120, height: 120)
```

line 18，将：
```swift
                                VStack { Text("\(Int(100-d.capacityDegradationPercent))%").font(.title).bold(); Text("Health").font(.caption2).foregroundColor(.secondary) }
```
替换为：
```swift
                                VStack { Text("\(Int(100-(d.capacityDegradationPercent ?? 0)))%").font(.title).bold(); Text("Health").font(.caption2).foregroundColor(.secondary) }
```

line 20，将：
```swift
                            Text(healthLabel(d.capacityDegradationPercent)).font(.headline).foregroundColor(healthColor(d.capacityDegradationPercent))
```
替换为：
```swift
                            Text(healthLabel(d.capacityDegradationPercent ?? 0)).font(.headline).foregroundColor(healthColor(d.capacityDegradationPercent ?? 0))
```

line 21，`d.mileageKm` 是计算属性（非可选），保持不变。

line 26，将：
```swift
                            VStack(alignment: .leading) { Text("Original").font(.caption).foregroundColor(.secondary); Text("\(String(format:"%.1f",d.originalCapacityKwh)) kWh").font(.title3).bold() }
```
替换为：
```swift
                            VStack(alignment: .leading) { Text("Original").font(.caption).foregroundColor(.secondary); Text("\(String(format:"%.1f", d.originalCapacityKwh ?? 0)) kWh").font(.title3).bold() }
```

line 28，将：
```swift
                            VStack(alignment: .trailing) { Text("Current").font(.caption).foregroundColor(.secondary); Text("\(String(format:"%.1f",d.currentCapacityKwh)) kWh").font(.title3).bold() + Text(" -\(String(format:"%.1f",d.capacityDegradationPercent))%").font(.caption).foregroundColor(.red) }
```
替换为：
```swift
                            VStack(alignment: .trailing) { Text("Current").font(.caption).foregroundColor(.secondary); Text("\(String(format:"%.1f", d.currentCapacityKwh ?? 0)) kWh").font(.title3).bold() + Text(" -\(String(format:"%.1f", d.capacityDegradationPercent ?? 0))%").font(.caption).foregroundColor(.red) }
```

line 34，将：
```swift
                            Chart(d.history, id: \.date) { p in
```
替换为：
```swift
                            Chart(d.history ?? [BatteryHealthPoint(date: "", capacityKwh: 0)], id: \.date) { p in
```

- [ ] **Step 3: 静态验证**

Run: `grep -n "capacityDegradationPercent\|originalCapacityKwh\|currentCapacityKwh\|var mileageKm\|struct BatteryHealthPoint" MateLink/Core/Models/CarStatus.swift`
Expected: 模型扩展字段定义 + BatteryHealthPoint 结构 + mileageKm 计算属性均存在

Run: `grep -n "d.capacityDegradationPercent\|d.originalCapacityKwh\|d.currentCapacityKwh\|d.history" MateLink/Features/Battery/BatteryHealthView.swift`
Expected: 所有引用均带 `?? 默认值`（capacityDegradationPercent/originalCapacityKwh/currentCapacityKwh 用 `?? 0`，history 用 `?? [...]`）；`d.mileageKm` 不需兜底（计算属性）

Run: `grep -c "??" MateLink/Features/Battery/BatteryHealthView.swift`
Expected: 至少 `5`（line 17,18,20,26,28,34 共6处兜底）

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/Core/Models/CarStatus.swift app_mimo/ios/MateLink/Features/Battery/BatteryHealthView.swift
git commit -m "fix(mimo-ios): extend BatteryHealth with optional fields + View fallbacks (B4)"
```

---

### Task 6: 最终静态一致性验证

**Files:** 无修改，仅验证

- [ ] **Step 1: 验证所有 BLOCKER 字段对齐**

Run（在 `E:/project/tesla_master/app_mimo/ios` 目录）:
```bash
echo "=== B1: MoreView 唯一 ===" && grep -rn "struct MoreView" MateLink/ | wc -l
echo "=== B2: 旧字段已清除 ===" && grep -rn "estBatteryRangeKm\|carVersion\|tirePressure: nil\|tirePressure:" MateLink/Core/API/ApiClient.swift | wc -l
echo "=== B3: DashboardView 无 s.tirePressure 复合 ===" && grep -n "s.tirePressure\b" MateLink/Features/Dashboard/DashboardView.swift | wc -l
echo "=== B4: BatteryHealthView 兜底 ===" && grep -c "??" MateLink/Features/Battery/BatteryHealthView.swift
echo "=== B5: consumptionKwh 计算属性 ===" && grep -n "var consumptionKwh" MateLink/Core/Models/CarStatus.swift
```
Expected:
- B1: `1`（MoreView 唯一）
- B2: `0`（旧字段清除）
- B3: `0`（无复合 tirePressure 引用）
- B4: `>=5`（兜底数）
- B5: 输出计算属性行

- [ ] **Step 2: 验证 git 状态干净**

Run: `cd E:/project/tesla_master && git status --short`
Expected: 无未提交变更（5 个 commit 已完成）

- [ ] **Step 3: 输出最终验证报告（交 Jovi 在 Mac 编译）**

提示 Jovi：在 Mac 上执行 `xcodebuild -project app_mimo/ios/MateLink.xcodeproj -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build` 确认编译通过。若仍有错误，把错误贴回。

---

## Self-Review

**1. Spec coverage:** 5 个 BLOCKER（B1-B5）各对应一个 Task。B1→Task1, B2→Task4, B3→Task3, B4→Task5, B5→Task2。Task6 最终验证。全覆盖。

**2. Placeholder scan:** 每个 step 含完整代码或确切 grep 命令 + expected 输出。无 TBD/TODO。B4 的 `?? 默认值` 明确为 `?? 0` 和 `?? [BatteryHealthPoint(date: "", capacityKwh: 0)]`。

**3. Type consistency:**
- `consumptionKwh` 计算属性用 `distanceKm * efficiency / 1000.0`（Double），与所有读取处（`.consumptionKwh`）类型一致 ✓
- CarStatus init 33 字段顺序与模型 line 6-18 定义一致 ✓
- `BatteryHealthPoint` 的 `date: String`/`capacityKwh: Double` 与 View line 35 `.value("kWh", p.capacityKwh)` 一致 ✓
- `mileageKm` 计算属性返回 `Double`，View line 21 `d.mileageKm.formatted()` 合法 ✓
- `shiftState: nil` 与模型 `shiftState: String?` 一致 ✓

**4. 顺序依赖:** Task 间无依赖（不同文件/不同字段）。Task 2 和 Task 5 都改 CarStatus.swift，但改不同 struct（Drive vs BatteryHealth），无冲突。可任意顺序执行。

**5. 风险点:**
- B4 的可选字段方案让 mock 模式下 BatteryHealthView 显示 0%/0 kWh（功能不完整但编译通过）。这是 BLOCKER 修复的权衡，非功能完善。后续应补 mock_data.json 的 battery_health 扩展字段。
- 无法在 Windows 编译验证，依赖 Jovi Mac 端 `xcodebuild` 最终确认。
