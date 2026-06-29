## 0. Shared Base（共享层）

- [x] T-000 创建 `app_mimo/shared/` 目录
- [x] T-001 创建 Mock 数据 `mock_data.json`（**2 辆虚拟车** + 30 天历史 + 全字段）
- [x] T-002 定义 API 类型 `api-types.ts`（Car、CarStatus、Charge、Drive、BatteryHealth、SentryEvent、Trip、VisitedRegion）
- [x] T-003 编写 API 端点清单文档

## 1. Web - 项目初始化

- [x] TW-001 初始化 React + Vite + Tailwind + TypeScript 项目到 `app_mimo/web_matelink/`
- [x] TW-002 配置路由（React Router）+ 状态管理（Zustand）
- [x] TW-003 集成 Mock 数据 + API Client
- [x] TW-004 实现 Layout（侧边栏导航 18 项 + 路由 + 响应式）

## 2. Web - Dashboard

- [x] TW-101 实现 Dashboard 页面布局（车辆图 + 状态卡片 + 信息网格）
- [x] TW-102 实现电量/续航显示（大数字 + 进度条）
- [x] TW-103 实现车辆状态徽章（online/driving/charging/asleep/offline + 颜色）
- [x] TW-104 实现 2D 车辆图（按 exterior_color 匹配）
- [x] TW-105 实现信息卡片（位置+海拔、里程、车内/外温度、胎压、车门锁、充电线）
- [x] TW-106 实现状态行（空调状态、充电上限%、高电量警告）
- [x] TW-107 实现充电中卡片（功率、已充电量、预计剩余、进度环）
- [x] TW-108 实现 7 天电量趋势折线图（hover tooltip + 缩放）
- [x] TW-109 实现 5s 自动轮询刷新 + 下拉刷新
- [x] TW-110 实现车辆切换 Modal

## 3. Web - 驾驶模块

- [x] TW-201 实现驾驶列表页（分页 + 日期筛选 + 列表项 hover）
- [x] TW-202 实现驾驶详情页（Leaflet 全屏轨迹 + 起止 Marker）
- [x] TW-203 实现驾驶详情统计卡片（平均/最高速度、能耗、效率、温度、海拔变化）
- [x] TW-204 实现图表标签切换（速度/功率/海拔曲线 + Recharts）
- [x] TW-205 实现图表缩放/拖拽/hover tooltip
- [x] TW-206 实现天气卡片（沿途天气 + 温度 + 图标）
- [x] TW-207 实现行程时间线可视化（驾驶段绿 + 充电段橙 + 休息段灰）

## 4. Web - 充电模块

- [x] TW-301 实现充电列表页（分页 + AC/DC 筛选 + 月度柱状图）
- [x] TW-302 实现充电详情页（功率面积图 + Brush 缩放）
- [x] TW-303 实现充电详情统计卡片（充入电量、费用、效率、起止电量）
- [x] TW-304 实现充电详情地图（充电位置 Marker）
- [x] TW-305 实现 Current Charge 实时充电页（进度环 + 功率/电压/电流 + 实时曲线）

## 5. Web - 电池 + 更新

- [x] TW-401 实现电池健康页（健康度环形进度 + 容量对比 + 衰减趋势图）
- [x] TW-402 实现软件更新历史列表（版本号 + 安装日期 + 运行天数 + 最长版本徽章）

## 6. Web - 统计分析

- [x] TW-501 实现 Mileage 里程钻取（Year→Month→Day→Drive 四级 + 面包屑）
- [x] TW-502 实现 Statistics 高级统计（记录/极值/AC-DC 饼图/温度统计/年份筛选）
- [x] TW-503 实现 Heatmap 热力图（15d×24h + hover/click）
- [x] TW-504 实现 Top Destinations（Leaflet Cluster + 大小颜色编码 + popup）
- [x] TW-505 实现 Efficiency Curve（散点回归 + 温度颜色编码 + 区域选择）
- [x] TW-506 实现 Countries/Regions Visited（国旗+距离+能耗+排序+展开详情）

## 7. Web - 监控 + 高级

- [x] TW-601 实现 Sentry Events 页（事件列表 + 详情 + 地图定位）
- [x] TW-602 实现 Trips 路线旅行页（自动检测 + 手动创建 + 时间线 + 路线地图）

## 8. Web - 设置 + 配置

- [x] TW-701 实现设置页（连接、高级、偏好、中国、开发、关于 6 个区域）
- [x] TW-702 实现 Test Connection 三步检测（/ping → /healthz → /cars）
- [x] TW-703 实现 Mock Mode 开关（确认对话框 + 顶部 banner）
- [x] TW-704 实现首次配置页（欢迎页、URL/Token 输入、测试连接、跳过 Mock）
- [x] TW-705 实现分时电价配置（峰/平/谷时段 + 电价输入）

## 9. Web - 部署

- [x] TW-801 Docker 部署配置（nginx + Dockerfile）
- [x] TW-802 Jovi 确认所有 18 页交互

## 10. iOS App - Foundation（Phase 1）

