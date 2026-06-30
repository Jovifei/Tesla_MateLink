# TODO-mimo — app_mimo 工程待完成与优化清单

> 最后更新: 2026-06-30
> 基于 opus 需求符合性审核结果整理
> 审核覆盖范围：app_mimo/ 全部（iOS + Android + Web）

---

## ✅ 已完成修复

| # | 修复 | Commit |
|---|---|---|
| 1 | iOS BLOCKER B1: 删除 SettingsView 重复 MoreView | initial |
| 2 | iOS BLOCKER B2: CarStatus 默认 init 重写匹配 33 字段 | initial |
| 3 | iOS BLOCKER B3: DashboardView 改用 4 独立胎压字段 | initial |
| 4 | iOS BLOCKER B4: BatteryHealth 加可选字段 + View 兜底 | initial |
| 5 | iOS BLOCKER B5: Drive.consumptionKwh 计算属性 | initial |
| 6 | iOS BLOCKER B6: getBatteryHealth 默认 init 重写 | initial |
| 7 | Android network_security_config（移除 usesCleartextTraffic） | 0c78fdf |
| 8 | Android UrlSecurity 运行时拦截公网 HTTP + 9 个 JUnit 测试 | 8b796be |
| 9 | Android ApiClient 接入 UrlSecurity | 283eec7 |
| 10 | iOS Keychain WhenUnlockedThisDeviceOnly（阻止 iCloud 同步） | 7bb5719 |
| 11 | 多语言 ja/de/fr（Android + iOS，57 keys × 6 文件） | fc4ba90 |
| 12 | Android Manifest 注册（BootReceiver/ChargingMonitorService/CarWidget） | b835bdf |
| 13 | Gradle wrapper 补全 | 5aecd48 |

---

## 🔴 高优先级（功能缺失，影响用户体验）

### iOS

| # | 问题 | 模块 | 难度 | 依赖 |
|---|---|---|---|---|
| I-1 | 通知功能完全缺失（无 NotificationManager / 7 种通知类型未实现） | Notifications | 大 | — |
| I-2 | 高德 SDK 未接入（AmapView.swift 仍用 MapKit，首行 TODO） | Map | 大 | Podfile + AMap SDK |
| I-3 | 6 页面无地图（DriveDetail / ChargeDetail / TripDetail / WhereWasI / RegionsVisited / TopDestinations） | Map | 大 | 依赖 I-2 |
| I-4 | 多实例完全缺失（单实例架构，AppState 无 Instance 模型） | Settings | 大 | AppState 重构 |
| I-5 | Apple Watch 完全缺失（无 WatchKit 项目） | Watch | 大 | Xcode 项目配置 |
| ~~I-6~~ | ~~BatteryHealth mock 数据缺失~~ | ~~Battery~~ | ~~小~~ | ✅ a8e2697 |
| ~~I-7~~ | ~~UpdatesView 无数据源~~ | ~~Updates~~ | ~~小~~ | ✅ 32818e9 |
| ~~I-8~~ | ~~Timeline 缺休息段 + 驾驶段颜色不符~~ | ~~Timeline~~ | ~~小~~ | ✅ f5af2bd |
| ~~I-9~~ | ~~Drive/Charge 详情页图表假数据~~ | ~~Drives/Charges~~ | ~~中~~ | ✅ 0c7a347 |
| I-10 | DestinationsView 无地图标注（仅列表） | Destinations | 中 | 依赖 I-2 |
| ~~I-11~~ | ~~年度报告缺总费用汇总 + 充电习惯~~ | ~~Reports~~ | ~~小~~ | ✅ cdae8da |
| I-12 | 导出缺 Excel 格式（仅 CSV/JSON） | Reports | 中 | 需 Excel 库 |

### Android

