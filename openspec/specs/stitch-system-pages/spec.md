# stitch-system-pages Specification

## Purpose
TBD - created by archiving change stitch-white-1to1. Update Purpose after archive.
## Requirements
### Requirement: 设置页 1:1 Stitch 还原
设置页（SettingsScreen / SettingsView）SHALL 1:1 还原 Stitch screen `4c90a050b87c44b1aaf73a8ba590ad96`「MateLink 设置 (中文版)」，含服务器地址/API Token/连接测试/保存/语言切换/主题切换/模拟模式/实例连接状态。

#### Scenario: 视觉 1:1
- **WHEN** 用户从更多菜单进入设置
- **THEN** 表单输入（1px 边框 focus 转黑 #171717）+ 开关 + 连接状态 chip 视觉与 Stitch 一致

#### Scenario: 数据层保留
- **WHEN** 用户测试连接或保存
- **THEN** 调用现有 SettingsViewModel/SecureSettingsDataStore，UI 仅改视觉不改逻辑

### Requirement: 固件版本页 1:1 Stitch 还原
固件版本页（UpdatesScreen / UpdatesView）SHALL 1:1 还原 Stitch screen `2c1bd185b2d14b5ba5647bd762c9c240`「MateLink 固件版本 (Swiss Minimal)」，含当前版本 + 更新历史时间线。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入固件版本
- **THEN** 当前版本卡 + 历史时间线视觉与 Stitch 一致

### Requirement: 关于页 1:1 Stitch 还原
关于页（AboutScreen / AboutView）SHALL 1:1 还原 Stitch screen `845c19f9afe94ddc9d1544b3a6936f1c`「MateLink 关于 (Swiss Minimal)」（5330 高最全版），含品牌/车辆基础信息/应用版本/平台技术栈/开源组件许可。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入关于页
- **THEN** 品牌 + 车辆摘要 + 版本 + 技术栈 + 开源许可视觉与 Stitch 一致

### Requirement: 哨兵历史页 1:1 Stitch 还原
哨兵历史页（SentryScreen / SentryView）SHALL 1:1 还原 Stitch screen `7b959ff2df234fe4ba834c7eb96dcd9c`「MateLink 哨兵历史 (Swiss Minimal)」，含事件时间线 + 灵敏度设置。

#### Scenario: 视觉 1:1
- **WHEN** 用户进入哨兵历史
- **THEN** 事件时间线 + 灵敏度卡视觉与 Stitch 一致

#### Scenario: iOS Feature 缺失补建
- **WHEN** iOS 无 Sentry Feature 目录
- **THEN** 子代理新建 `Features/Sentry/SentryView.swift` 并接入 MoreView 入口

