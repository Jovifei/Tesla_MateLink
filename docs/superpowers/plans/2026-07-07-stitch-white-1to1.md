---
change: stitch-white-1to1
design-doc: docs/superpowers/specs/2026-07-07-stitch-white-1to1-design.md
base-ref: 5abae758d021c382d48cc9489fb4aa5abe97a6c9
archived-with: 2026-07-08-stitch-white-1to1
---

# Implementation Plan — stitch-white-1to1

> 19 页 Stitch 简约白 1:1 还原到 app_glm (Android + iOS)
> 执行方式：/child-claude 按页派发子代理（build_mode=subagent-driven-development）

## 关联产物

- OpenSpec change: `openspec/changes/stitch-white-1to1/`（proposal/design/specs/tasks）
- 技术设计: `docs/superpowers/specs/2026-07-07-stitch-white-1to1-design.md`
- 任务清单: `openspec/changes/stitch-white-1to1/tasks.md`（8 组 35 任务）
- Stitch 源: 项目 `11493757920836657212`，via `mcp__stitch__get_screen`

## 执行约束

- **数据层零改动**：不改 ViewModel/Repository/Dao/Api
- **源真理**：Stitch HTML（每子代理开工先 `get_screen` 读 HTML）
- **复用**：`shared/design-tokens.json` + Stitch 组件（StitchCard/Chip/DataRow/BottomBar）
- **双端同子代理**：每页 Android+iOS 由同一子代理完成，保证一致
- **验证**：文件结构 + 视觉 checklist + Stitch 截图对照（无 JDK/Xcode）

## 子代理派发批次

### 批次 0：预检（主代理直接做，tasks §1）
- 1.1 核实 design-tokens.json 双端解析
- 1.2 核实 Stitch 组件双端可用
- 1.3 核实字体资源双端
- 1.4 核实 Stitch MCP `get_screen` 可用

### 批次 1：L1 校准（4 子代理并行，tasks §2）
每子代理读 Stitch HTML → 校准 Android Composable + iOS SwiftUI View：
- 2.1 仪表盘 `405f645538ae4a788b30aa4f64550e6f`
- 2.2 行程历史 `11444dd2914644cab88e53dd6973e46e`
- 2.3 充电历史 `2958ceb895414130bb618a34682e26f7`
- 2.4 更多菜单 `607f50c463444dbf8183d6f0e96dfabb`

### 批次 2：L2 详情页（3 子代理并行，tasks §3）
- 3.1 行程详情 `7262882484106971972`（地图+5曲线）
- 3.2 充电详情 `12c4a93d5f484d1c89a16c3e385e59cb`（4曲线+阶段）
- 3.3 当前充电 `5d52c8ca82df434e9bd4a67e74290ffc`（iOS 若缺新建 Features/CurrentCharge/）

### 批次 3：L2 分析页（7 子代理并行，tasks §4）
- 4.1 续航分析 `c4bf3de8c1ee4f439751ab3bc14fb601`
- 4.2 能耗分析 `3f828fd2e1bb462bb104b6aae0e19290`
- 4.3 电池健康 `a903f4ccfaf64988b12eebd9b6b07d5f`
- 4.4 待机耗电 `78dd96dc2e1d4882a30b0af4f9b83f17`
- 4.5 热力图 `5ddffde05eba4fec9ba278857d5f5b24`
- 4.6 时间线 `e1b336b48d1c48cca53d693131a44839`
- 4.7 里程钻取 `9d4bc5d2a8024d0c8397b7d3cd037848`（超长 HTML 分段读取，单独慢）

### 批次 4：L2 系统页（4 子代理并行，tasks §5）
- 5.1 设置 `4c90a050b87c44b1aaf73a8ba590ad96`
- 5.2 固件版本 `2c1bd185b2d14b5ba5647bd762c9c240`
- 5.3 关于 `845c19f9afe94ddc9d1544b3a6936f1c`
- 5.4 哨兵历史 `7b959ff2df234fe4ba834c7eb96dcd9c`（双端若缺新建）

### 批次 5：L2 成本页（1 子代理，tasks §6）
- 6.1 成本分析 `cbf4541b745f447d8de3e67eacc2df50`

### 批次 6：导航接入（主代理汇总，tasks §7）
- 7.1 Android NavGraph 路由+跳转链路
- 7.2 iOS MoreView/ContentView 入口+NavigationLink
- 7.3 中文文案对齐 Stitch

### 批次 7：验证（主代理，tasks §8）
- 8.1 文件存在性（19×2 屏幕文件非空）
- 8.2 视觉 checklist（无阴影/1px边框/8px圆角/JetBrains Mono/Inter）
- 8.3 Stitch 截图对照（关键页抽样）
- 8.4 数据层未改 grep 核实
- 8.5 导航链路核实

## 子代理指令模板

每个子代理收到的指令包含：
1. change 名 + 任务编号（如 2.1）
2. Stitch screen ID（如 `405f645538ae4a788b30aa4f64550e6f`）
3. 目标文件（Android Composable + iOS SwiftUI View 路径）
4. 执行步骤：`mcp__stitch__get_screen` 读 HTML → 解析结构/色值/字号/间距 → 改 Android → 改 iOS → 接导航 → 自检
5. 约束：复用 Stitch 组件、数字 JetBrains Mono、无阴影、1px边框、8px圆角、中文文案、数据层不动
6. 完成后：勾选 tasks.md 对应任务 + commit

## 提交策略

每子代理完成一页双端后提交一次：
```
git commit -m "feat(stitch-white-1to1): <page> 1:1 还原 (Android+iOS)

- Stitch screen <id>
- Android: <Composable>
- iOS: <SwiftUI View>
- 数据层未改"
```

## 风险与缓解（详见 Design Doc §6）

- 里程钻取 103894px → 分段读取 + 骨架优先
- iOS Feature 缺失 → 就地新建
- 无编译验证 → 文件结构 + checklist + 截图对照
- 子代理上下文 → 每子代理只读 1 页 HTML
