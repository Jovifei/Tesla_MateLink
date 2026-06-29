# Comet Design Handoff

- Change: glm-p2-3d-watch
- Phase: design
- Mode: compact
- Context hash: edc64a2bb1078e57799e71df72fb3cfe1751869e5ee73c05fdb0fcab1b8efb9a

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p2-3d-watch/proposal.md

- Source: openspec/changes/glm-p2-3d-watch/proposal.md
- Lines: 1-27
- SHA256: f1cfaaa50fb9455f88e0998909d4323aec3f09c98f8de26a00cb4459df8438e1

```md
## Why

P1 完成后，用户希望提升视觉体验。3D 车辆展示让用户直观看到车辆外观和状态（车门/车窗/充电口动画）。Apple Watch 让车主在手腕上快速查看电量和续航。

## What Changes

### 3D 车辆展示
- **T-101**: 集成 SceneKit (iOS) / Filament (Android) 3D 渲染引擎
- **T-102**: 加载 Tesla 3D 模型（.usdz/.glb 格式）
- **T-103**: 车辆状态可视化（门/窗/充电口开合动画）
- **T-104**: 手势交互（旋转/缩放/平移）

### Apple Watch
- **T-201**: WatchKit app 项目结构搭建
- **T-202**: 电量/续航/状态显示
- **T-203**: WatchComplications（表盘复杂功能）
- **T-204**: iPhone↔Watch 数据同步（WatchConnectivity）

## Capabilities

- `3d-vehicle-display`: 3D 车辆展示和状态可视化
- `apple-watch`: Apple Watch 应用和表盘组件

## Non-Goals
- 车辆远程控制（wake/lock/charge）→ P3
- AI 自然语言查询 → P3
- Android Wear OS
- 自定义车辆颜色/配置器```

## openspec/changes/glm-p2-3d-watch/design.md

- Source: openspec/changes/glm-p2-3d-watch/design.md
- Lines: 1-20
- SHA256: 432200f645771985b18aef038353a3822d61aadb5d28c8a344ffc3da76ff06cc

```md
## Context / Goals

P2 视觉增强。3D 展示 + Apple Watch。

## Decisions

- D-1: iOS 用 SceneKit + .usdz（原生支持），Android 用 Filament + .glb
- D-2: 3D 模型从 Tesla 官方资源或社区开源模型获取
- D-3: Watch 数据通过 WatchConnectivity.framework 从 iPhone 同步，不直连 API
- D-4: WatchComplications 用 CLKComplicationTemplateGraphicRectangularFullImage

## Tasks (8)

- T-101: iOS SceneKit 集成
- T-102: Android Filament 集成
- T-103: 车辆状态动画
- T-104: 手势交互
- T-201: Watch 项目结构
- T-202: Watch 数据显示
- T-203: WatchComplications
- T-204: iPhone↔Watch 同步```

## openspec/changes/glm-p2-3d-watch/tasks.md

- Source: openspec/changes/glm-p2-3d-watch/tasks.md
- Lines: 1-12
- SHA256: e8a25ebece5123a2699050e613add9d21349e279778e0c6df1b4d8b5201ecf3b

```md
## 1. 3D 车辆展示

- [ ] T-101 iOS SceneKit 集成 + .usdz 模型加载
- [ ] T-102 Android Filament 集成 + .glb 模型加载
- [ ] T-103 车辆状态可视化（门/窗/充电口动画）
- [ ] T-104 手势交互（旋转/缩放/平移）

## 2. Apple Watch

- [ ] T-201 WatchKit app 项目结构搭建
- [ ] T-202 电量/续航/状态显示界面
- [ ] T-203 WatchComplications（表盘复杂功能）
- [ ] T-204 iPhone↔Watch WatchConnectivity 数据同步```