| # | 问题 | 模块 | 难度 | 依赖 |
|---|---|---|---|---|
| ~~A-1~~ | ~~Dashboard 7 天电量趋势硬编码~~ | ~~Dashboard~~ | ~~小~~ | ✅ 4cfb5d6 |
| ~~A-2~~ | ~~分时电价 UI 缺失~~ | ~~Settings~~ | ~~中~~ | ✅ 16e362a |
| ~~A-3~~ | ~~语言切换 UI 缺失~~ | ~~Settings~~ | ~~小~~ | ✅ 23fd322 |
| ~~A-4~~ | ~~Mock 模式数据层未实现~~ | ~~API~~ | ~~中~~ | ✅ 154c765 |
| A-5 | 年度报告缺"常用路线"分析 | Reports | 中 | 需地理聚合 |
| A-6 | 导出缺 Excel 格式（仅 CSV/JSON） | Reports | 中 | 需 Excel 库 |

### Web

| # | 问题 | 模块 | 难度 | 依赖 |
|---|---|---|---|---|
| ~~W-1~~ | ~~API client 纯 mock 无真实 fetch~~ | ~~API~~ | ~~中~~ | ✅ 2a2e081 |
| W-2 | 无 Dockerfile / nginx.conf 部署配置 | DevOps | 小 | — |
| ~~W-3~~ | ~~Trips.tsx 纯空壳~~ | ~~Pages~~ | ~~小~~ | ✅ 9c5328f |
| ~~W-4~~ | ~~6 页面半实现（Math.random / 硬编码）~~ | ~~Pages~~ | ~~中~~ | ✅ f1194ae |
| W-5 | 全站零真实地图渲染（4 处地图均为 placeholder 文字） | Map | 中 | 需 Leaflet 集成 |
| ~~W-6~~ | ~~状态管理未统一~~ | ~~State~~ | ~~小~~ | ✅ a6aab6d |

---

## 🟡 中优先级（跨平台一致性）

| # | 问题 | 影响 | 详情 |
|---|---|---|---|
| X-1 | CarStatus schema 双端分裂 | 数据模型 | iOS 扁平 33 字段 vs Android 嵌套 9 子对象，字段名不同（since vs state_since） |
| X-2 | Car 模型字段缺失 | 数据模型 | Android 缺 eid / vid / car_settings / teslamate_details / total_updates |
| X-3 | 空值策略不一致 | 运行时 | iOS 偏非空（String/Int），Android 全可空（String?/Double?） |
| X-4 | shared/api-types.ts 未被双端引用 | 架构 | 仅 Web 用，iOS/Android 手写独立类型 |
| X-5 | ISO8601 解析双端实现方式不同 | 代码风格 | iOS 分散在各 View，Android 集中在 DateTimeParse.kt |
| X-6 | mock_data.json 仅 iOS 加载 | 数据 | Android 不加载（嵌套 schema 不兼容） |

---

## 🟢 低优先级（代码质量）

| # | 问题 | 模块 |
|---|---|---|
| Q-1 | TeslamateRepository 无 Room 缓存回退（弱网时无离线数据） | Android |
| Q-2 | Room schema JSON 未生成（schemas/ 目录为空） | Android |
| Q-3 | iOS Keychain 未设 kSecAttrService（命名空间冲突风险） | iOS |
| Q-4 | iOS carState category 预留但未实现触发逻辑 | iOS |
| Q-5 | Android 401/403 响应仅 TODO 注释，未实现 token 刷新/登出 | Android |
| Q-6 | Excel 导出缺失（双端仅 CSV/JSON） | 双端 |

---

## 📋 按目录快速修复清单

### app_mimo/ios/

