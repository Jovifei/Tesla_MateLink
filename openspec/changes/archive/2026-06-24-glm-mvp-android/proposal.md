# Proposal: MateLink Android (Full)

## Summary
Android 全量实现：工程初始化 → 数据层 → 所有页面 → 中国本地化 → Google Play 上架

## Goals
- 工程初始化 (Gradle + Compose + dependencies, 直抄 matedroid 90%)
- API Client (Retrofit + OkHttp, 直抄 matedroid 80%)
- 12 个页面 (Dashboard/Drives/Charges/Battery/Statistics/Heatmap ...)
- 中国本地化 (AMap SDK + GCJ-02 + 分时电价)
- Widget (直抄 matedroid)
- Google Play 上架

## Strategy
**matedroid 直抄 75%**: 架构、API Client、主题、Dashboard、Drives、Charges、Battery、Widget 全直抄

## Non-Goals
- 3D 车辆 → v1.2
- Apple Watch → iOS only

## Reference
- matedroid: 完整 Clean Architecture 代码 ★★★★★
- 计划: `docs/glm_安卓实施计划.md`