- [x] T-101 创建 iOS 项目（Swift + SwiftUI）到 `app_mimo/ios/`
- [x] T-102 配置 Swift Package Manager、依赖（Swift Charts、MapKit）
- [x] T-103 实现 API Client（URLSession + async/await）
- [x] T-104 实现数据模型（Car、CarStatus、Charge、Drive、BatteryHealth）
- [x] T-105 实现 Token 安全存储（Keychain）
- [x] T-106 实现离线缓存（SwiftData，30 天 TTL）
- [x] T-107 实现底部 Tab 导航（Dashboard、Drives、Charges、More）
- [x] T-108 实现浅色/深色主题（Apple-Like + 车色强调色）
- [x] T-109 实现首次配置页（URL + Token + 测试连接）
- [x] T-110 实现设置页（服务器配置、单位、主题、Mock 开关）
- [x] T-111 实现多车切换（Modal 选择器）
- [x] T-112 搭建 TeslaMateApi Docker 测试环境

## 11. iOS App - Dashboard

- [x] T-201 实现 Dashboard 页面布局
- [x] T-202 实现电量/续航/状态徽章/2D 车辆图
- [x] T-203 实现信息卡片（位置+海拔、里程、温度、胎压、车门锁、充电线、空调）
- [x] T-204 实现充电上限 + 高电量警告
- [x] T-205 实现充电中卡片
- [x] T-206 实现 5s 自动轮询 + 下拉刷新

## 12. iOS App - Charges

- [x] T-301 实现充电列表页（分页 + AC/DC 筛选）
- [x] T-302 实现充电详情页（地图 + 曲线图 + 统计卡片）
- [x] T-303 实现 Current Charge 实时充电页

## 13. iOS App - Drives

- [x] T-401 实现驾驶列表页（分页 + 日期筛选）
- [x] T-402 实现驾驶详情页（地图 + 曲线图 + 统计卡片 + 天气 + 时间线）
- [x] T-403 实现轨迹点抽稀算法

## 14. iOS App - Battery + More

- [x] T-501 实现电池健康页
- [x] T-502 实现 Mileage 里程钻取
- [x] T-503 实现 Statistics 高级统计
- [x] T-504 实现 Sentry Events
- [x] T-505 实现 Countries Visited
- [x] T-506 实现 Trips 路线旅行
- [x] T-507 实现 Software Versions
- [x] T-508 实现 More 页面入口

## 15. 中国本地化

- [x] T-601 集成高德地图 SDK（iOS 端）
- [x] T-602 实现 GCJ-02 坐标纠偏
- [x] T-603 实现地图切换逻辑（zh-CN → 高德）
- [x] T-604 实现分时电价配置 + 计算
- [x] T-605 实现中文 UI（i18n）

## 16. Android App（Phase 2 — 当前执行，无 Mac 先做 Android）

- [x] T-701 创建 Android 项目（Kotlin + Jetpack Compose + Material 3）到 `app_mimo/android/`
- [x] T-702 配置 Gradle 依赖（Compose、Retrofit、OkHttp、Room、MPAndroidChart、高德 SDK）
- [x] T-703 实现 API Client（Retrofit + OkHttp），参考 matedroid `data/api/`
- [x] T-704 实现数据模型（Car、CarStatus、Charge、Drive、BatteryHealth），参考 matedroid `data/model/`
- [x] T-705 实现 Token 安全存储（EncryptedSharedPreferences），参考 matedroid
- [x] T-706 实现离线缓存（Room 数据库，30 天 TTL），参考 matedroid `data/local/`
- [x] T-707 实现底部导航（Dashboard、Drives、Charges、More），参考 matedroid `ui/navigation/`
- [x] T-708 实现浅色/深色主题（Material 3 + 车色强调色），参考 matedroid `ui/theme/`
- [x] T-709 实现首次配置页（URL + Token + 测试连接），参考 matedroid `settings/`
- [x] T-710 实现设置页（服务器、单位、主题、Mock），参考 matedroid `settings/`
- [x] T-711 实现 Dashboard（电量、状态徽章、2D 车辆图、信息卡片、5s 轮询），参考 matedroid `dashboard/`
- [x] T-712 实现充电列表 + 详情 + Current Charge，参考 matedroid `charges/`
- [x] T-713 实现驾驶列表 + 详情（地图 + 曲线），参考 matedroid `drives/`
- [x] T-714 实现电池健康，参考 matedroid `battery/`
- [x] T-715 实现 Mileage 里程钻取，参考 matedroid `mileage/`
- [x] T-716 实现 Stats for Nerds，参考 matedroid `stats/`
- [x] T-717 实现 Sentry Events，参考 matedroid `sentry/`
- [x] T-718 实现 Countries Visited，参考 matedroid `stats/CountriesVisitedScreen.kt`
- [x] T-719 实现 Software Versions，参考 matedroid `updates/`
- [x] T-720 实现 Trips，参考 matedroid `trips/`
- [x] T-721 实现多车切换，参考 matedroid
- [x] T-722 实现中国本地化（高德地图 + GCJ-02 + 分时电价 + 中文）
- [x] T-723 实现 Mock 模式 + 离线缓存
- [x] T-724 真机测试

## 17. Release（Phase 3）

- [x] T-801 iOS 真机测试
- [x] T-802 Android 真机测试
- [x] T-803 性能测试
- [x] T-804 准备 App Store 截图 + 描述
- [x] T-805 提交 App Store + Google Play
