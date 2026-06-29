---
comet_change: mimo-remaining-work
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-25-mimo-remaining-work
status: final
---

# mimo-remaining-work 深度设计文档

> 日期：2026-06-24
> 目标：完成剩余 48 个任务（地图切换 + 测试 + 验证）

## 1. Android 地图切换

### 1.1 切换策略

利用 `MapUtils.isChineseLocale()` 条件分支：
- 中文环境 → `AmapComposeView`（已封装，内置 GCJ-02）
- 其他环境 → 保留 osmdroid fallback

### 1.2 页面替换清单

| 页面 | osmdroid 位置 | 替换组件 | 工作量 |
|------|--------------|----------|--------|
| Dashboard | DashboardScreen.kt:2016 | AmapPointView | 0.5h |
| DriveDetail | DriveDetailScreen.kt:520 | AmapRouteView | 1h |
| ChargeDetail | ChargeDetailScreen.kt:600 | AmapPointView | 0.5h |
| TripDetail | TripDetailScreen.kt:1162 | AmapRouteView | 1h |
| WhereWasI | WhereWasIScreen.kt:218 | AmapPointView | 0.5h |
| RegionsVisited | RegionsVisitedScreen.kt:555 | 需扩展 Polygon | 2h |
| TopDestinations | 新功能 | AmapMultiPointView | 1h |

### 1.3 风险点

- RegionsVisited 需扩展 AmapComposeView 支持 Polygon
- TripDetail 有延迟加载优化，替换时需保留
- Dashboard 地图嵌在 LazyColumn 中，需确认滚动冲突
- osmdroid 的 BoundingBox.zoomToBoundingBox 无直接高德等价 API

## 2. iOS 地图切换

### 2.1 组件封装

新增 `Core/Map/` 下三个组件：
- `AMapRepresentable.swift` — MAMapView UIViewRepresentable 封装
- `AmapRouteView.swift` — 路线专用视图
- `AmapMultiPointView.swift` — 多点视图

### 2.2 页面清单

| 页面 | 状态 | 修改内容 | 工作量 |
|------|------|----------|--------|
| Dashboard | 已有 AmapView | 内部替换为 MAMapView | 0.5h |
| DriveDetail | 无地图 | 新增路线地图 section | 1h |
| ChargeDetail | 无地图 | 新增充电位置地图 section | 0.5h |
| TripDetail | 不存在 | 新建页面 | 2h |
| WhereWasI | 不存在 | 新建页面 | 1.5h |
| RegionsVisited | 不存在 | 新建页面 | 1.5h |
| TopDestinations | 不存在 | 新建页面 | 1.5h |

### 2.3 风险点

- 高德 API Key 需注册
- MAMapView 线程安全
- 路线绘制性能（>1000 点需抽稀）
- 非中国用户保留 MapKit fallback

## 3. 测试策略

### 3.1 执行顺序

```
Android 真机 (3d) → iOS 真机 (3d) → Web (1d) → 异常 (1.5d) → 验证 (3d)
```

### 3.2 Android 真机测试

| 任务 | 验证点 |
|------|--------|
| Dashboard | 状态/电量/位置/地图 |
| 充电历史 | 列表/详情/分时电价 |
| 驾驶历史 | 行程/路线/统计 |
| 电池健康 | SOH/容量/温度 |
| 高德地图 | 缩放/定位/路线/纠偏 |
| 中文 UI | 所有页面/语言切换 |
| 分时电价 | 配置/计算/保存 |

### 3.3 iOS 真机测试

步骤同 Android，额外关注 Widget 和 Podfile 依赖。

### 3.4 Web 端测试

- react-i18next 语言切换
- 地图组件响应式布局
- 深色模式适配
- 分时电价配置页面

### 3.5 异常/边界测试

| 场景 | 预期行为 |
|------|----------|
| GPS 信号丢失 | 显示最后已知位置，提示定位失败 |
| 弱网环境 | 瓦片渐进加载，超时提示 |
| API Key 无效 | 降级到 OSM，用户提示 |
| 跨时区 | 时间自动调整，电价时段跟随 |
| 后台杀死 | 恢复上次页面状态 |

## 4. 最终验证

### 4.1 三端一致性

- 地图加载/缩放/旋转行为对比
- GPS 定位精度对比（偏差 <5m）
- UI 交互一致性

### 4.2 性能测试

| 指标 | 目标 |
|------|------|
| 冷启动时间 | < 3s (移动端) / < 2s (Web) |
| 地图帧率 | ≥ 30fps |
| 内存占用 | 空闲 < 150MB，万点轨迹 < 300MB |
| 大量轨迹点 | 1 万点无卡顿，10 万点不崩溃 |

### 4.3 测试报告

- 测试环境/版本/设备矩阵
- 功能一致性对比表
- 性能数据表
- 问题清单及严重程度
- 结论与上线建议

## 5. 总工作量

| 模块 | 工作量 |
|------|--------|
| Android 地图切换 | 6h |
| iOS 地图切换 + 新页面 | 10-12h |
| Android 测试 | 3 天 |
| iOS 测试 | 3 天 |
| Web 测试 | 1 天 |
| 异常测试 | 1.5 天 |
| 最终验证 | 3 天 |
| **总计** | **~10 天** |
