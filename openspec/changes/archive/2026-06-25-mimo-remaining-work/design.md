## Context

mimo-localization-test 已完成基础架构（高德 SDK 依赖、GCJ-02、地图切换逻辑、分时电价、i18n），剩余工作是将这些基础组件应用到具体页面，以及完成测试。

## Goals / Non-Goals

**Goals:**
- 完成 Android 6 个页面的地图切换
- 完成 iOS 7 个页面的地图切换 + 4 个新页面
- 完成真机测试（Android + iOS + Web）
- 完成异常/边界测试
- 完成最终验证 + 测试报告

**Non-Goals:**
- 不修改已有的基础架构
- 不添加新功能
- 不修改 API 层

## Decisions

### D1: Android 地图切换策略

**选择**：利用 `MapUtils.isChineseLocale()` 条件分支，中文环境用 `AmapComposeView`，其他保留 osmdroid fallback。

**理由**：
- AmapComposeView 已内置 GCJ-02 坐标转换
- 保留 osmdroid 作为 fallback，确保非中国用户正常使用
- 参考 matedroid 的地图使用模式

### D2: iOS 地图切换策略

**选择**：扩展 AmapView.swift，新增 `AmapRouteView` 和 `AmapMultiPointView` 组件。

**理由**：
- AmapView 已有 MapKit fallback + GCJ-02 转换
- 需要支持单点、路线、多点三种模式
- 保留 MapKit fallback 路径

### D3: 测试策略

**选择**：Android → iOS → Web → 异常 → 最终验证

**理由**：
- Android/iOS 是核心用户场景，优先验证
- Web 端可并行
- 异常测试依赖基础功能稳定后再测边界

## Risks / Trade-offs

| 风险 | 影响 | 缓解方案 |
|------|------|----------|
| 高德 API Key 未申请 | 阻塞所有地图任务 | 用户需尽快申请 |
| iOS 需要 Mac | 阻塞 iOS 测试 | 用户需准备 Mac |
| RegionsVisited Polygon 扩展 | Android 需扩展 AmapComposeView | 预留 2h 工作量 |
| 大量轨迹点性能 | 地图渲染卡顿 | 使用 RouteSimplifier 抽稀 |
