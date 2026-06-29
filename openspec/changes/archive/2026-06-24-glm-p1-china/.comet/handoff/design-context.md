# Comet Design Handoff

- Change: glm-p1-china
- Phase: design
- Mode: compact
- Context hash: 7c51f7cf33df1e09700e4c4f277c8cf411dbe6cc3ce2b1353e23baacede95d69

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p1-china/proposal.md

- Source: openspec/changes/glm-p1-china/proposal.md
- Lines: 1-57
- SHA256: 6c9a961e1cf95d01f891073f9d45dace59adafa6aad45c18ecf718396cafe9a9

```md
## Why

`glm-review-fix-critical` 已修复 Android Dashboard 地图占位（B5）和 GCJ02Converter（B8），但地图渲染仍是文本占位（"🗺️ Map integration pending"）。中国市场是核心目标，需要 Android 集成真实高德地图 SDK + iOS 完善已有 GCJ-02 + MapKit 方案。同时 TOU 分时电价已在 iOS 有数据模型（TariffConfigView.swift），但成本计算未接入 CostView。

## What Changes

### iOS (T-101 ~ T-104) — 不引入新 SDK
- **T-101**: 提取 TariffConfigView 硬编码中文字符串到 `Localizable.strings` (en + zh-Hans)
- **T-102**: GCJ02Converter 增加 HK/Macau 特例（不应用偏移）
- **T-103**: TariffConfigView DatePicker locale 改用 `Locale.current`，不硬编码 zh_Hans_CN
- **T-104**: 在 CostView 接入 `TariffConfig.priceForHour()` 显示 TOU 估算成本（无配置 UI，仅 hardcode 默认 ¥1.0/0.7/0.3）

### Android (T-201 ~ T-207) — 端口 app_mimo 现成实现
- **T-201**: 添加高德 SDK 依赖 `amap3d:9.6.1` + `amaplocation:6.4.5` 到 gradle
- **T-202**: AndroidManifest 配置 AMap API key meta-data + 定位权限 + OpenGL ES
- **T-203**: 端口 `AmapComposeView.kt`（TextureMapView + DisposableEffect 生命周期管理）
- **T-204**: 创建 `MapUtils.kt` 含 `isChineseLocale()` 判断
- **T-205**: `DashboardScreen.LocationCard` 替换占位为 `AmapComposeView`（仅 zh locale）
- **T-206**: 创建 `TariffConfig.kt` 数据类 + hardcoded 默认费率（无配置 UI）
- **T-207**: `CostScreen` 集成 hardcoded TOU 计算显示

### 共享 (T-301 ~ T-302)
- **T-301**: 验证 iOS/Android GCJ-02 输出一致（同坐标输入 → 同输出，<1m 误差）
- **T-302**: 同步 `app_glm/shared/api-types.ts` 含 TariffConfig schema

## Capabilities

### New Capabilities
- `android-amap-integration`: Android 端高德地图 SDK 集成
- `china-hk-macau-handling`: HK/Macau 坐标特例处理（GCJ-02 跳过偏移）
- `tou-tariff-cost-display`: 分时电价成本显示（默认费率，无配置）

### Modified Capabilities
无（不改变现有 spec 行为）

## Impact

### Files Modified
- iOS: `Core/Utils/GCJ02Converter.swift`, `Features/Cost/TariffConfigView.swift`, `Features/Cost/CostView.swift`, `Resources/en.lproj/Localizable.strings`, `Resources/zh-Hans.lproj/Localizable.strings`
- Android: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `ui/dashboard/DashboardScreen.kt`, `ui/cost/CostScreen.kt`
- Shared: `app_glm/shared/api-types.ts`

### New Files
- Android: `ui/components/AmapComposeView.kt`, `util/MapUtils.kt`, `data/model/TariffConfig.kt`
- 共享: GCJ-02 一致性测试向量

### Dependencies
- 新增 Android: `com.amap.api:3dmap:9.6.1`, `com.amap.api:location:6.4.5`
- 阻塞项: AMap Android API key 由 Jovi 注册（package: `com.teslamatelink`）

## Non-Goals
- TOU 配置 UI（推迟到 `glm-p1-china-polish`）
- 历史充电用 TOU 重算
- 全屏地图路由
- PIPL 隐私政策
- iOS AMap SDK 引入（用 MapKit + iOS 17 Apple Maps 在中国本就用高德瓦片）
- build flavor (china/global)
```

