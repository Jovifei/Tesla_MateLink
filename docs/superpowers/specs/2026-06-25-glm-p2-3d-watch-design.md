---
comet_change: glm-p2-3d-watch
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-25-glm-p2-3d-watch
status: final
---

# glm-p2-3d-watch — 3D 车辆展示 + Apple Watch

## Context / Goals

- T-101~T-104: 3D 车辆展示（SceneKit iOS / Filament Android）
- T-201~T-204: Apple Watch 应用

## Decisions

- D-1: iOS SceneKit + .usdz（原生支持），Android Filament + .glb
- D-2: 3D 模型从 Tesla 官方或社区开源获取
- D-3: Watch 数据通过 WatchConnectivity.framework 同步，不直连 API
- D-4: WatchComplications 用 CLKComplicationTemplateGraphicRectangularFullImage

## Tasks (8)

- T-101: iOS SceneKit 集成
- T-102: Android Filament 集成
- T-103: 车辆状态动画
- T-104: 手势交互
- T-201: Watch 项目结构
- T-202: Watch 数据显示
- T-203: WatchComplications
- T-204: iPhone↔Watch 同步
