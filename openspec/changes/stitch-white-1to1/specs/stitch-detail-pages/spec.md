## ADDED Requirements

### Requirement: 行程详情页 1:1 Stitch 还原
行程详情页（DriveDetailScreen / DriveDetailView）SHALL 1:1 还原 Stitch screen `7262882484106971972`「MateLink 行程详情 (Swiss Minimal)」的视觉，含路线地图、距离/时长/最高速/均速/能耗/效率摘要卡、速度/功率/海拔/车内温/车外温 5 曲线 Tab 切换。数据源为现有 DriveViewModel。

#### Scenario: 视觉 1:1
- **WHEN** 用户从行程历史点击某条行程进入详情
- **THEN** 页面视觉（纯白底/1px #E5E5E5 边框卡/8px 圆角/无阴影/Inter 文字/JetBrains Mono 数字 tabular-nums）与 Stitch HTML 一致

#### Scenario: 5 曲线 Tab 切换
- **WHEN** 用户在行程详情切换速度/功率/海拔/车内温/车外温 Tab
- **THEN** 曲线图区域切换，金色 #A16207 主线，坐标轴 label-caps 风格

#### Scenario: 数据降级标识
- **WHEN** 某曲线只有摘要数据无完整时间序列
- **THEN** 显示降级图表并标注"模拟数据 — 基于摘要"（与 PRD 一致）

### Requirement: 充电详情页 1:1 Stitch 还原
充电详情页（ChargeDetailScreen / ChargeDetailView）SHALL 1:1 还原 Stitch screen `12c4a93d5f484d1c89a16c3e385e59cb`「MateLink 充电详情 (Swiss Minimal)」，含 SoC/功率/电压/温度 4 曲线 + 恒流/恒压/涓流阶段划分 + 月度充电习惯。数据源为现有 ChargeViewModel。

#### Scenario: 4 曲线 + 阶段
- **WHEN** 用户进入某次充电详情
- **THEN** 显示 4 曲线 Tab + 充电阶段分段标识（恒流/恒压/涓流），视觉与 Stitch 一致

### Requirement: 当前充电实时监控页 1:1 Stitch 还原
当前充电页（CurrentChargeScreen / CurrentChargeView）SHALL 1:1 还原 Stitch screen `5d52c8ca82df434e9bd4a67e74290ffc`「MateLink 当前充电 (Current Charge)」，含 1s 功率曲线 + ETA + 当前阶段 + 实时 SoC。数据源为现有充电实时数据流。

#### Scenario: 实时监控
- **WHEN** 车辆正在充电且用户从充电历史置顶卡或仪表盘进入
- **THEN** 显示实时 SoC/功率/ETA/阶段，1s 曲线滚动，视觉与 Stitch 一致

#### Scenario: iOS Feature 缺失补建
- **WHEN** iOS 无 CurrentCharge Feature 目录
- **THEN** 子代理新建 `Features/CurrentCharge/CurrentChargeView.swift` 并接入充电列表置顶入口
