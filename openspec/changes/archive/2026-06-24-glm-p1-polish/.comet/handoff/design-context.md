# Comet Design Handoff

- Change: glm-p1-polish
- Phase: design
- Mode: compact
- Context hash: 0957883ca344e978a95ec63bfab1133ef44e28da82be7b86f627ce9b1af08878

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p1-polish/proposal.md

- Source: openspec/changes/glm-p1-polish/proposal.md
- Lines: 1-29
- SHA256: 8854f0483673eeb8523ac69db81c32cb2934402cae22f92eebdff7ddc62af47d

```md
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
- iOS AMap SDK 集成```

## openspec/changes/glm-p1-polish/design.md

- Source: openspec/changes/glm-p1-polish/design.md
- Lines: 1-25
- SHA256: f08db537b3b8dd94222dd0a3e3f3ac29e8a29dfe9e7fb1916f08724b29cbc79c

```md
## Context / Goals / Non-Goals

F-105~F-108，3 个独立功能并行实现。

### D-1: 效率评分公式
```
score = min(100, max(0, (estimatedConsumption / actualConsumption) × 100))
```
- estimatedConsumption: 车型 EPA/NEDC 基准值（hardcode = 150 Wh/km）
- actualConsumption: real consumption from drives data

### D-2: 多语言用 AI 翻译非母语
选择：对 ja/de/fr 用 AI 生成翻译（而非人工）。iOS 6 个 Localizable.strings、Android 3 个 strings.xml、Web 3 个 json。不需要专业翻译服务。

### D-3: 访问地区用 MapKit 区域
iOS 用 MKMapView delegate 检测 drive 结束经纬度所属区域。Android 用 Geocoder 反查。

## Tasks

- [ ] T-101 iOS EfficiencyView 评分公式 + UI
- [ ] T-102 Android EfficiencyScreen 评分公式 + UI  
- [ ] T-201 iOS DestinationsView 区域分组
- [ ] T-202 Android DestinationsScreen 区域分组
- [ ] T-301 iOS: ja.lproj + de.lproj + fr.lproj Localizable.strings
- [ ] T-302 Android: values-ja + values-de + values-fr strings.xml
- [ ] T-303 Web: ja.json + de.json + fr.json```

## openspec/changes/glm-p1-polish/tasks.md

- Source: openspec/changes/glm-p1-polish/tasks.md
- Lines: 1-14
- SHA256: 348a6d55e7f07b6370d3d6331e20e82aba1821eee61496bf9443176c1013c0a7

```md
## 1. 效率评分

- [ ] T-101 iOS `EfficiencyView.swift`添加评分+UI
- [ ] T-102 Android `EfficiencyScreen.kt` 评分+UI

## 2. 访问地区统计

- [ ] T-201 iOS `DestinationsView.swift` 区域分组
- [ ] T-202 Android `DestinationsScreen.kt` 区域分组

## 3. 多语言

- [ ] T-301 iOS: `ja.lproj` + `de.lproj` + `fr.lproj` `Localizable.strings`
- [ ] T-302 Android: `values-ja` + `values-de` + `values-fr` `strings.xml`
- [ ] T-303 Web: `ja.json` + `de.json` + `fr.json````

