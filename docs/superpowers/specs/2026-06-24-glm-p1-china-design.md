---
comet_change: glm-p1-china
role: technical-design
canonical_spec: openspec
status: final
archived-with: 2026-06-24-glm-p1-china
---

# glm-p1-china — 中国本地化 v1 技术设计

## Context

`glm-review-fix-critical` archived (2026-06-24)，Android Dashboard LocationCard 仍是文本占位。Designer 团队 4 视角共识后选 v1 路线：iOS 完善已有 MapKit + GCJ02 方案；Android 端口 app_mimo 现成 AMap 实现；TOU 用 hardcoded 默认费率显示估算成本。

## Goals

1. Android Dashboard zh locale 渲染真实高德地图 + 车辆 marker
2. iOS TariffConfigView 国际化 + locale 修复
3. CostView/CostScreen 显示 TOU 估算成本
4. HK/Macau 坐标特例处理
5. GCJ-02 跨平台一致性可验证

## Architecture

```
┌─ Tesla API (WGS-84 lat/lng) ─┐
│                              │
└──────────► ViewModel ────────┴──► CostView ─► TariffConfig.priceForHour(hour) ─► 估算成本
                  │
                  ▼
            UI Component (iOS AmapView / Android AmapComposeView)
                  │
                  ▼
         GCJ02Converter.isInChina + isInSpecialRegion
                  │       │
        中国大陆 │       │ HK/Macau/境外
                  ▼       ▼
        wgs84ToGcj02  原坐标
                  │
                  ▼
         Marker on map (correct alignment)
```

**关键原则：** 存储中立（WGS-84），转换在显示层。

## Decisions (摘自 design.md)

D-1 ~ D-7 见 `openspec/changes/glm-p1-china/design.md`。Designer 团队 4 视角投票结果。

## Implementation

### iOS (T-101 ~ T-104)
- `Localizable.strings` 双语，提取 TariffConfigView 15 处硬编码
- `GCJ02Converter.swift` 新增 `isInSpecialRegion()` 返回 true 时短路退出 wgs84ToGcj02
- `TariffConfigView.swift` DatePicker `.environment(\.locale, .current)` 替代硬编码
- `CostView.swift` 调用 `tariffConfig.priceForHour(charge.startHour) * energyAdded` 显示 TOU 估算

### Android (T-201 ~ T-207)
- `libs.versions.toml`: `amap3d = "9.6.1"`, `amaplocation = "6.4.5"`
- `AndroidManifest.xml`:
  ```xml
  <meta-data android:name="com.amap.api.v2.apikey" android:value="${AMAP_API_KEY}"/>
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
  <uses-feature android:glEsVersion="0x00020000"/>
  ```
- `AmapComposeView.kt` 端口（TextureMapView + DisposableEffect 生命周期）：
  ```kotlin
  @Composable
  fun AmapComposeView(lat: Double, lng: Double, zoom: Float = 15f, ...) {
      AndroidView(factory = { TextureMapView(it).apply { onCreate(null) } }, ...)
      DisposableEffect(Unit) {
          mapView.onResume()
          onDispose { mapView.onPause(); mapView.onDestroy() }
      }
  }
  ```
- `MapUtils.kt`:
  ```kotlin
  object MapUtils {
      fun isChineseLocale(): Boolean = Locale.getDefault().language == "zh"
  }
  ```
- `DashboardScreen.LocationCard`:
  ```kotlin
  if (MapUtils.isChineseLocale() && hasAmapKey) {
      AmapComposeView(gcjLat, gcjLng, markers = listOf(carMarker))
  } else {
      Text("Map unavailable")
  }
  ```
- `TariffConfig.kt`:
  ```kotlin
  data class TariffConfig(
      val peakPrice: Double = 1.0, val flatPrice: Double = 0.7, val valleyPrice: Double = 0.3
  ) {
      fun priceForHour(hour: Int): Double = when (hour) {
          in 0..6 -> valleyPrice
          in 9..11, in 17..21 -> peakPrice
          else -> flatPrice
      }
  }
  ```

### 共享 (T-301 ~ T-302)
- 测试向量 JSON: `{"input": [{lat,lng}], "expected_gcj": [{lat,lng}]}`
- iOS unit test + Android unit test 用同一向量
- `api-types.ts` 添加 `TariffConfig` interface

## Risks / Trade-offs

| 风险 | 严重度 | 缓解 |
|---|---|---|
| AMap API key 缺失 | HIGH | 占位卡片 + 不 crash，运行时检测 |
| HK/Macau bounding box 边界误判 | LOW | 简化矩形覆盖 95% 场景，v2 改多边形 |
| AmapComposeView 端口缺陷 | LOW | app_mimo 版本已验证 |
| TOU hardcode 默认值不准 | MEDIUM | 用户教育："默认费率，可在 v2 配置" |

## Migration Plan

1. iOS 改动（T-101~T-104）独立提交
2. Android 改动（T-201~T-207）独立提交，AMap key 文档化
3. 共享验证（T-301~T-302）作为收尾
4. 每 task 独立 commit，失败可 git revert

## Testing Strategy

- iOS：单元测试 GCJ02Converter (北京/上海/HK/海外 4 个标定点 + isInSpecialRegion 边界)
- Android：单元测试 GCJ02Converter (同一向量) + AmapComposeView Composable preview
- 手动冒烟：zh-CN locale + 有/无 AMap key 两个场景

## Open Questions

1. AMap key 来源 — Jovi 后续提供
2. TOU 默认时段 — 以上海为参考 (峰 9-11 + 17-21、谷 0-6、平其余)，是否需要其他城市预设？v2 处理
