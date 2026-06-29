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
- 自定义车辆颜色/配置器