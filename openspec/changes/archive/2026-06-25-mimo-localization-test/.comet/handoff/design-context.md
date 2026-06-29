# Comet Design Handoff

- Change: mimo-localization-test
- Phase: design
- Mode: compact
- Context hash: 53a66f3c50d11eba5a69a56218456af2a7ad1d0b6ecc6c2ba8a8271d40528a11

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/mimo-localization-test/proposal.md

- Source: openspec/changes/mimo-localization-test/proposal.md
- Lines: 1-42
- SHA256: ed8bd4e02ed649ad41e71c68b6c5bfe70c3b0c0820da00fe1076497c58b206b8

```md
## Why

MateLink MVP 核心功能已完成（Web + Android + iOS），但缺少中国本地化和真机测试。中国市场是主要目标，高德地图和中文 UI 是刚需。真机测试是上架前的必要步骤。

## What Changes

### 中国本地化
- **高德地图集成**：Android + iOS 集成高德地图 SDK，替代 OpenStreetMap
- **GCJ-02 坐标纠偏**：WGS-84 → GCJ-02 转换，确保地图标注准确
- **分时电价配置**：峰/平/谷时段 + 电价设置，充电成本自动计算
- **中文 UI**：所有界面文案翻译为简体中文
- **i18n 框架**：支持中/英文切换

### 真机测试
- **Android 真机测试**：核心流程验证、性能测试
- **iOS 真机测试**：核心流程验证、性能测试
- **Bug 修复**：测试发现的问题修复

## Capabilities

### New Capabilities

- `amap-integration`: 高德地图 SDK 集成（Android + iOS）
- `gcj02-conversion`: WGS-84 → GCJ-02 坐标纠偏
- `time-of-use-tariff`: 分时电价配置与计算
- `chinese-localization`: 简体中文 UI + i18n 框架
- `device-testing`: 真机测试流程与验证

### Modified Capabilities

- `dashboard`: 地图组件切换为高德
- `drive-detail`: 路线地图使用高德
- `charge-detail`: 充电位置使用高德
- `settings`: 添加语言切换和电价配置

## Impact

- Android 需要添加高德地图 SDK 依赖
- iOS 需要添加高德地图 SDK 依赖
- 所有页面需要 i18n 支持
- 坐标纠偏逻辑需要在地图渲染前应用
- 不影响 API 层和数据层
```

## openspec/changes/mimo-localization-test/design.md

- Source: openspec/changes/mimo-localization-test/design.md
- Lines: 1-89
- SHA256: fb4fe298c3aee1a20f2f96a948649c125c1c0bf69ddb25f8bb0762b69a564de8

[TRUNCATED]

```md
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
```

Full source: openspec/changes/mimo-localization-test/design.md

## openspec/changes/mimo-localization-test/tasks.md

- Source: openspec/changes/mimo-localization-test/tasks.md
- Lines: 1-74
- SHA256: 6335eedb8f5955aa1f2e9d810e82bbf54421481c490da9af0bf25a8e69c2d07c

```md
## 1. 高德地图集成（Android）

- [ ] T-001 申请高德地图 API Key
- [ ] T-002 添加高德地图 SDK 依赖到 build.gradle.kts
- [ ] T-003 实现 AmapView 封装组件
- [ ] T-004 Dashboard 地图组件切换为高德
- [ ] T-005 DriveDetail 路线地图切换为高德
- [ ] T-006 ChargeDetail 充电位置地图切换为高德
- [ ] T-007 TopDestinations 地图切换为高德

## 2. 高德地图集成（iOS）

- [ ] T-010 添加高德地图 SDK 依赖
- [ ] T-011 实现 AmapView 封装组件
- [ ] T-012 Dashboard 地图组件切换为高德
- [ ] T-013 DriveDetail 路线地图切换为高德
- [ ] T-014 ChargeDetail 充电位置地图切换为高德
- [ ] T-015 TopDestinations 地图切换为高德

## 3. GCJ-02 坐标纠偏

- [ ] T-020 实现 GCJ02Converter 工具类（Android）
- [ ] T-021 实现 GCJ02Converter 工具类（iOS）
- [ ] T-022 所有地图渲染前应用坐标纠偏
- [ ] T-023 验证纠偏精度（误差 < 1m）

## 4. 分时电价配置

- [ ] T-030 实现电价配置页面（Android）
- [ ] T-031 实现电价配置页面（iOS）
- [ ] T-032 实现电价配置页面（Web）
- [ ] T-033 实现充电成本计算逻辑
- [ ] T-034 默认电价配置（峰 ¥1.0/平 ¥0.7/谷 ¥0.3）
- [ ] T-035 充电详情显示分时电价成本

## 5. 中文 UI（i18n）

- [ ] T-040 Android strings.xml 添加中文翻译
- [ ] T-041 iOS Localizable.strings 添加中文翻译
- [ ] T-042 Web i18n 添加中文翻译
- [ ] T-043 设置页添加语言切换选项
- [ ] T-044 验证所有页面中文显示正确

## 6. 真机测试（Android）

- [ ] T-050 准备 Android 测试设备
- [ ] T-051 安装并启动 App
- [ ] T-052 测试 Dashboard 功能
- [ ] T-053 测试充电历史功能
- [ ] T-054 测试驾驶历史功能
- [ ] T-055 测试电池健康功能
- [ ] T-056 测试高德地图显示
- [ ] T-057 测试中文 UI
- [ ] T-058 测试分时电价
- [ ] T-059 记录并修复发现的 bug

## 7. 真机测试（iOS）

- [ ] T-060 准备 iOS 测试设备（需要 Mac）
- [ ] T-061 安装并启动 App
- [ ] T-062 测试 Dashboard 功能
- [ ] T-063 测试充电历史功能
- [ ] T-064 测试驾驶历史功能
- [ ] T-065 测试电池健康功能
- [ ] T-066 测试高德地图显示
- [ ] T-067 测试中文 UI
- [ ] T-068 测试分时电价
- [ ] T-069 记录并修复发现的 bug

## 8. 最终验证

- [ ] T-070 三端功能一致性验证
- [ ] T-071 性能测试（启动时间、帧率、内存）
- [ ] T-072 编写测试报告
```

