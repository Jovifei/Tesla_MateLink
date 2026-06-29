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
- T-204: iPhone↔Watch 同步