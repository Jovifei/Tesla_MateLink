# android-map-dashboard Specification

## Purpose
TBD - created by archiving change glm-review-fix-critical. Update Purpose after archive.
## Requirements
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