```
MateLink/
├── Core/
│   ├── API/
│   │   └── [ ] I-9: ApiClient 添加 drive detail 时间序列端点
│   ├── Map/
│   │   └── [ ] I-2: AmapView.swift 重写用 MAMapView（需 Podfile）
│   └── Models/
│       └── [ ] X-1: CarStatus schema 与 Android 统一
├── Features/
│   ├── Battery/
│   │   └── [ ] I-6: 补 mock_data.json 的 batteryHealth 扩展字段
│   ├── Dashboard/
│   │   └── [ ] (已完成: 胎压4字段 + 5s轮询 + 下拉刷新)
│   ├── Drives/
│   │   └── [ ] I-9: DriveDetailView 图表接真实数据
│   ├── Charges/
│   │   └── [ ] I-9: ChargeDetailView 图表接真实数据
│   ├── Destinations/
│   │   └── [ ] I-10: 添加地图标注
│   ├── Notifications/
│   │   └── [ ] I-1: 实现 NotificationManager + 7 种通知类型
│   ├── Reports/
│   │   └── [ ] I-11: 补总费用汇总 + 充电习惯
│   │   └── [ ] I-12: 添加 Excel 导出
│   ├── Settings/
│   │   └── [ ] I-4: 重构为多实例架构
│   ├── Timeline/
│   │   └── [ ] I-8: 添加休息段 + 修正颜色
│   └── Updates/
│       └── [ ] I-7: 接 getUpdates API
└── Watch App/
    └── [ ] I-5: 创建完整 WatchKit 项目
```

### app_mimo/android/

```
app/src/main/java/com/matelink/
├── data/
│   ├── api/
│   │   └── [ ] A-4: 添加 Mock 数据拦截器
│   ├── export/
│   │   └── [ ] A-6: 添加 Excel 导出格式
│   ├── repository/
│   │   └── [ ] Q-1: TeslamateRepository 添加 Room 缓存回退
│   └── model/
│       └── [ ] X-1: CarModels.kt schema 与 iOS 统一
├── ui/screens/
│   ├── dashboard/
│   │   └── [ ] A-1: 7 天趋势从历史数据获取
│   ├── reports/
│   │   └── [ ] A-5: 补常用路线分析
│   └── settings/
│       └── [ ] A-2: 创建 TariffConfigScreen
│       └── [ ] A-3: 添加语言切换入口
├── widget/
│   └── [ ] (已完成: Manifest 注册)
└── receiver/
    └── [ ] (已完成: Manifest 注册)
```

### app_mimo/web_matelink/

```
src/
├── api/
│   └── [ ] W-1: client.ts 改用真实 fetch
│   └── [ ] X-4: 引用 shared/api-types.ts
├── pages/
│   └── [ ] W-3: Trips.tsx 实现完整功能
│   └── [ ] W-4: 6 页面替换 Math.random 为真实数据
│   └── [ ] W-5: 4 处地图用 Leaflet 真实渲染
├── store/
│   └── [ ] W-6: App.tsx / Settings / Onboarding 改用 useStore
├── [ ] W-2: 创建 Dockerfile + nginx.conf
```

---

## 📊 审核符合率参考

| 模块 | 符合率 | 关键缺失 |
|---|---|---|
| iOS Foundation + Dashboard | 70% | 3 个编译阻塞（已修） |
| iOS Drives + Charges + Analytics | 54.5% | 假数据图表、缺地图、BatteryHealth 模型断裂 |
| Android 全量 | 81.3% | 1 处硬编码、3 个配置缺失 |
| P1 中国/通知/打磨 | 50% | iOS 通知缺失、iOS 高德未接入、仅 en/zh |
| P2 3D/Watch/Reports/多实例 | 46.7% | 3D 仅 placeholder、Watch 缺失、iOS 多实例缺失 |
| 跨平台 + 安全 | 50% | 数据模型分裂、Web 纯 mock |

---

## 🔧 执行建议

**第一梯队（1-2 天，立即可做）：**
- A-1, A-3, I-6, I-7, I-8, W-3, W-6 → 小改动，直接修

**第二梯队（3-5 天，需要 API 集成）：**
- A-2, A-4, I-9, I-11, W-1, W-4 → 需接 Repository / API

**第三梯队（1-2 周，架构级）：**
- I-1, I-2, I-3, I-4, X-1 → 需重构 / 新 SDK 集成

**第四梯队（2+ 周，新功能）：**
- I-5, I-12, A-5, A-6, W-2, W-5 → 需新项目 / 新依赖
