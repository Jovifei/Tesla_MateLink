## ADDED Requirements

### Requirement: 续航分析页 1:1 Stitch 还原
续航分析页（RangeScreen / RangeView）SHALL 1:1 还原 Stitch screen `c4bf3de8c1ee4f439751ab3bc14fb601`「MateLink 续航分析 (Swiss Minimal)」，含预估 vs 实际偏差、影响因素、综合续航评级、同车型相对位置。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入续航分析
- **THEN** 偏差卡 + 影响因素列表 + 评级卡视觉与 Stitch HTML 一致（白底/1px 边框/JetBrains Mono 数字）

### Requirement: 能耗分析页 1:1 Stitch 还原
能耗分析页（EfficiencyScreen / EfficiencyView）SHALL 1:1 还原 Stitch screen `3f828fd2e1bb462bb104b6aae0e19290`「MateLink 能耗分析 (Swiss Minimal)」，含平均能耗、评级、趋势、分布、同车型对比、优化建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入能耗分析
- **THEN** 能耗评级 + 趋势曲线 + 散点分布 + 同车型对比 + 建议卡视觉与 Stitch 一致

### Requirement: 电池健康页 1:1 Stitch 还原
电池健康页（BatteryHealthScreen / BatteryHealthView）SHALL 1:1 还原 Stitch screen `a903f4ccfaf64988b12eebd9b6b07d5f`「MateLink 电池健康 (中文导航)」，含健康度、容量衰减、循环统计、温度分布、维护建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入电池健康
- **THEN** 健康度主卡 + 衰减曲线 + 循环统计 + 温度分布 + 维护建议视觉与 Stitch 一致

### Requirement: 待机耗电页 1:1 Stitch 还原
待机耗电页（VampireScreen / VampireView）SHALL 1:1 还原 Stitch screen `78dd96dc2e1d4882a30b0af4f9b83f17`「MateLink 待机耗电详情 (Swiss Minimal 优化)」，含总损耗、平均功率、耗电来源（哨兵/第三方唤醒/温度预调节）、趋势、优化建议。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入待机耗电
- **THEN** 损耗总览 + 耗电来源分解 + 趋势曲线 + 优化建议视觉与 Stitch 一致

### Requirement: 热力图页 1:1 Stitch 还原
热力图页（HeatmapScreen / HeatmapView）SHALL 1:1 还原 Stitch screen `5ddffde05eba4fec9ba278857d5f5b24`「MateLink 热力图 (Swiss Minimal)」，含高频时间段、常去目的地、路线排行。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入热力图
- **THEN** 24h 热力网格 + 目的地列表 + 路线排行视觉与 Stitch 一致

### Requirement: 时间线页 1:1 Stitch 还原
时间线页（TimelineScreen / TimelineView）SHALL 1:1 还原 Stitch screen `e1b336b48d1c48cca53d693131a44839`「MateLink 时间线 (Swiss Minimal)」，含 24h 活动轴、事件分段（驾驶/充电/待机/哨兵）。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入时间线
- **THEN** 24h 活动轴 + 事件分段色块 + 事件详情视觉与 Stitch 一致

### Requirement: 里程钻取页 1:1 Stitch 还原
里程钻取页（Statistics/MileageScreen / StatisticsView/MileageView）SHALL 1:1 还原 Stitch screen `9d4bc5d2a8024d0c8397b7d3cd037848`「MateLink 里程钻取 (Swiss Minimal)」，含年度总里程、月度趋势、场景分布、365 热力、Top5、年→月→日多级钻取。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入里程钻取
- **THEN** 年度总览 + 月度趋势 + 365 热力 + Top5 视觉与 Stitch 一致

#### Scenario: 年→月→日钻取
- **WHEN** 用户点击某月再点击某日
- **THEN** 钻取到月度详情再到当日行程列表，视觉与 Stitch 一致

#### Scenario: 超长 HTML 分段读取
- **WHEN** 子代理读取 103894px 超长 HTML
- **THEN** 按 section 分段读取，优先还原年→月→日三级骨架，细节曲线降级处理
