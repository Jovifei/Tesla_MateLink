# Comet Design Handoff

- Change: mimo-remaining-work
- Phase: design
- Mode: compact
- Context hash: 475bd6a7577cc5d88fad9b9df13cd2f1736f7be61f6c3736a66a22afef1420d8

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/mimo-remaining-work/proposal.md

- Source: openspec/changes/mimo-remaining-work/proposal.md
- Lines: 1-54
- SHA256: 5239e3aabd116128ad9ab76fbce477acd0ad8d6bbe3359dd7c5e8df97a23872a

```md
## Why

mimo-localization-test 已完成 22/70 任务（31%），剩余 48 个任务需要处理：
- Android 地图组件切换（6 个页面）
- iOS 地图组件切换 + 新页面（7 个页面）
- 真机测试（Android + iOS + Web）
- 异常/边界测试
- 最终验证

## What Changes

### Android 地图切换
- Dashboard 地图组件切换为高德
- DriveDetail 路线地图切换为高德
- ChargeDetail 充电位置地图切换为高德
- TripDetail 路线地图切换为高德
- WhereWasI 位置历史地图切换为高德
- RegionsVisited 区域地图切换为高德（需扩展 Polygon 支持）

### iOS 地图切换 + 新页面
- MAMapView UIViewRepresentable 封装
- Dashboard 地图组件切换为高德
- DriveDetail 新增路线地图
- ChargeDetail 新增充电位置地图
- 新建 TripDetail、WhereWasI、RegionsVisited、TopDestinations 页面

### 测试
- Android 真机测试（3 天）
- iOS 真机测试（3 天）
- Web 端测试（1 天）
- 异常/边界测试（1.5 天）
- 最终验证（3 天）

## Capabilities

### New Capabilities

- `android-map-switching`: Android 高德地图组件切换
- `ios-map-switching`: iOS 高德地图组件切换 + 新页面
- `device-testing`: 真机测试策略
- `final-verification`: 三端一致性验证 + 性能测试

### Modified Capabilities

- `dashboard`: 地图组件切换
- `drive-detail`: 路线地图切换
- `charge-detail`: 充电位置地图切换

## Impact

- Android 6 个页面需要修改
- iOS 需要新建 4 个页面 + 修改 3 个页面
- 需要高德 API Key（T-001）
- 需要真机测试设备
```

## openspec/changes/mimo-remaining-work/design.md

- Source: openspec/changes/mimo-remaining-work/design.md
- Lines: 1-55
- SHA256: 26d2d8bad57381d2e08c022d637ec2e55ae0e9a5a4db767dec80f98216786d18

```md
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
```

## openspec/changes/mimo-remaining-work/tasks.md

- Source: openspec/changes/mimo-remaining-work/tasks.md
- Lines: 1-73
- SHA256: b2a2d99fb428075480bac6a6eb871dc6e89e1e2e282d9876d6575e31e5a801aa

```md
## 1. 前置条件

- [ ] T-001 申请高德地图 API Key（https://lbs.amap.com/）

## 2. Android 地图切换（6h）

- [ ] T-005 Dashboard 地图组件切换为高德（AmapPointView）
- [ ] T-006 DriveDetail 路线地图切换为高德（AmapRouteView）
- [ ] T-007 ChargeDetail 充电位置地图切换为高德（AmapPointView）
- [ ] T-007b TripDetail 路线地图切换为高德（AmapRouteView）
- [ ] T-007c WhereWasI 位置历史地图切换为高德（AmapPointView）
- [ ] T-007d RegionsVisited 区域地图切换为高德（需扩展 Polygon 支持）
- [ ] T-008 TopDestinations 地图切换为高德

## 3. iOS 地图切换 + 新页面（10-12h）

- [ ] T-010c MAMapView UIViewRepresentable 封装（AMapRepresentable.swift）
- [ ] T-010d AmapRouteView 路线专用视图
- [ ] T-010e AmapMultiPointView 多点视图
- [ ] T-013 Dashboard 地图组件切换为高德（内部替换）
- [ ] T-014 DriveDetail 新增路线地图 section
- [ ] T-015 ChargeDetail 新增充电位置地图 section
- [ ] T-015b 新建 TripDetail 页面（路线地图 + 时间线）
- [ ] T-015c 新建 WhereWasI 页面（位置历史地图）
- [ ] T-015d 新建 RegionsVisited 页面（区域地图 + 多点标注）
- [ ] T-016 新建 TopDestinations 页面（目的地地图 + 列表）

## 4. Android 真机测试（3 天）

- [ ] T-050 准备 Android 测试设备（2+ 台不同厂商/版本）
- [ ] T-051 安装并启动 App（adb install → 首次引导）
- [ ] T-052 测试 Dashboard 功能（状态/电量/位置/地图）
- [ ] T-053 测试充电历史功能（列表/详情/分时电价）
- [ ] T-054 测试驾驶历史功能（行程/路线/统计）
- [ ] T-055 测试电池健康功能（SOH/容量/温度）
- [ ] T-056 测试高德地图显示（缩放/定位/路线/纠偏）
- [ ] T-057 测试中文 UI（所有页面/语言切换）
- [ ] T-058 测试分时电价（配置/计算/保存）
- [ ] T-059 记录并修复发现的 bug

## 5. iOS 真机测试（3 天）

- [ ] T-060 准备 iOS 测试设备（需要 Mac + iPhone）
- [ ] T-061 安装并启动 App
- [ ] T-062 测试 Dashboard 功能
- [ ] T-063 测试充电历史功能
- [ ] T-064 测试驾驶历史功能
- [ ] T-065 测试电池健康功能
- [ ] T-066 测试高德地图显示
- [ ] T-067 测试中文 UI
- [ ] T-068 测试分时电价
- [ ] T-069 记录并修复发现的 bug

## 6. Web 端测试（1 天）

- [ ] T-080 测试 react-i18next 语言切换
- [ ] T-081 测试地图组件响应式布局
- [ ] T-082 测试深色模式适配
- [ ] T-083 测试分时电价配置页面

## 7. 异常/边界测试（1.5 天）

- [ ] T-090 测试 GPS 信号丢失时地图行为
- [ ] T-091 测试弱网环境下地图加载
- [ ] T-092 测试高德 API Key 无效/过期时降级行为
- [ ] T-093 测试跨时区地图切换
- [ ] T-094 测试应用后台被杀死后地图状态恢复

## 8. 最终验证（3 天）

- [ ] T-100 三端功能一致性验证（Android/iOS/Web 对比表）
- [ ] T-101 性能测试（启动时间、帧率、内存、大量轨迹点渲染）
- [ ] T-102 编写测试报告（通过率、遗留 bug、风险项）
```

