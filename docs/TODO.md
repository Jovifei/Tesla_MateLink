# Tesla_MateLink TODO — 待完成与优化清单

> 最后更新: 2026-06-30
> 基于 12 opus agent 需求符合性审核结果整理

---

## ✅ 已完成（本批次）

| # | 修复 | Commit | 目录 |
|---|---|---|---|
| 1 | iOS BLOCKER B1-B6（编译错误） | initial commit | app_mimo |
| 2 | Android network_security_config | 0c78fdf / 9d7d87e | 双端 |
| 3 | Android UrlSecurity 运行时拦截公网 HTTP | 8b796be / 02b8de9 | 双端 |
| 4 | iOS Keychain WhenUnlockedThisDeviceOnly | 7bb5719 / 2c847e9 | 双端 |
| 5 | 多语言 ja/de/fr（57 keys × 6 文件） | fc4ba90 | app_mimo |
| 6 | Manifest 注册（BootReceiver/Service/Widget） | b835bdf | app_mimo |
| 7 | Onboarding 持久化 + ping path 修复 | 79fdd37 | app_glm |
| 8 | Gradle wrapper 补全 | pending | 双端 |
| 9 | 详情页图表 "Simulated data" 标注 | pending | app_glm |
| 10 | RealCarRepository + DelegatingCarRepository | feature/20260626/glm-real-repository | app_glm Android |
| 11 | SettingsScreen 数据源 Switch（Mock/Real 切换） | feature/20260626/glm-real-repository | app_glm Android |
| 12 | 17 轮交叉审核 Android CRITICAL/HIGH 清零 | 0101ebf / 4f46ecd / a2af2ec / c3e1816 | app_mimo Android |

---

## 🔴 高优先级（功能缺失，影响用户体验）

### app_glm

| # | 问题 | 模块 | 难度 |
|---|---|---|---|
| G-1 | Android 8 个分析页硬编码 mock（Battery/Heatmap/Efficiency/Vampire/Range/Destinations/Cost/Updates）。**RealCarRepository 已完成（feature/20260626/glm-real-repository），切换路径已打通——SettingsScreen 开启 "Use Real Backend" 即可。但无真实 TeslaMate 后端，Mock 仍是默认。** | Android | 中 |
| G-2 | iOS Drive/Charge 详情页图表用 sin()/random() 假数据 | iOS | 中（需 API 端点支持时间序列数据） |
| G-3 | Android CI/CD 完全缺失（无 .github/workflows/） | Android | 小 |
| G-4 | Android 上架配置缺失（play publisher/隐私政策/截图） | Android | 中 |
| G-5 | iOS Settings 缺单位/时区设置 UI | iOS | 小 |
| G-6 | iOS 7 天电量趋势图用硬编码数据 | iOS | 小 |

### app_mimo

| # | 问题 | 模块 | 难度 |
|---|---|---|---|
| M-1 | iOS 通知功能完全缺失（无 NotificationManager/7 种通知类型） | iOS | 大 |
| M-2 | iOS 高德 SDK 未接入（AmapView.swift 仍 MapKit TODO） | iOS | 大（需 Podfile + MAMapView） |
| M-3 | iOS 6 页面无地图（DriveDetail/ChargeDetail/TripDetail/WhereWasI/RegionsVisited/TopDestinations） | iOS | 大 |
| M-4 | iOS 多实例完全缺失（单实例架构） | iOS | 大（需重构 AppState） |
| M-5 | Apple Watch 完全缺失 | iOS | 大（需 WatchKit 项目） |
| M-6 | Android Dashboard 7 天趋势硬编码 | Android | 小 |
| M-7 | Android 分时电价 UI 缺失（仅数据模型） | Android | 中 |
| M-8 | Android 语言切换 UI 缺失 | Android | 小 |
| M-9 | Android Mock 模式数据层未实现 | Android | 中 |
| M-10 | Web 端 API 纯 mock 无真实 fetch | Web | 中 |
| M-11 | Web 端无 Dockerfile/nginx 部署配置 | Web | 小 |
| M-12 | Web 端 1 空壳 + 6 半实现页面 | Web | 中 |

---

