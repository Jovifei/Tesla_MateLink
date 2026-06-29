## Context

mimo-mvp 交叉审核发现的问题清单：
1. Dashboard 缺 7 天趋势图、门锁/充电线卡片、高电量警告、海拔
2. Web 8 页空壳（EfficiencyCurve、Trips、TopDestinations、Mileage 等）
3. Web 状态管理割裂
4. iOS 缺 SentryEvent/Trip/VisitedRegion 模型
5. 下拉刷新、卡片点击、车辆切换 Modal 未实现

## Goals / Non-Goals

**Goals:**
- 三端 Dashboard 功能完整
- Web 18 页全部有实际交互
- Web 状态管理统一
- iOS 数据模型完整

**Non-Goals:**
- 不添加新功能
- 不修改 API 层
- 不修改 Android/iOS 架构

## Decisions

### D1: Dashboard 趋势图

**选择**：使用 mock_data.json 中的 battery_health 历史数据生成 7 天趋势

**理由**：
- 数据已有，无需额外 API
- 三端统一实现

### D2: Web 状态管理统一

**选择**：删除 App.tsx 中的 useState，统一使用 Zustand store

**理由**：
- store.ts 已定义完整状态
- 消除数据不同步问题

### D3: Web 页面补全策略

**选择**：优先补全核心交互（筛选、分页、图表），其次补全辅助功能

**理由**：
- 核心交互影响用户体验
- 辅助功能可延后

## Risks / Trade-offs

| 风险 | 影响 | 缓解方案 |
|------|------|----------|
| Web 页面重写可能引入 bug | 中 | 逐页面测试 |
| iOS 模型添加可能影响编译 | 低 | 验证编译通过 |
