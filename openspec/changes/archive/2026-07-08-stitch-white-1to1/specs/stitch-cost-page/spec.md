## ADDED Requirements

### Requirement: 成本分析页 1:1 Stitch 还原
成本分析页（CostScreen / CostView）SHALL 1:1 还原 Stitch screen `cbf4541b745f447d8de3e67eacc2df50`「MateLink 成本分析 (Swiss Minimal)」，含月度充电成本、驾驶习惯总结、分时电价 TOU 成本统计。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单或月度摘要进入成本分析
- **THEN** 月度成本卡 + 驾驶习惯总结 + TOU 时段分解视觉与 Stitch HTML 一致（白底/1px #E5E5E5 边框/8px 圆角/无阴影/JetBrains Mono 数字 tabular-nums）

#### Scenario: TOU 分时电价
- **WHEN** 用户查看成本分析
- **THEN** 成本按 TOU 时段（峰/平/谷）分解显示，色块语义与 Stitch 一致

#### Scenario: 数据层保留
- **WHEN** 页面渲染成本数据
- **THEN** 调用现有 CostViewModel/TariffConfig，UI 仅改视觉不改计算逻辑