## 🟡 中优先级（跨平台一致性）

| # | 问题 | 影响 |
|---|---|---|
| X-1 | CarStatus schema 双端分裂（iOS 扁平 33 字段 vs Android 嵌套 9 子对象） | 数据模型不一致 |
| X-2 | 字段命名不一致（id vs carId, color vs exteriorColor） | 序列化/反序列化 |
| X-3 | 空值策略不一致（iOS 偏非空，Android 全可空） | 运行时行为差异 |
| X-4 | shared/api-types.ts 未被双端引用 | 仅 Web 用，非真正共享 |
| X-5 | ISO8601 解析双端实现方式不同（iOS 分散 vs Android 集中） | 行为一致但代码风格不同 |

---

## 🟢 低优先级（代码质量）

| # | 问题 | 模块 |
|---|---|---|
| Q-1 | app_mimo iOS BatteryHealth mock 数据缺失（可选字段为 nil 显示 0%） | iOS |
| Q-2 | app_glm iOS Keychain 未设 kSecAttrService（命名空间冲突风险） | iOS |
| Q-3 | app_mimo Android TeslamateRepository 无 Room 缓存回退（弱网无离线数据） | Android |
| Q-4 | app_mimo Android Room schema JSON 未生成 | Android |
| Q-5 | app_mimo iOS UpdatesView 无数据源（Mock 返回空数组） | iOS |
| Q-6 | app_mimo iOS Timeline 缺休息段 + 驾驶段颜色不符 | iOS |

---

## 🔵 iOS/Watch 审核遗留（17 轮交叉审核，需 Mac/Xcode）

> 以下问题在 Android 侧已清零，iOS 侧因无 Mac 编译环境无法修复。
> 验证方式：Mac + Xcode 编译后运行，对照 issue 逐项确认。

| # | 严重级别 | 问题 | 文件 |
|---|---|---|---|
| I-1 | **CRITICAL** | @State 数据竞争 — Task.detached 中读取 selectedYear/yearCharges/chargeEnergyAdded，需在 detached 前捕获为局部变量 | `app_mimo/ios/.../AnnualReportPDFView.swift:363,381,384` |
| I-2 | **CRITICAL** | WCSession delegate 回调访问 lastStatus 无 @MainActor 保护，后台线程与主线程并发读写 | `app_glm/ios/.../PhoneWCSessionManager.swift:50-62` |
| I-3 | HIGH | Timer 未取消 — DashboardView 5s 轮询在 view offscreen 时继续，无 .onDisappear | `app_glm/ios/.../DashboardView.swift:8,73` |
| I-4 | HIGH | writeWidgetData() 每 5s 重建 ImageRenderer + JPEG 编码，无变更检测门控 | `app_glm/ios/.../DashboardView.swift:100-103` |
| I-5 | HIGH | KeychainHelper.save 丢弃 SecItemAdd 返回值，静默失败 | `app_glm/ios/.../AppState.swift:5-18` |
| I-6 | HIGH | cacheNodes() 在 .usdz 加载失败的 fallback 分支未调用 | `app_glm/ios/.../Vehicle3DView.swift:28-36` |
| I-7 | HIGH | WatchConnectivityManager 缺 @MainActor，@Published 属性从后台线程修改 | `app_glm/ios/.../WatchConnectivityManager.swift:16` |
| I-8 | HIGH | AnnualReportPDFView 多余的 newPage() 导致 PDF 首页空白 | `app_mimo/ios/.../AnnualReportPDFView.swift:361` |
| I-9 | HIGH | CSV/JSON 导出用 efficiency 反算 energyKwh 而非直接使用 consumptionKwh 字段 | `app_mimo/ios/.../ExportView.swift:197,231` |
| I-10 | MEDIUM | ExportView 日期解析用固定 DateFormatter 而非 ISO8601DateFormatter（丢失时区偏移） | `app_mimo/ios/.../ExportView.swift:141` |
| I-11 | MEDIUM | PhoneWCSessionManager.activate() 无双重激活保护 | `app_glm/ios/.../PhoneWCSessionManager.swift:15-19` |
| I-12 | MEDIUM | Watch sendToWatch 仅持久化 batteryLevel/range 2 字段（共 5 字段，state/isClimateOn/isLocked 丢失） | `app_glm/ios/.../WatchConnectivityManager.swift:50-52` |
| I-13 | MEDIUM | ClockKit 已废弃（watchOS 10+），应迁移到 WidgetKit | `app_glm/ios/.../ComplicationController.swift:1` |

