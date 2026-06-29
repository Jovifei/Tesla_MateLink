---
comet_change: glm-p2-multi-instance
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-25-glm-p2-multi-instance
status: final
---

# glm-p2-multi-instance — 多实例切换

## Context / Goals

- T-101~T-104: 多实例数据模型、切换 UI、数据隔离、Widget/通知过滤

## Decisions

- D-1: Instance 数据模型：id, name, serverUrl, apiToken, carId
- D-2: iOS AppGroup UserDefaults 存实例列表，Android DataStore
- D-3: 切换实例时重新加载所有数据
- D-4: Widget/通知使用当前活跃实例

## Tasks (4)

- T-101: 多实例数据模型
- T-102: 实例切换 UI
- T-103: 数据隔离
- T-104: Widget/通知按实例过滤
