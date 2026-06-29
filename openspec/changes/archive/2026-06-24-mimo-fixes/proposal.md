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
