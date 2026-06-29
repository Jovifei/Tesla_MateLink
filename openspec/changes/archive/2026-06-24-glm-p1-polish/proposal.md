## Why

`glm-p1-china` 归档后，P1 剩余高效驾驶评分 (F-105)、访问地区统计 (F-106)、多语言 (F-108)。这些是用户体验提升，独立于通知推送。

## What Changes

### F-105: 效率评分 (Golden Foot)
- iOS `EfficiencyView` 添加评分映射：消耗/预期 → 0-100 分 + 图标
- Android `EfficiencyScreen` 添加同样评分逻辑
- 评分公式：efficiencyScore = estimatedEfficiency / actualConsumption × 100，clamp [0,100]

### F-106: 访问过的地区统计
- iOS `DestinationsView` 当前按国家分组，需改为标准地图区域（Europe/NA/Asia 等）
- Android `DestinationsScreen` 同步

### F-108: 多语言支持（5+ 种）
- 翻译现有 iOS `Localizable.strings` 到：en（已有）、zh-Hans（已有）、ja（日本）、de（德国）、fr（法国）
- Android `values/strings.xml` 到：`values-ja`、`values-de`、`values-fr`
- Web `zh.json` → `ja.json`、`de.json`、`fr.json`

## Capabilities

- `efficiency-scoring`: 效率评分 Golden Foot
- `visited-regions`: 访问地区统计  
- `multi-language`: 5+ 语言国际化

## Non-Goals
- 推送通知 (→ glm-p1-notify)
- Widget 增强 (→ glm-p1-notify)
- iOS AMap SDK 集成