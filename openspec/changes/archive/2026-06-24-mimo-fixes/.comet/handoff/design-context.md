# Comet Design Handoff

- Change: mimo-fixes
- Phase: design
- Mode: compact
- Context hash: fbcd7c93119b3192beb089ee4426063fce3683e80afaf1a8b54eb1768cf55164

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/mimo-fixes/proposal.md

- Source: openspec/changes/mimo-fixes/proposal.md
- Lines: 1-37
- SHA256: 2242b279c155c472db2edd54b02bb1a7f51a74369ee67a5a809089742dcf3459

```md
## Why

mimo-mvp 完成交叉审核后发现多个遗留问题：
- Dashboard 缺少 7 天趋势图、门锁/充电线卡片、高电量警告
- Web 8 个页面是空壳/半实现
- Web 状态管理割裂（App.tsx useState vs store.ts zustand）
- iOS 缺少 SentryEvent/Trip/VisitedRegion 数据模型

这些问题影响用户体验和代码质量，需要在下一阶段修复。

## What Changes

- **Dashboard 增强**：三端补充 7 天趋势图、门锁/充电线卡片、高电量警告、海拔显示
- **Web 页面补全**：补全 EfficiencyCurve、Trips、TopDestinations、Mileage 等页面的交互逻辑
- **Web 状态统一**：统一使用 Zustand store，删除 App.tsx 中的重复 useState
- **iOS 模型补全**：添加 SentryEvent、Trip、VisitedRegion 数据模型
- **交互增强**：下拉刷新、卡片点击跳转、车辆切换 Modal

## Capabilities

### New Capabilities

- `dashboard-enhanced`: Dashboard 增强功能（趋势图、门锁卡片、高电量警告）
- `web-pages-complete`: Web 页面补全（交互逻辑、真实数据）
- `state-unified`: Web 状态管理统一

### Modified Capabilities

- `dashboard`: 补充趋势图、门锁/充电线卡片
- `settings`: 补充主题/单位联动逻辑

## Impact

- 三端 Dashboard 页面需要更新
- Web 8 个页面需要重写交互逻辑
- iOS 需要添加 3 个数据模型
- 不影响已有的 API 层和数据层
```

## openspec/changes/mimo-fixes/design.md

- Source: openspec/changes/mimo-fixes/design.md
- Lines: 1-54
- SHA256: 246939309a9a9f25b4258bbf38a84443adea7eca3006a0826746db4e4c287c3d

```md
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
```

## openspec/changes/mimo-fixes/tasks.md

- Source: openspec/changes/mimo-fixes/tasks.md
- Lines: 1-45
- SHA256: 0475e215a9ee092fea098c3e21780d366ad3989290bfe127a795b599d2ec59e5

```md
## 1. Dashboard 增强（三端）

- [ ] T-001 Android Dashboard 添加 7 天电量趋势图
- [ ] T-002 Android Dashboard 添加门锁/充电线状态卡片
- [ ] T-003 Android Dashboard 添加高电量警告（chargeLimitSoc > 90%）
- [ ] T-004 Android Dashboard 添加海拔显示
- [ ] T-005 iOS Dashboard 添加 7 天电量趋势图
- [ ] T-006 iOS Dashboard 添加门锁/充电线状态卡片
- [ ] T-007 iOS Dashboard 添加高电量警告
- [ ] T-008 iOS Dashboard 添加海拔显示
- [ ] T-009 Web Dashboard 添加 7 天电量趋势图
- [ ] T-010 Web Dashboard 添加门锁/充电线状态卡片
- [ ] T-011 Web Dashboard 添加高电量警告
- [ ] T-012 Web Dashboard 添加海拔显示

## 2. Web 状态管理统一

- [ ] T-020 删除 App.tsx 中的重复 useState（currentCarId、mockMode、theme）
- [ ] T-021 统一使用 Zustand store 管理全局状态
- [ ] T-022 修复 Settings 页面主题/单位切换联动

## 3. Web 页面补全

- [ ] T-030 EfficiencyCurve 添加回归线和最优区间高亮
- [ ] T-031 Trips 实现自动检测和手动创建逻辑
- [ ] T-032 TopDestinations 集成 Leaflet 地图
- [ ] T-033 Mileage 实现年→月→日钻取交互
- [ ] T-034 Heatmap 修复随机数据问题（使用 mock_data.json）
- [ ] T-035 Drives 添加骨架屏 Loading 状态
- [ ] T-036 所有列表页添加分页/无限加载

## 4. iOS 数据模型补全

- [ ] T-040 添加 SentryEvent 数据模型
- [ ] T-041 添加 Trip 数据模型
- [ ] T-042 添加 VisitedRegion 数据模型
- [ ] T-043 验证 iOS 编译通过

## 5. 交互增强

- [ ] T-050 Android Dashboard 实现下拉刷新（SwipeRefresh）
- [ ] T-051 Android Dashboard 实现车辆切换 Modal
- [ ] T-052 Android Dashboard 实现卡片点击跳转
- [ ] T-053 iOS Dashboard 实现下拉刷新
- [ ] T-054 iOS Dashboard 实现车辆切换 Sheet
```

