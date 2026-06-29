# TeslaMate 移动 App PRD · 总索引

> **项目代号**：tesla_master（待定正式品牌名）
> **文档前缀**：`glm_`（生成模型标识）
> **文档位置**：`E:\project\tesla_master\docs\PRD\glm\`
> **版本**：v1.0 · **日期**：2026-06-22

---

## 一、项目一句话定义

基于自托管 [TeslaMate](https://github.com/teslamate-org/teslamate) 的车辆数据，开发 **Android + iOS 跨平台移动 App**，让车主随时随地查看车辆状态、行程历史、充电记录、电池健康等数据。

## 二、PRD 文档结构

| 文档 | 内容 | 状态 |
|---|---|---|
| [glm_00_README.md](./glm_00_README.md) | 本索引 + 文档导航 | ✅ |
| [glm_01_产品概述.md](./glm_01_产品概述.md) | 产品愿景、目标用户、价值主张、竞品分析 | ✅ |
| [glm_02_功能需求.md](./glm_02_功能需求.md) | MVP 功能清单 + 版本路线图 | ✅ |
| [glm_03_信息架构与页面流程.md](./glm_03_信息架构与页面流程.md) | App 信息架构、核心用户流程 | ✅ |
| [glm_04_技术架构.md](./glm_04_技术架构.md) | 技术选型、系统架构、模块划分 | ✅ |
| [glm_05_数据模型与API.md](./glm_05_数据模型与API.md) | 数据模型、API 对接方案 | ✅ |
| [glm_06_非功能需求.md](./glm_06_非功能需求.md) | 性能、安全、合规、本地化 | ✅ |
| [glm_07_项目计划.md](./glm_07_项目计划.md) | 里程碑、工作量估算、资源 | ✅ |
| [glm_08_风险评估.md](./glm_08_风险评估.md) | 风险矩阵、应对策略 | ✅ |
| [glm_09_设计参考.md](./glm_09_设计参考.md) | UI 设计参考清单（竞品截图库） | ✅ |

## 三、关键决策摘要

| 维度 | 决策 |
|---|---|
| **目标平台** | Android + iOS（跨平台一套代码） |
| **技术方案** | React Native 0.76+ + Expo SDK 52+（详见 `glm_04`） |
| **后端依赖** | 用户自托管的 TeslaMate + TeslaMateApi（不建自有后端） |
| **第一版范围** | 只读数据展示，不做车端命令（详见 `glm_02`） |
| **目标用户** | 已自托管 TeslaMate 的 Tesla 车主（全球，含中国） |
| **商业模式** | 免费 + 可选付费功能（详见 `glm_01`） |
| **差异化** | ① 3D 车辆展示 ② 中国本地化（高德地图 + 分时电价） ③ AI 增强查询 |
| **品牌策略** | 独立品牌名 + "for TeslaMate" 副标题（避免冒用 TeslaMate 官方名义） |

## 四、参考仓库索引

详细仓库解析见 `docs/git_ref/glm/`、`docs/git_ref/mimo/`、`docs/git_ref/openclaw/`。核心参考：

- **数据源**：[teslamate-org/teslamate](https://github.com/teslamate-org/teslamate)（8.5k⭐）
- **后端 API**：[tobiasehlert/teslamateapi](https://github.com/tobiasehlert/teslamateapi)（231⭐）
- **Android 架构参考**：[vide/matedroid](https://github.com/vide/matedroid)（67⭐）
- **iOS SwiftUI 参考**：`docs/git_ref/openclaw/Tesla_Clone_Swiftui/`
- **中文竞品参考**：`docs/git_ref/openclaw/teslamate-moblie/`（已上架 TestFlight + Google Play）
- **中国本地化**：`docs/git_ref/mimo/teslamate-chinese-dashboards/`（45 个汉化仪表盘 + 高德地图 + 分时电价）
- **3D 车辆 UI**：[gwesseling/Tesla-app](https://github.com/gwesseling/Tesla-app)（RN + Three.js）
- **官方命令 SDK**：[teslamotors/vehicle-command](https://github.com/teslamotors/vehicle-command)（第二版使用）

## 五、核心里程碑

| 里程碑 | 时间 | 交付 |
|---|---|---|
| M0 · PRD 与设计冻结 | Week 0 | 本套 PRD + UI 设计稿 |
| M1 · MVP Alpha | Week 6 | 内测：Dashboard + Drives + Charges |
| M2 · MVP Beta | Week 10 | 公测：+ Battery Health + Updates + 多车 |
| M3 · iOS 上架 | Week 12 | App Store 审核 |
| M4 · Android 上架 | Week 12 | Google Play 上架 |
| M5 · v1.1 中国本地化 | Week 16 | 高德地图 + 分时电价 |
| M6 · v2.0 车端命令 | Week 24 | 基于 vehicle-command SDK |

详见 `glm_07_项目计划.md`。

## 六、变更记录

| 版本 | 日期 | 变更 | 作者 |
|---|---|---|---|
| v1.0 | 2026-06-22 | 初始版本，基于 20+ 参考仓库 + 需求分析 | Jovi + Claude |
