# app_mimo 剩余修复设计

> **日期**: 2026-07-05
> **分支**: `main`
> **目标**: 修复 5 个 Medium Bug + 补齐 iOS "添加实例" 页面

---

## 修复清单 (6 项)

### Bug 1: iOS RangePageView accuracy 用电池百分比而非续航里程

**文件**: `app_mimo/ios/MateLink/Features/Range/RangeView.swift`
**问题**: `loadData()` 将 `startBatteryLevel`/`endBatteryLevel` 作为 estimated/actual，实际是电量消耗而非续航偏差
**修复**: 改用 `startIdealRangeKm - endIdealRangeKm` 作为预估续航消耗，`distanceKm` 作为实际续航，accuracy = `(1 - abs(estimated - actual) / estimated) * 100`

### Bug 2: iOS DashboardView Timer 闭包捕获问题

**文件**: `app_mimo/ios/MateLink/Features/Dashboard/DashboardView.swift`
**问题**: `Timer.scheduledTimer` 闭包直接捕获 view 引用
**修复**: 改用 `onReceive(Timer.publish(...))` 模式，与 SwiftUI 生命周期绑定

### Bug 3: Android ApiClient cachedApi 竞态

**文件**: `app_mimo/android/.../data/api/ApiClient.kt`
**问题**: `cachedApi` 的 get() 是 check-then-act，非原子操作
**修复**: 用 `synchronized(this)` 包裹 get() 的 null 检查 + 创建 + 赋值

### Bug 4: Android CostScreen loading Box 缺 padding

**文件**: `app_mimo/android/.../screens/cost/CostScreen.kt`
**问题**: loading 状态的 Box 未应用 scaffold padding，被 TopAppBar 遮挡
**修复**: `Modifier.fillMaxSize().padding(padding)`

### Bug 5: Android RangeScreen/TimelineScreen 用 collectAsState()

**文件**: `RangeScreen.kt`, `TimelineScreen.kt`
**问题**: 用 `collectAsState()` 而非 `collectAsStateWithLifecycle()`，后台仍收集
**修复**: 改为 `collectAsStateWithLifecycle()`

### Feature 6: iOS "添加实例" 页面

**文件**: 新建 `app_mimo/ios/MateLink/Features/Settings/AddInstanceView.swift`
**问题**: Android 有实例管理 UI，iOS 无
**修复**: 创建 SwiftUI 表单页：服务器 URL 输入、API Token 输入、连接测试、保存。参考 Android `InstanceViewModel` 模式

---

## 不在范围

- UI 规范 (已完成)
- 数据层 (已完成)
- 页面补全 (已完成)
