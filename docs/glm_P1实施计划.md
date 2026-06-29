# Tesla_MateLink P1 实施计划 (v1.1)

> P0 MVP 已 archive | 下阶段：中国本地化 + Widget + 推送 + 多语言 + 效率评分
> 日期：2026-06-24 | 技术栈不变

## P1 功能清单 (F-101 ~ F-108 + 4 缺口)

| ID | 功能 | 优先级 | iOS | Android | 估计 |
|---|---|---|---|---|---|
| **G-01** | iOS Timeline 屏幕 | ✅ 已完成 | ✅ 已有 | ✅ 已有 | — |
| **G-02** | iOS Widget 增强 | ⚠️ 需增强 | ✅ 基础已有 | ✅ 已有 | 2d |
| **G-03** | Android Dashboard 地图 | 🔴 缺口 | ✅ 已有 AmapView | 📋 | 2d |
| **F-101** | 中国本地化 (GCJ-02 + 高德) | P1 | ⚠️ 已有 GCJ02 | 📋 | 2d |
| **F-102** | 分时电价完善 | P1 | ✅ 已有 TariffConfig | 📋 | 1d |
| **F-103** | 桌面 Widget | P1 | 📋 | ✅ 已有 | 3d |
| **F-104** | 推送通知 | P1 | 📋 | 📋 | 5d |
| **F-105** | 行程效率评分 (Golden Foot) | P1 | 📋 | 📋 | 2d |
| **F-106** | 访问过的地区统计 | P1 | ⚠️ 已有 Destinations | ⚠️ 已有 | 1d |
| **F-107** | 统计钻取完善 | P1 | ✅ 已有 | 📋 待实现 | 2d |
| **F-108** | 多语言 (5+ 种) | P1 | ⚠️ 已有 zh-Hans | 📋 | 3d |

## P1 Change 拆分

| # | Change | 范围 | 工时 | 依赖 |
|---|---|---|---|---|
| 1 | `glm-p1-gaps` | G-01~G-03 (iOS Timeline + iOS Widget + 双端 Dashboard 地图) | 7d | P0 |
| 2 | `glm-p1-china` | F-101 + F-102 (高德 SDK + GCJ-02 Android + 分时电价完善) | 3d | #1 |
| 3 | `glm-p1-notify` | F-103 + F-104 (推送通知 + Widget 完善) | 8d | #1 |
| 4 | `glm-p1-polish` | F-105 + F-106 + F-108 (效率评分 + 地区统计 + 多语言) | 6d | #2 |

## 时间线

```
W1-W1.5: glm-p1-gaps (7d)           ← 补 P0 缺口
W1.5-W2: glm-p1-china (3d)          ← 中国本地化
W2-W3:   glm-p1-notify (8d)         ← 推送+Widget
W3-W4:   glm-p1-polish (6d)         ← 效率评分+多语言
W4:      集成测试 + verify + archive
```

## 下一步

Jovi 确认后，按 `/comet-open` → `/comet-design` → `/comet-build` 流程逐个推进。
