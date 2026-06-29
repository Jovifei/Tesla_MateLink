## Context

MateLink MVP 核心功能已完成，需要补充中国本地化和真机测试。

**当前状态**：
- Web: 18 页完整（使用 OpenStreetMap）
- Android: 157 Kotlin 文件
- iOS: 29 Swift 文件
- 数据模型已对齐 shared/api-types.ts

**约束**：
- 高德地图 SDK 需要申请 Key（个人开发者免费）
- iOS 需要 Xcode + 真机
- Android 需要 Android Studio + 真机

## Goals / Non-Goals

**Goals:**
- 中国用户可使用高德地图查看车辆位置和行驶路线
- 所有 UI 文案支持简体中文
- 分时电价自动计算充电成本
- 坐标纠偏确保地图标注准确
- 真机测试验证核心功能

**Non-Goals:**
- 不修改 API 层
- 不添加新功能
- 不修改数据模型
- 不做 App Store/Google Play 上架（单独 change）

## Decisions

### D1: 高德地图集成

**选择**：Android 使用高德地图 SDK 3D，iOS 使用高德地图 SDK

**理由**：
- OpenStreetMap 国内数据稀疏，基本不可用
- 高德地图个人开发者免费
- 支持 GCJ-02 坐标系，无需额外纠偏

**备选**：百度地图（免费但 API 复杂）、腾讯地图（功能较少）

### D2: GCJ-02 坐标纠偏

**选择**：在地图渲染前应用 GCJ-02 转换

**理由**：
- TeslaMate 输出 WGS-84 坐标
- 高德地图使用 GCJ-02 坐标系
- 不纠偏会导致标注偏移 100-500 米

**实现**：
- 参考 teslamate-chinese-dashboards 的纠偏算法
- 在 Utils 层添加 GCJ02Converter

### D3: 分时电价

**选择**：客户端计算，用户可配置峰/平/谷时段和电价

**理由**：
- 不需要后端支持
- 默认值覆盖大部分中国城市
- 用户可自定义

**默认值**：
- 峰时段（10:00-15:00, 18:00-21:00）：¥1.0/kWh
- 平时段（07:00-10:00, 15:00-18:00, 21:00-23:00）：¥0.7/kWh
- 谷时段（23:00-07:00）：¥0.3/kWh

### D4: i18n 框架

**选择**：
- Android: strings.xml 多语言资源
- iOS: Localizable.strings
- Web: react-i18next

**理由**：
- 各平台原生方案，性能最佳
- 支持动态切换语言

## Risks / Trade-offs

| 风险 | 影响 | 缓解方案 |
|------|------|----------|
| 高德 SDK 集成复杂 | 中 | 参考 teslamate-chinese-dashboards |
| GCJ-02 精度问题 | 低 | 使用标准算法，误差 < 1m |
| 真机测试发现严重 bug | 中 | 预留 bug 修复时间 |
| iOS 需要 Mac | 高 | 用户需准备 Mac 或使用云 Mac |
