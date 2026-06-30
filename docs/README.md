# Tesla MateLink — 项目文档索引

> Tesla 车辆数据伴侣应用 · 跨平台（Android + iOS + Web）· [TeslaMate](https://github.com/adriankumpf/teslamate) 移动端伴侣

## 项目简介

MateLink 让 Tesla 车主以原生体验查看自托管 TeslaMate 记录的车辆数据，替代浏览器访问 Grafana 仪表盘。提供蓝牙连接、实时车辆数据追踪、行程/充电历史、电池健康、成本统计等功能。

## 仓库结构

本仓库包含同一产品的多个 AI 实现变体，便于横向对比：

| 目录 | 说明 | 平台 |
|------|------|------|
| `app/` | 主实现骨架 | Android |
| `app_glm/` | GLM AI 产出的跨平台实现 | Android + iOS (含 Watch/Widget) + Shared |
| `app_mimo/` | MIMO AI 产出的跨平台实现 | Android + iOS + Web + Shared |
| `web_matelink/` | Web 客户端（Vite + React + Zustand） | Web |

每个变体内部统一采用 `android/` `ios/` `shared/` `web_matelink/` 子结构。

## 文档导航

| 文档 | 内容 |
|------|------|
| [GUIDE.md](GUIDE.md) | 开发环境搭建与常用命令 |
| [01-ARC-系统架构.md](01-ARC-系统架构.md) | 系统架构、模块划分、技术栈 |
| [TODO.md](TODO.md) | 总体待办 |
| [TODO-glm.md](TODO-glm.md) | app_glm 变体待办 |
| [TODO-mimo.md](TODO-mimo.md) | app_mimo 变体待办 |
| [PRD/](PRD/) | 产品需求文档 |
| [PLAN/](PLAN/) | 实施计划 |
| [app_glm/README.md](../app_glm/README.md) | GLM 变体阶段验收状态 |
| [app_mimo/README.md](../app_mimo/README.md) | MIMO 变体功能矩阵 |

## 技术栈速览

- **Android**: Kotlin · Jetpack Compose · Hilt · Room · Retrofit · WorkManager · Glance Widget
- **iOS**: Swift · SwiftUI · WidgetKit · WatchConnectivity · Watch App
- **Web**: TypeScript · React · Vite · Zustand
- **本地化**: 高德地图 + GCJ-02 坐标转换 + 多语言（de/fr/ja 等） + TOU 分时电价

## 工具链

本工程已初始化以下辅助工具（详见 [GUIDE.md](GUIDE.md)）：

- **CodeGraph** (`.codegraph/`) — tree-sitter 符号知识图谱
- **code-review-graph** (`.code-review-graph/`) — 代码审查知识图谱
- **comet** (`.comet/`) — 规格驱动工作流
- **openspec** (`openspec/`) — 规格变更管理
