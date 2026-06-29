# android-amap-integration Specification

## Purpose
TBD - created by archiving change glm-p1-china. Update Purpose after archive.
## Requirements
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

