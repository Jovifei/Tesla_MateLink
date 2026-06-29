# Proposal: MateLink iOS Finalize (Battery + Analytics + China + Test)

## Summary
iOS 电池健康 + 7 个分析页 + 中国本地化 + 测试上架 (F-010/011/101/102 + Web-only pages)

## Goals
- BatteryHealthView (已有基础,增强)
- StatisticsView (钻取) + HeatmapView + DestinationsView + EfficiencyView + VampireView + RangeView + CostView
- 高德地图集成 + GCJ-02
- 分时电价配置
- 真机测试 + App Store 截图 + 提交审核

## Dependencies
- glm-mvp-foundation + dashboard + drives + charges

## Reference
- Web 对应 7 个页面 (React → SwiftUI 移植)
- matedroid BatteryHealthScreen + MileageScreen
- teslamate-chinese-dashboards (GCJ-02 + 分时电价 SQL)