## openspec/changes/glm-p1-china/design.md

- Source: openspec/changes/glm-p1-china/design.md
- Lines: 1-78
- SHA256: 0b25d873fef560471c454a17aa987361af50a38ca286f2e72205ec6751fa6b35

```md
## Context

`glm-review-fix-critical` archived 后，Android Dashboard 地图仍是文本占位。Designer 团队 (D1-D4) 共识：iOS 用 MapKit + 已有 GCJ02Converter 即可，Android 端口 app_mimo 现成 AMap 实现。

## Goals / Non-Goals

**Goals:**
1. Android Dashboard 在 zh locale 渲染真实高德地图 + 车辆 marker
2. iOS TariffConfigView 字符串国际化 + locale 修复
3. CostView/CostScreen 显示 TOU 估算成本（默认费率 hardcode）
4. GCJ-02 跨平台一致性可验证

**Non-Goals:**
- TOU 配置 UI（v2）
- 历史充电 TOU 重算（v2）
- 全屏地图路由（v2）
- PIPL 合规（v2）
- iOS AMap SDK
- build flavor 拆分

## Decisions

### D-1: iOS 不引入 AMap SDK
**选择：** 继续用 MapKit。iOS 17+ Apple Maps 在中国大陆使用 AutoNavi（高德）瓦片，原生 GCJ-02。已有 `GCJ02Converter.swift` 处理 pin 坐标偏移。

**拒绝 AMap SDK：** Licensing 复杂（需 bundle ID 注册，非 App Store 分发要付费），引入新 Pod 增加构建复杂度，MapKit 已满足需求。

### D-2: Android 端口 app_mimo 现成 AMap 实现
**选择：** `app_mimo/android/.../ui/components/AmapComposeView.kt` 已有完整实现（TextureMapView + DisposableEffect 生命周期）。直接端口适配 `com.teslamatelink` 包，不重写。

**拒绝重写：** app_mimo 版本已解决 Compose + AMap 生命周期 gotcha，重写是 NIH。

### D-3: TOU 配置 UI 砍到 v2
**选择：** v1 hardcode 默认费率 ¥1.0/0.7/0.3（峰/平/谷），覆盖 80% 用户。配置 UI 留给 `glm-p1-china-polish`。

**拒绝在 v1 做配置 UI：** 数据模型已有（iOS `TariffConfig`），配置 UI 是 1d 额外工作但价值不大（用户改默认值的频率低）。

### D-4: GCJ-02 转换只在显示层
**选择：** 存储用 WGS-84（Tesla API 原生），ViewModel/Repository 不做转换，仅在 `AmapComposeView.kt`（中国）和 iOS `AmapView`（中国）内部调用 `GCJ02Converter.wgs84ToGcj02()` 后设 marker。

**拒绝存储 GCJ-02：** 数据应保持中立，转换是渲染细节。

### D-5: HK/Macau 特例
**选择：** `GCJ02Converter.isInChina()` 当前 bounding box 包含 HK/Macau，但这两地区实际用 WGS-84（不在 GCJ-02 强制区）。增加 `isInSpecialRegion()` 判断，HK/Macau 跳过偏移。

**Bounding box：**
- HK: lat 22.15-22.55, lng 113.83-114.42
- Macau: lat 22.10-22.22, lng 113.52-113.60

### D-6: 拒绝 MapDisplay 抽象接口（D4 提议）
**选择：** 不抽象 `MapDisplay` 跨平台接口。iOS 和 Android map API 差异太大（UIViewRepresentable vs AndroidView），抽象徒劳。

### D-7: 拒绝 build flavor (D1 提议)
**选择：** 单一 build flavor。中国版和国际版用 runtime locale check (`Locale.getDefault().language == "zh"`) 切换 AMap vs Google Maps。增加 build flavor 是过早优化。

## Risks / Trade-offs

| 风险 | 缓解 |
|---|---|
| **AMap Android API key 未获取** | T-202 检测 key 为空时显示 "Map unavailable" 占位卡片，不 crash |
| **AMap SDK ~8MB 增加 APK** | 可接受，单一 flavor 策略 |
| **HK/Macau 坐标判断边界** | bounding box 测试，若误差大可改为更精确多边形（v2） |
| **iOS MapKit 在中国精度** | Apple Maps 实际用高德瓦片，GCJ02Converter 修正 pin 即可（已验证） |
| **AmapComposeView 端口风险** | app_mimo 版本经测试，端口仅改 package 名 |

## Migration Plan

1. T-101~T-104: iOS 小修小补（无 SDK 变更）
2. T-201~T-205: Android AMap 集成（含 API key 配置文档）
3. T-206~T-207: TOU hardcode（最小改动）
4. T-301~T-302: 共享验证

回滚：每 task 独立 commit，git revert 可逐步回滚。AMap key 缺失场景已有 fallback。

## Open Questions

1. AMap Android API key — Jovi 何时提供？v1 完成时即使 key 缺失，代码逻辑也应完整（只是运行时显示占位）
2. iOS TariffConfig 历史 charge 是否要重算？当前决定：仅显示，不重算（与 v2 范围）
```