---

## 🟣 RealCarRepository 接入清单（app_glm Android）

> RealCarRepository + DelegatingCarRepository + SettingsScreen Switch 已实现。
> 以下 8 个分析页当前硬编码 mock 数据，切换 "Use Real Backend" 后需验证数据流是否正常。

| # | 页面 | 当前数据源 | 接入 Real 后预期 |
|---|---|---|---|
| R-1 | BatteryScreen | 硬编码 | 需确认 BatteryHealth API 端点数据格式 |
| R-2 | HeatmapScreen | 硬编码 | 需有真实行程数据（经纬度序列） |
| R-3 | EfficiencyScreen | 硬编码 | 从 getDrives 获取 efficiency 字段 |
| R-4 | VampireScreen | 硬编码 | 需检测连续静止期间的电池消耗 |
| R-5 | RangeScreen | 硬编码 | 从 getCharges 获取 idealRange 数据 |
| R-6 | DestinationsScreen | 硬编码 | 从 getDrives 提取 endAddress 统计 |
| R-7 | CostScreen | 硬编码 | 从 getCharges 获取 cost 字段 |
| R-8 | UpdatesScreen | 硬编码 | 从 getUpdates 获取 update 历史 |

---

## 📋 按目录分组的快速修复清单

### app_glm/android/
- [ ] G-1: 8 个硬编码分析页接 Repository（HeatmapScreen/EfficiencyScreen/VampireScreen/RangeScreen/DestinationsScreen/CostScreen/UpdatesScreen/BatteryScreen）
- [ ] G-3: 创建 .github/workflows/android.yml（detekt + assembleDebug）
- [ ] G-4: 添加 play publisher 插件配置
- [ ] M-6: DashboardScreen 7 天趋势从历史数据获取

### app_glm/ios/
- [ ] G-2: DriveDetailView/ChargeDetailView 图表接真实 API 时间序列数据
- [ ] G-5: SettingsView 添加单位 Picker + 时区设置
- [ ] G-6: DashboardView 7 天趋势从 API 获取
- [ ] Q-2: KeychainHelper 添加 kSecAttrService

### app_mimo/android/
- [ ] M-7: 创建 TariffConfigScreen（电价配置 UI）
- [ ] M-8: SettingsScreen 添加语言切换入口
- [ ] M-9: ApiClient 添加 Mock 数据拦截器
- [ ] Q-3: TeslamateRepository 添加 Room 缓存回退
- [ ] Q-4: 运行一次 gradle build 生成 Room schema JSON

### app_mimo/ios/
- [ ] M-1: 实现 NotificationManager + 7 种通知类型
- [ ] M-2: Podfile 添加 AMap SDK，重写 AmapView 用 MAMapView
- [ ] M-3: 6 个页面添加地图组件
- [ ] M-4: AppState 重构为多实例架构
- [ ] M-5: 创建 WatchKit 项目
- [ ] Q-5: UpdatesView 接 getUpdates API
- [ ] Q-6: TimelineView 添加休息段 + 修正颜色

### app_mimo/web_matelink/
- [ ] M-10: api/client.ts 改用真实 fetch
- [ ] M-11: 创建 Dockerfile + nginx.conf
- [ ] M-12: 修复 6 个半实现页面（Trips/TopDestinations/EfficiencyCurve/Mileage/Heatmap/Statistics）

---

## 📊 符合率参考（审核结果）

| 模块 | app_glm | app_mimo |
|---|---|---|
| Foundation + Dashboard | 75% | 70% |
| Drives + Charges + Analytics | 79% | 54.5% |
| Android 全量 | 79% | 81.3% |
| P1 中国/通知/打磨 | 100% | 50% |
| P2 3D/Watch/Reports | 47% (app_glm 范围) | 46.7% |
| 跨平台 + 安全 | 30% | 50% |
