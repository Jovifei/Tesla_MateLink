# Tesla MateLink MIMO

> 🚗 多平台 Tesla 车辆数据伴侣 — Android + iOS + Web

## 项目简介

MateLink 是 [TeslaMate](https://github.com/adriankumpf/teslamate) 自托管车辆数据记录器的移动端伴侣应用。让 Tesla 车主以原生体验在手机上查看车辆数据，替代手机浏览器访问 Grafana 仪表盘。

本目录 (`app_mimo/`) 是 **mimo AI** 产出的跨平台实现，包含 Android、iOS 和 Web 三个客户端。

## 项目结构

```
app_mimo/
├── android/                    # Android 客户端
│   └── app/src/main/java/com/matelink/
│       ├── data/               # 数据层（API + Room + Repository + Export）
│       ├── di/                 # Hilt 依赖注入
│       ├── domain/             # 领域模型
│       ├── locale/             # 语言切换
│       ├── notification/       # 充电/哨兵通知
│       ├── receiver/           # BootReceiver
│       ├── service/            # ChargingMonitorService
│       ├── ui/                 # Jetpack Compose UI（17 个模块）
│       ├── util/               # 工具类（GCJ-02, UrlSecurity）
│       └── widget/             # Glance 桌面小组件
├── ios/                        # iOS 客户端
│   └── MateLink/
│       ├── Core/               # API + Models + Utils + Theme
│       ├── Features/           # 17 个功能模块
│       ├── Resources/          # 5 语言本地化
│       └── Widget/             # WidgetKit 小组件
├── shared/                     # 共享 API 类型定义
└── web_matelink/               # Web 客户端
    └── src/
        ├── api/                # API client（真实 fetch + mock 降级）
        ├── pages/              # 18 个页面
        └── store/              # Zustand 状态管理
```

## 技术栈

| 平台 | 技术 |
|---|---|
| **Android** | Kotlin, Jetpack Compose, Hilt, Room v12 (14实体/11迁移), Retrofit+OkHttp, Moshi, WorkManager, Glance Widget, AMap SDK |
| **iOS** | Swift, SwiftUI, Swift Charts, WidgetKit, MapKit, SceneKit, ClockKit, WatchConnectivity |
| **Web** | React 19, TypeScript, Vite, Tailwind CSS, Zustand, Recharts, Leaflet |
| **共享** | GCJ-02 坐标转换（双端一致）, TariffConfig 分时电价, ISO 8601 解析 |

## 功能清单

### ✅ 已完成

#### 核心功能（P0 MVP）
| 功能 | Android | iOS | Web |
|---|:---:|:---:|:---:|
| 车辆状态实时监控（5s 轮询） | ✅ | ✅ | ✅ |
| 电池电量 + 续航显示 | ✅ | ✅ | ✅ |
| 驾驶历史列表 + 详情 | ✅ | ✅ | ✅ |
| 充电历史列表 + 详情 | ✅ | ✅ | ✅ |
| 位置地图（高德/MapKit） | ✅ | ✅ | ✅ |
| 多车切换 | ✅ | ✅ | ✅ |
| Mock 模式（离线开发） | ✅ | ✅ | ✅ |
| 设置页（URL/Token/主题） | ✅ | ✅ | ✅ |
| Onboarding 首次配置 | ✅ | ✅ | ✅ |
| 桌面小组件 | ✅ Glance | ✅ WidgetKit | — |

#### 分析功能（P0+P1）
| 功能 | Android | iOS | Web |
|---|:---:|:---:|:---:|
| 统计钻取（年→月→日→行程） | ✅ | ✅ | ✅ |
| 热力图（15天×24小时） | ✅ | ✅ | ✅ |
| 效率散点图 + Golden Foot 评分 | ✅ | ✅ | ✅ |
| 目的地 Top20 + 区域统计 | ✅ | ✅ | ✅ |
| 成本分析（AC/DC 拆分） | ✅ | ✅ | ✅ |
| 续航分析（预估 vs 实际） | ✅ | ✅ | ✅ |
| 吸血鬼损耗 | ✅ | ✅ | ✅ |
| 电池健康 | ✅ | ✅ | — |
| 时间线 | ✅ | ✅ | — |
| 固件更新 | ✅ | ✅ | ✅ |
| 里程钻取 | ✅ | — | ✅ |
| Stats for Nerds | ✅ | — | — |
| Sentry 事件 | ✅ | — | ✅ |
| 国家/地区访问统计 | ✅ | ✅ | — |

#### 中国本地化（P1）
| 功能 | Android | iOS | Web |
|---|:---:|:---:|:---:|
| 高德地图 SDK 集成 | ✅ | MapKit fallback | — |
| GCJ-02 坐标纠偏（含港澳豁免） | ✅ | ✅ | — |
| 分时电价（峰/平/谷） | ✅ | ✅ | — |
| 中文界面 | ✅ | ✅ | ✅ |

#### 通知推送（P1）
| 功能 | Android | iOS |
|---|:---:|:---:|
| 充电完成通知 | ✅ | ❌ 未实现 |
| 哨兵模式通知 | ✅ | ❌ 未实现 |
| 胎压低告警 | ✅ | ❌ 未实现 |
| 软件更新通知 | ✅ | ❌ 未实现 |
| 里程成就通知 | ✅ | ❌ 未实现 |
| 电池健康告警 | ✅ | ❌ 未实现 |
| 开机自动恢复 | ✅ BootReceiver | — |

#### P2 功能
| 功能 | Android | iOS | Web |
|---|:---:|:---:|:---:|
| 3D 车辆展示 | Canvas 2D placeholder | SceneKit + .usdz | — |
| 年度报告 PDF | ✅ | ✅ | — |
| 数据导出（CSV/JSON） | ✅ | ✅ | — |
| 多实例切换 | ✅ 完整 CRUD | ❌ 单实例 | — |
| Apple Watch | — | ❌ 未实现 | — |

#### 安全加固
| 项目 | 状态 |
|---|---|
| Android network_security_config | ✅ |
| Android UrlSecurity 运行时拦截公网 HTTP | ✅ |
| Android EncryptedSharedPreferences (token) | ✅ |
| iOS Keychain WhenUnlockedThisDeviceOnly | ✅ |
| iOS Onboarding 持久化（不再每次冷启动重置） | ✅ |
| Bearer token 走 Authorization Header（不入 URL） | ✅ |

#### 多语言
| 语言 | Android | iOS | Web |
|---|:---:|:---:|:---:|
| English | ✅ | ✅ | ✅ |
| 中文 | ✅ | ✅ | ✅ |
| 日本語 | ✅ | ✅ | ✅ |
| Deutsch | ✅ | ✅ | ✅ |
| Français | ✅ | ✅ | ✅ |

#### 工程基建
| 项目 | 状态 |
|---|---|
| Gradle wrapper | ✅ |
| Room v12 数据库（14 实体/11 迁移） | ✅ |
| Hilt 依赖注入（3 模块） | ✅ |
| WorkManager 数据同步 | ✅ |
| FileProvider 文件分享 | ✅ |
| Android Manifest 组件注册 | ✅ |
| 语言切换 UI（6 选项） | ✅ |

---

### ⏳ 待完成

#### 高优先级
| # | 问题 | 平台 | 难度 |
|---|---|---|---|
| 1 | iOS 通知功能完全缺失（需 NotificationManager + 7 种类型） | iOS | 大 |
| 2 | iOS 高德 SDK 未接入（当前用 MapKit fallback） | iOS | 大 |
| 3 | iOS 6 页面无地图（DriveDetail/ChargeDetail/TripDetail/WhereWasI/RegionsVisited/TopDestinations） | iOS | 大 |
| 4 | iOS 多实例架构（需重构 AppState） | iOS | 大 |
| 5 | Apple Watch 应用（需 WatchKit 项目） | iOS | 大 |

#### 中优先级
| # | 问题 | 平台 | 难度 |
|---|---|---|---|
| 6 | 跨平台 CarStatus schema 统一（iOS 扁平 vs Android 嵌套） | 双端 | 大 |
| 7 | Android 常用路线分析 | Android | 中 |
| 8 | Excel 导出格式 | 双端 | 中 |
| 9 | Web 真实地图渲染（当前 placeholder） | Web | 中 |
| 10 | Web Dockerfile + nginx 部署配置 | Web | 小 |

#### 低优先级
| # | 问题 | 平台 |
|---|---|---|
| 11 | Android TeslamateRepository Room 缓存回退（弱网离线） | Android |
| 12 | Room schema JSON 生成 | Android |
| 13 | iOS Timeline 休息段颜色微调 | iOS |

---

## 构建指南

### Android
```bash
cd android
./gradlew assembleDebug
# 需要 Java 17+, Android SDK 35
# 高德地图需在 gradle.properties 配置 AMAP_API_KEY
```

### iOS
```bash
cd ios
# 用 Xcode 打开 MateLink.xcodeproj
# 需要 iOS 16.0+ deployment target
# 高德 SDK 需 Podfile 配置（当前用 MapKit fallback）
```

### Web
```bash
cd web_matelink
npm install
npm run dev      # 开发服务器
npm run build    # 生产构建
```

## 环境要求

| 平台 | 要求 |
|---|---|
| Android | compileSdk 35, minSdk 26, Java 17 |
| iOS | 16.0+, Xcode 15+ |
| Web | Node.js 18+ |
| 后端 | [TeslaMate](https://github.com/adriankumpf/teslamate) + [TeslaMateApi](https://github.com/tobiasehlert/teslamateapi) |

## 安全说明

- API token 存储：iOS Keychain (WhenUnlockedThisDeviceOnly) / Android EncryptedSharedPreferences (AES256)
- 网络传输：运行时 UrlSecurity 拦截公网 HTTP（仅允许 HTTPS 或局域网 IP）
- 平台层：Android network_security_config + iOS ATS
- 不收集用户数据，不连接第三方服务

## 许可证

MIT License

## 作者

JoviF
