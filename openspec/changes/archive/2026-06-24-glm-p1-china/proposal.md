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
