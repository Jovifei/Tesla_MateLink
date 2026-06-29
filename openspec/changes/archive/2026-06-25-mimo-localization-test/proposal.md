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
