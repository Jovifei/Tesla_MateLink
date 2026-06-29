## ADDED Requirements

### Requirement: Dashboard 地图展示
Android Dashboard SHALL 在 LocationCard 中展示实际地图，标记车辆当前位置。

#### Scenario: 有位置数据时显示地图
- **WHEN** Dashboard 加载车辆状态且 `latitude`/`longitude` 非空
- **THEN** LocationCard 展示 OpenStreetMap 地图，车辆位置标记为 marker

#### Scenario: 无位置数据时显示占位
- **WHEN** 车辆状态中 latitude 或 longitude 为空
- **THEN** LocationCard 展示 "No location data" 文字提示，不显示空白地图

#### Scenario: 点击地图跳转
- **WHEN** 用户点击 LocationCard 地图
- **THEN** 导航到全屏地图视图（预留路由）

## Open Items

> T7 audit 2026-06-24: 以下场景当前未满足，需后续 change 补全。

1. **Spec/Design 漂移**: 本 spec 写 "OpenStreetMap 地图"，但 `design.md` D-7 明确选择高德 3D Map SDK。以 design.md 为准。
2. **实现为骨架占位**: `DashboardScreen.kt` 的 `LocationCard` 仅显示文字坐标 + "🗺️ Map integration pending: configure AMap API key in AndroidManifest.xml" 占位符，无实际地图渲染。
3. **AmapComposeView.kt 存在但未接入**: `app_mimo/android/.../AmapComposeView.kt` 是完整的高德 MapView 封装（Marker、Polyline、生命周期管理），但零屏幕引用，属于 dead code。
4. **"点击地图跳转"未实现**: 无全屏地图路由或 Navigation 注册。
5. **需后续工作**: 将 AmapComposeView 接入 LocationCard + 注册全屏地图路由 + 配置 AMap API key。
