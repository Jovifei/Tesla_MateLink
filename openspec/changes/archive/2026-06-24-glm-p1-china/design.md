## Context

`glm-review-fix-critical` archived 后，Android Dashboard 地图仍是文本占位。Designer 团队 (D1-D4) 共识：iOS 用 MapKit + 已有 GCJ02Converter 即可，Android 端口 app_mimo 现成 AMap 实现。

## Goals / Non-Goals

**Goals:**
1. Android Dashboard 在 zh locale 渲染真实高德地图 + 车辆 marker
2. iOS TariffConfigView 字符串国际化 + locale 修复
3. CostView/CostScreen 显示 TOU 估算成本（默认费率 hardcode）
4. GCJ-02 跨平台一致性可验证

**Non-Goals:**
- TOU 配置 UI（v2）
- 历史充电 TOU 重算（v2）
- 全屏地图路由（v2）
- PIPL 合规（v2）
- iOS AMap SDK
- build flavor 拆分

## Decisions

### D-1: iOS 不引入 AMap SDK
**选择：** 继续用 MapKit。iOS 17+ Apple Maps 在中国大陆使用 AutoNavi（高德）瓦片，原生 GCJ-02。已有 `GCJ02Converter.swift` 处理 pin 坐标偏移。

**拒绝 AMap SDK：** Licensing 复杂（需 bundle ID 注册，非 App Store 分发要付费），引入新 Pod 增加构建复杂度，MapKit 已满足需求。

### D-2: Android 端口 app_mimo 现成 AMap 实现
**选择：** `app_mimo/android/.../ui/components/AmapComposeView.kt` 已有完整实现（TextureMapView + DisposableEffect 生命周期）。直接端口适配 `com.teslamatelink` 包，不重写。

**拒绝重写：** app_mimo 版本已解决 Compose + AMap 生命周期 gotcha，重写是 NIH。

### D-3: TOU 配置 UI 砍到 v2
**选择：** v1 hardcode 默认费率 ¥1.0/0.7/0.3（峰/平/谷），覆盖 80% 用户。配置 UI 留给 `glm-p1-china-polish`。

**拒绝在 v1 做配置 UI：** 数据模型已有（iOS `TariffConfig`），配置 UI 是 1d 额外工作但价值不大（用户改默认值的频率低）。

### D-4: GCJ-02 转换只在显示层
**选择：** 存储用 WGS-84（Tesla API 原生），ViewModel/Repository 不做转换，仅在 `AmapComposeView.kt`（中国）和 iOS `AmapView`（中国）内部调用 `GCJ02Converter.wgs84ToGcj02()` 后设 marker。

**拒绝存储 GCJ-02：** 数据应保持中立，转换是渲染细节。

### D-5: HK/Macau 特例
**选择：** `GCJ02Converter.isInChina()` 当前 bounding box 包含 HK/Macau，但这两地区实际用 WGS-84（不在 GCJ-02 强制区）。增加 `isInSpecialRegion()` 判断，HK/Macau 跳过偏移。

**Bounding box：**
- HK: lat 22.15-22.55, lng 113.83-114.42
- Macau: lat 22.10-22.22, lng 113.52-113.60

### D-6: 拒绝 MapDisplay 抽象接口（D4 提议）
**选择：** 不抽象 `MapDisplay` 跨平台接口。iOS 和 Android map API 差异太大（UIViewRepresentable vs AndroidView），抽象徒劳。

### D-7: 拒绝 build flavor (D1 提议)
**选择：** 单一 build flavor。中国版和国际版用 runtime locale check (`Locale.getDefault().language == "zh"`) 切换 AMap vs Google Maps。增加 build flavor 是过早优化。

## Risks / Trade-offs

| 风险 | 缓解 |
|---|---|
| **AMap Android API key 未获取** | T-202 检测 key 为空时显示 "Map unavailable" 占位卡片，不 crash |
| **AMap SDK ~8MB 增加 APK** | 可接受，单一 flavor 策略 |
| **HK/Macau 坐标判断边界** | bounding box 测试，若误差大可改为更精确多边形（v2） |
| **iOS MapKit 在中国精度** | Apple Maps 实际用高德瓦片，GCJ02Converter 修正 pin 即可（已验证） |
| **AmapComposeView 端口风险** | app_mimo 版本经测试，端口仅改 package 名 |

## Migration Plan

1. T-101~T-104: iOS 小修小补（无 SDK 变更）
2. T-201~T-205: Android AMap 集成（含 API key 配置文档）
3. T-206~T-207: TOU hardcode（最小改动）
4. T-301~T-302: 共享验证

回滚：每 task 独立 commit，git revert 可逐步回滚。AMap key 缺失场景已有 fallback。

## Open Questions

1. AMap Android API key — Jovi 何时提供？v1 完成时即使 key 缺失，代码逻辑也应完整（只是运行时显示占位）
2. iOS TariffConfig 历史 charge 是否要重算？当前决定：仅显示，不重算（与 v2 范围）
