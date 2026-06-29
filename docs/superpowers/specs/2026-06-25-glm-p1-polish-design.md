---
comet_change: glm-p1-polish
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-24-glm-p1-polish
status: final
---

# glm-p1-polish — P1 打磨 + 多语言

## Context / Goals

- F-105: 高效驾驶评分 Golden Foot
- F-106: 访问地区统计
- F-108: 5+ 语言国际化

## Key Decisions

- D-1: score = min(100, max(0, (est/actual) × 100))
- D-2: AI 翻译 ja/de/fr
- D-3: MapKit region detection

## Tasks (7)

### F-105 (T-101 ~ T-102)
- T-101: iOS EfficiencyView 评分
- T-102: Android EfficiencyScreen 评分

### F-106 (T-201 ~ T-202)
- T-201: iOS DestinationsView 区域分组
- T-202: Android DestinationsScreen 区域分组

### F-108 (T-301 ~ T-303)
- T-301: iOS ja.lproj + de.lproj + fr.lproj
- T-302: Android values-ja + values-de + values-fr
- T-303: Web ja.json + de.json + fr.json