## openspec/changes/glm-p1-china/tasks.md

- Source: openspec/changes/glm-p1-china/tasks.md
- Lines: 1-24
- SHA256: 7883afdc89e25d51ea0f6159825801bd255279f4f74a560bb7706a9f3417a038

```md
## 1. iOS 修复与增强

- [ ] T-101 提取 `TariffConfigView.swift` 硬编码中文字符串到 `Localizable.strings`（en + zh-Hans）
- [ ] T-102 `GCJ02Converter.swift` 增加 `isInSpecialRegion()` 检测 HK/Macau 并跳过偏移
- [ ] T-103 修复 `TariffConfigView.swift` DatePicker locale 改为 `Locale.current`，不硬编码 `zh_Hans_CN`
- [ ] T-104 `CostView.swift` 接入 `TariffConfig.priceForHour()` 显示 TOU 估算成本

## 2. Android 高德地图集成

- [ ] T-201 添加 `com.amap.api:3dmap:9.6.1` + `com.amap.api:location:6.4.5` 到 `gradle/libs.versions.toml` 和 `app/build.gradle.kts`
- [ ] T-202 `AndroidManifest.xml` 添加 AMap API key meta-data + 定位权限 + OpenGL ES 配置
- [ ] T-203 端口 `app_mimo` 的 `AmapComposeView.kt` 到 `app_glm/android/.../ui/components/`，包名改为 `com.teslamatelink`
- [ ] T-204 创建 `util/MapUtils.kt` 含 `isChineseLocale()` helper
- [ ] T-205 `DashboardScreen.kt` 的 `LocationCard` 替换文本占位为 `AmapComposeView`（仅 zh locale，缺 key 显示 fallback）

## 3. Android TOU 默认费率

- [ ] T-206 创建 `data/model/TariffConfig.kt`（hardcoded 默认费率，无配置 UI）
- [ ] T-207 `CostScreen.kt` 显示 TOU 估算成本（hardcode 默认）

## 4. 共享验证

- [ ] T-301 验证 iOS/Android `GCJ02Converter` 输出一致（同坐标输入 → 同输出，误差 <1m）
- [ ] T-302 更新 `app_glm/shared/api-types.ts` 添加 TariffConfig schema
```

## openspec/changes/glm-p1-china/specs/android-amap-integration/spec.md

- Source: openspec/changes/glm-p1-china/specs/android-amap-integration/spec.md
- Lines: 1-20
- SHA256: 0c02c6b7d15bc08cc1515a8ba0dc49e4eb7901037e050c6db667cdbca45e271e

