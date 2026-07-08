# Tasks — stitch-white-1to1

> 执行策略：build 阶段用 /child-claude 派发子代理，每页一子代理负责双端（Android Composable + iOS SwiftUI）1:1 还原。每子代理开工前用 `mcp__stitch__get_screen` 读取对应 screen ID 的 HTML 作为源真理。

## 1. 预检（核实 prior change 产物可用）

- [x] 1.1 核实 design-tokens.json 双端解析 — Android Theme.kt OK, iOS AppTheme.swift 有 StitchColors+StitchFont OK
- [x] 1.2 核实 Stitch 组件 — Android components/ OK; iOS 当前是 DashboardView.swift 内 private struct 需提取
- [x] 1.3 iOS Stitch 组件提取到共享 Components/StitchComponents.swift (internal), 更新 L1 四视图引用
- [x] 1.4 iOS JetBrains Mono 字体: 从 Android res/font/ 复制 .ttf 到 iOS Resources/, Info.plist 注册, StitchFont 改用 .custom
- [x] 1.5 核实 Stitch MCP 可用 (已读 19 页 screen 清单)

## 2. L1 校准（4页 — 对照真实 HTML 修正 prior 偏差）

- [x] 2.1 仪表盘 `405f645538ae4a788b30aa4f64550e6f` — 校准 Android DashboardScreen + iOS DashboardView（状态头/电池卡/充电卡/信息网格/7日趋势，中文 Tab「仪表盘」）
- [x] 2.2 行程历史 `11444dd2914644cab88e53dd6973e46e` — 校准 Android DriveListScreen + iOS DriveListView（月分组/卡片行/效率徽章，中文 Tab「行程」）
- [x] 2.3 充电历史 `2958ceb895414130bb618a34682e26f7` — 校准 Android ChargeListScreen + iOS ChargeListView（实时置顶卡/月分组/AC-DC 标识，中文 Tab「充电」）
- [x] 2.4 更多菜单 `607f50c463444dbf8183d6f0e96dfabb` — 校准 Android MoreScreen + iOS MoreView（车辆摘要+分组导航+报告入口，中文 Tab「更多」）

## 3. L2 详情页（3页）

- [ ] 3.1 行程详情 `7262882484106971972` — 还原 Android DriveDetailScreen + iOS DriveDetailView（地图+5曲线 Tab+摘要，接行程列表跳转）
- [ ] 3.2 充电详情 `12c4a93d5f484d1c89a16c3e385e59cb` — 还原 Android ChargeDetailScreen + iOS ChargeDetailView（4曲线+充电阶段+月度习惯，接充电列表跳转）
- [ ] 3.3 当前充电 `5d52c8ca82df434e9bd4a67e74290ffc` — 还原 Android CurrentChargeScreen + iOS CurrentChargeView（1s功率曲线+ETA+阶段；iOS 若缺则新建 Features/CurrentCharge/）

## 4. L2 分析页（7页）

- [ ] 4.1 续航分析 `c4bf3de8c1ee4f439751ab3bc14fb601` — 还原 Android RangeScreen + iOS RangeView（偏差+影响因素+评级+同车型对比）
- [ ] 4.2 能耗分析 `3f828fd2e1bb462bb104b6aae0e19290` — 还原 Android EfficiencyScreen + iOS EfficiencyView（平均能耗+评级+趋势+散点+同车型+建议）
- [ ] 4.3 电池健康 `a903f4ccfaf64988b12eebd9b6b07d5f` — 还原 Android BatteryHealthScreen + iOS BatteryHealthView（健康度+衰减+循环+温度+维护建议）
- [ ] 4.4 待机耗电 `78dd96dc2e1d4882a30b0af4f9b83f17` — 还原 Android VampireScreen + iOS VampireView（损耗+来源+趋势+优化建议）
- [ ] 4.5 热力图 `5ddffde05eba4fec9ba278857d5f5b24` — 还原 Android HeatmapScreen + iOS HeatmapView（24h网格+目的地+路线排行）
- [ ] 4.6 时间线 `e1b336b48d1c48cca53d693131a44839` — 还原 Android TimelineScreen + iOS TimelineView（24h活动轴+事件分段）
- [ ] 4.7 里程钻取 `9d4bc5d2a8024d0c8397b7d3cd037848` — 还原 Android Statistics/MileageScreen + iOS StatisticsView/MileageView（年度+月度+场景+365热力+Top5+年→月→日钻取；超长 HTML 分段读取）

## 5. L2 系统页（4页）

- [ ] 5.1 设置 `4c90a050b87c44b1aaf73a8ba590ad96` — 还原 Android SettingsScreen + iOS SettingsView（服务器/Token/连接测试/语言/主题/模拟/实例状态）
- [ ] 5.2 固件版本 `2c1bd185b2d14b5ba5647bd762c9c240` — 还原 Android UpdatesScreen + iOS UpdatesView（当前版本+历史时间线）
- [x] 5.3 关于 `845c19f9afe94ddc9d1544b3a6936f1c` — 还原 Android AboutScreen + iOS AboutView（品牌+车辆摘要+版本+技术栈+开源许可）
- [x] 5.4 哨兵历史 `7b959ff2df234fe4ba834c7eb96dcd9c` — 还原 Android SentryScreen + iOS SentryView（事件时间线+灵敏度；iOS 若缺则新建 Features/Sentry/）

## 6. L2 成本页（1页）

- [ ] 6.1 成本分析 `cbf4541b745f447d8de3e67eacc2df50` — 还原 Android CostScreen + iOS CostView（月度成本+驾驶习惯+TOU 分时电价）

## 7. 导航与入口接入

- [ ] 7.1 Android NavGraph 核实所有 19 页路由注册 + 跳转链路（仪表盘→行程详情、充电历史→充电详情/当前充电、更多→各分析/系统页、里程钻取年→月→日）
- [ ] 7.2 iOS MoreView/ContentView 核实所有 19 页入口 + NavigationLink 链路
- [ ] 7.3 中文文案对齐 Stitch（Tab 标签 仪表盘/行程/充电/更多；页面标题；状态文案）

## 8. 验证（文件结构 + 视觉 checklist）

- [ ] 8.1 文件存在性：19 页 Android Composable + 19 页 iOS SwiftUI View 均存在且非空
- [ ] 8.2 视觉 checklist：无阴影/elevation、所有卡 1px #E5E5E5 边框、8px 圆角、数字 JetBrains Mono tabular-nums、文字 Inter
- [ ] 8.3 Stitch 截图对照：每页 Android+iOS 渲染与 Stitch screenshot 肉眼比对（关键页抽样）
- [ ] 8.4 数据层未改核实：grep 确认无 ViewModel/Repository/Dao 改动
- [ ] 8.5 导航链路核实：所有跳转可达，无断链
