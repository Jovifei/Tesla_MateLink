# 01 · 系统架构

## 总览

Tesla MateLink 是 [TeslaMate](https://github.com/adriankumpf/teslamate) 自托管车辆数据记录器的移动端伴侣应用。本仓库以**多 AI 变体并存**的形式组织，同一产品由不同 AI（glm / mimo）独立实现，便于横向对比架构与质量。

```
tesla_master/
├── app/                 # 主实现骨架（Android）
├── app_glm/             # GLM 变体：android + ios(含 Watch/Widget) + shared
├── app_mimo/            # MIMO 变体：android + ios + web + shared
├── web_matelink/        # 独立 Web 客户端（Vite + React）
├── docs/                # 项目文档（PRD / PLAN / TODO）
└── openspec/            # 规格变更管理
```

## 数据流架构

```
┌──────────────┐     HTTPS/REST      ┌─────────────────┐
│ TeslaMate     │  ◄───────────────► │  MateLink App    │
│ (自托管后端)   │                     │  Android/iOS/Web │
│ Postgres+API  │                     └─────────────────┘
└──────────────┘                              │
                                              ├─ Network-First 策略
                                              ├─ 本地缓存降级（Room / 本地存储）
                                              └─ Mock 数据源（开发/演示）
```

## Android 架构（Clean Architecture）

```
ui/          Jetpack Compose Screens + ViewModel
   │
domain/      领域模型 + 用例
   │
data/        Repository（Network-First + 缓存降级）
   ├─ api/        Retrofit 端点
   ├─ Room/       本地数据库（多表缓存）
   └─ Export/     数据导出
di/          Hilt 依赖注入
service/     ChargingMonitorService（前台服务）
notification/ 充电 / 哨兵通知
widget/      Glance 桌面小组件
util/        GCJ-02 坐标转换 / UrlSecurity
locale/      多语言切换
```

**关键模式**：
- `DelegatingCarRepository` 代理模式 —— 运行时在 Mock/Real 数据源间切换。
- `RealCarRepository` —— Network-First + Room 缓存降级。
- WorkManager 后台同步 + 充电监控前台服务。

## iOS 架构（SwiftUI）

```
App/                  应用入口
Core/
   ├─ API/            网络层
   ├─ Models/         数据模型
   ├─ Storage/        本地存储
   ├─ Map/            地图（高德/GCJ-02）
   ├─ Theme/          主题
   ├─ Utils/          工具
   └─ WatchConnectivity/  双向通信
Features/             功能模块（Dashboard / Charges / Battery / Cost ...）
Widget/               WidgetKit 小组件
Watch App/            独立手表应用 + Complications
Resources/            多语言本地化
```

## Web 架构（web_matelink）

```
src/
├─ api/      API client（真实 fetch + mock 降级）
├─ pages/    页面（18 个）
└─ store/    Zustand 状态管理
```

技术栈：TypeScript · React · Vite · Zustand。

## 跨平台共享层（shared/）

| 文件 | 用途 |
|------|------|
| `api-types.ts` | 统一 API 类型定义 |
| `mock_data.json` | Mock 数据源 |
| `gcj02_test_vectors.json` | GCJ-02 坐标转换测试向量 |

## 关键技术决策

1. **多数据源策略**：Network-First，失败降级本地缓存，开发可切 Mock —— 保证弱网/演示可用。
2. **中国本地化**：高德地图 + GCJ-02 坐标转换（TeslaMate 原始为 WGS-84）。
3. **TOU 分时电价**：成本统计按时段电价计算。
4. **全平台覆盖**：Android（含 Widget）+ iOS（含 Watch + Widget）+ Web。

## 模块统计（CodeGraph 索引）

- 497 文件索引：Kotlin 326 · Swift 72 · YAML 48 · TSX 40 · TypeScript 11
- 主要符号：class 627 · function 515 · struct 162 · interface 91 · enum 88
