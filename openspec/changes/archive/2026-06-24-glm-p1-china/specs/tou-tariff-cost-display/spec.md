## ADDED Requirements

### Requirement: CostView 显示 TOU 估算成本
iOS CostView 和 Android CostScreen SHALL 在显示充电成本时，使用默认 TOU 费率（峰 ¥1.0/平 ¥0.7/谷 ¥0.3）计算并展示分时段估算成本。

#### Scenario: 峰时段充电
- **WHEN** 充电开始时间小时 ∈ [9, 12) ∪ [17, 22)
- **THEN** 估算成本 = energyAdded × 1.0

#### Scenario: 谷时段充电
- **WHEN** 充电开始时间小时 ∈ [0, 7)
- **THEN** 估算成本 = energyAdded × 0.3

#### Scenario: 平时段充电
- **WHEN** 充电开始时间小时不在峰/谷时段
- **THEN** 估算成本 = energyAdded × 0.7

#### Scenario: 默认费率显示
- **WHEN** 用户未配置自定义费率
- **THEN** 使用 hardcoded 默认费率，UI 显示 "默认费率" 标注

#### Scenario: TOU 与 API 成本对比
- **WHEN** 充电记录同时有 API 返回的 cost 和 TOU 估算成本
- **THEN** UI 显示两者，标注哪个是 TOU 估算