```md
## ADDED Requirements

### Requirement: Dashboard 地图在中国 locale 渲染高德地图
Android Dashboard SHALL 在 `Locale.getDefault().language == "zh"` 时，使用高德地图 SDK 渲染 LocationCard 内的车辆位置。

#### Scenario: 中国 locale + 有效 API key
- **WHEN** 设备 locale 为 zh-CN 且 AMap API key 已配置
- **THEN** LocationCard 渲染高德 TextureMapView，车辆 marker 出现在 GCJ-02 转换后的坐标

#### Scenario: 中国 locale + 无 API key
- **WHEN** 设备 locale 为 zh-CN 但 AMap API key 缺失
- **THEN** LocationCard 显示 "Map unavailable, configure AMap API key" 占位文字，不 crash

#### Scenario: 非中国 locale
- **WHEN** 设备 locale 为 en-US 或其他非 zh
- **THEN** LocationCard 不加载 AMap SDK，使用现有地图方案（占位或 Google Maps）

#### Scenario: 地图生命周期管理
- **WHEN** Composable 进入/退出/暂停/恢复
- **THEN** TextureMapView 正确调用对应生命周期方法（DisposableEffect 处理 onCreate/onResume/onPause/onDestroy）
```

## openspec/changes/glm-p1-china/specs/china-hk-macau-handling/spec.md

- Source: openspec/changes/glm-p1-china/specs/china-hk-macau-handling/spec.md
- Lines: 1-20
- SHA256: d1ceb26094bcfe007dbf886301e7ee27bdbf804996835a2f8698ce1404d35b48

```md
## ADDED Requirements

### Requirement: HK/Macau 坐标跳过 GCJ-02 偏移
GCJ02Converter SHALL 识别香港和澳门特别行政区的坐标，并在 `wgs84ToGcj02()` 调用时跳过偏移转换（这两地区实际使用 WGS-84）。

#### Scenario: 香港坐标
- **WHEN** 输入坐标在 HK 范围（lat 22.15-22.55, lng 113.83-114.42）
- **THEN** `wgs84ToGcj02()` 返回原坐标，不应用偏移

#### Scenario: 澳门坐标
- **WHEN** 输入坐标在 Macau 范围（lat 22.10-22.22, lng 113.52-113.60）
- **THEN** `wgs84ToGcj02()` 返回原坐标，不应用偏移

#### Scenario: 中国大陆坐标
- **WHEN** 输入坐标在中国大陆（不在 HK/Macau 范围内）
- **THEN** `wgs84ToGcj02()` 应用 GCJ-02 偏移转换

#### Scenario: 中国境外坐标
- **WHEN** 输入坐标在中国境外（isInChina() 返回 false）
- **THEN** `wgs84ToGcj02()` 返回原坐标
```

## openspec/changes/glm-p1-china/specs/tou-tariff-cost-display/spec.md

- Source: openspec/changes/glm-p1-china/specs/tou-tariff-cost-display/spec.md
- Lines: 1-24
- SHA256: 905bfc54565be010f0376969814b1d9c6e28fb49ded77bf0f2fdccaa8670e68f

```md
## ADDED Requirements

### Requirement: CostView 显示 TOU 估算成本
iOS CostView 和 Android CostScreen SHALL 在显示充电成本时，使用默认 TOU 费率（峰 ¥1.0/平 ¥0.7/谷 ¥0.3）计算并展示分时段估算成本。

#### Scenario: 峰时段充电
- **WHEN** 充电开始时间小时 ∈ [9, 12) ∪ [17, 22)
- **THEN** 估算成本 = energyAdded × 1.0

#### Scenario: 谷时段充电
- **WHEN** 充电开始时间小时 ∈ [0, 7)
- **THEN** 估算成本 = energyAdded × 0.3

#### Scenario: 平时段充电
- **WHEN** 充电开始时间小时不在峰/谷时段
- **THEN** 估算成本 = energyAdded × 0.7

#### Scenario: 默认费率显示
- **WHEN** 用户未配置自定义费率
- **THEN** 使用 hardcoded 默认费率，UI 显示 "默认费率" 标注

#### Scenario: TOU 与 API 成本对比
- **WHEN** 充电记录同时有 API 返回的 cost 和 TOU 估算成本
- **THEN** UI 显示两者，标注哪个是 TOU 估算
```

