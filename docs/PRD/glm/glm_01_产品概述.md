# glm_01 · 产品概述

## 1. 产品愿景

> **让每一位自托管 TeslaMate 的车主，都能在手机上以最优雅的方式查看自己车辆的全部数据。**

不是替代官方 Tesla App，而是**为数据极客打造的数据分析伴侣**——把 TeslaMate 在本地积累的精细化数据，用移动端最原生的体验呈现出来。

## 2. 问题陈述

### 2.1 现状痛点

| 痛点 | 描述 |
|---|---|
| **官方 Tesla App 只看实时** | 无法看历史行程、充电曲线、电池衰减趋势 |
| **Grafana 仪表盘手机难用** | TeslaMate 内置 20+ 仪表盘，但 Grafana Web 端在手机上体验差 |
| **现有 iOS 竞品闭源** | Mytess / HedgieMate / Teslog / T-Buddy 都闭源，且多数仅 iOS |
| **中文用户本地化缺失** | 国内地图不可用（OpenStreetMap 国内数据稀疏）、无分时电价、无中文 UI |
| **数据隐私顾虑** | 商业 SaaS 方案（如 MyTeslaMate 云服务）要求用户把数据上传到他人服务器 |

### 2.2 目标用户画像

**主要用户：自托管 TeslaMate 的 Tesla 车主**

| 画像 | 占比估计 | 特征 |
|---|---|---|
| **技术极客** | 50% | 已在 NAS / VPS / 树莓派跑 TeslaMate，懂 Docker，看 Grafana，对数据敏感 |
| **数据驱动型车主** | 30% | 想看电池衰减、充电效率、驾驶评分，但不愿意折腾 Grafana |
| **中国 Tesla 车主** | 15% | 上述任一类型 + 国内本地化需求（高德地图、分时电价、中文 UI） |
| **TeslaMate 新用户** | 5% | 刚装好 TeslaMate，想找个好用的手机客户端 |

**非目标用户**（第一版不服务）：
- 没装 TeslaMate 的车主（应引导他们先装 TeslaMate）
- 想要车控功能的用户（应引导他们用官方 Tesla App）

## 3. 价值主张

### 3.1 对用户的核心价值

1. **数据完整**：TeslaMate 有的数据，App 都能看（不是官方 App 的子集）
2. **隐私优先**：App 直连用户自己的 TeslaMate 实例，不经我们服务器
3. **跨平台一致**：iOS / Android 同等体验
4. **中国可用**：高德地图、分时电价、中文 UI（差异化卖点）
5. **3D 沉浸**：车辆 3D 模型展示（视觉差异化）

### 3.2 与竞品的差异化（含主观评分）

> 评分制：⭐⭐⭐⭐⭐ 最佳，⭐ 最差。基于用户视角的主观体验评估。

| 维度 | 官方 Tesla App | MateDroid | Mytess (iOS) | teslamate-moblie | **本项目** |
|---|---|---|---|---|---|
| **数据深度** ⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **移动体验** ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **数据主权** | ❌ Tesla 云 | ✅ 自托管 | ✅ 自托管 | ✅ 自托管 | **✅ 自托管** |
| 平台 | iOS + Android | Android only | iOS only | iOS + Android | **iOS + Android** |
| 数据源 | Tesla API 直连 | TeslaMate API | TeslaMate API（mytesla fork） | TeslaMate API | **TeslaMate API** |
| 开源 | ❌ | ✅ MIT | ❌ 闭源 | ✅（但代码未公开） | ✅（计划 MIT） |
| 中国本地化 | ❌ | ❌ | ❌ | 部分 | **✅ 高德 + 分时电价** |
| 3D 车辆 | ❌ | ❌（2D 图） | ❌ | ❌ | **✅ Three.js** |
| 实时推送 | ✅ | ❌ | ✅ | ❌ | ⏳ v1.1 |
| 桌面 Widget | ✅ | ✅ | ✅ | ❌ | ⏳ v1.1 |
| 车端命令 | ✅ | ✅（依赖 Owner API） | ✅ | ❌ | ❌（v2.0 规划） |
| Apple Watch | ✅ | N/A | ❌ | ❌ | ⏳ v1.2 |
| 离线查看 | ❌ | ✅ | ✅ | ❌ | ✅ |
| 免费开源 | N/A | ✅ | 部分付费 | ✅ | ✅ + 可选 PRO |
| 价格 | 免费 | 免费 | 免费 + $9.99 PRO | 免费 | **免费 + 可选付费** |

### 3.3 核心差异化策略

1. **中国本地化优先**——现有竞品几乎都不做，而中国是 Tesla 最大市场
2. **3D 车辆展示**——借鉴 `gwesseling/Tesla-app` 的 R3F 方案，视觉差异化
3. **AI 增强查询**（v2.0）——参考 `teslamate-mcp` 的思路，让用户用自然语言问"上周哪天最费电"
4. **跨平台开源**——`MateDroid` 仅 Android，`teslamate-moblie` 代码未公开，本项目填补"开源跨平台"空白

## 4. 商业模式

### 4.1 推荐：Freemium（免费 + 一次性 PRO 解锁）

| 功能 | 免费版 | PRO 版（$9.99 一次性） |
|---|---|---|
| Dashboard 实时状态 | ✅ | ✅ |
| 行程列表 + 详情 | ✅ | ✅ |
| 充电列表 + 详情 | ✅ | ✅ |
| 电池健康 | ✅ | ✅ |
| 软件更新历史 | ✅ | ✅ |
| 多车切换 | ✅ | ✅ |
| 浅色/深色主题 | ✅ | ✅ |
| **3D 车辆展示** | ⏳ 限免 | ✅ |
| **中国本地化（高德/分时电价）** | ⏳ 限免 | ✅ |
| **桌面 Widget** | ❌ | ✅ |
| **Apple Watch App** | ❌ | ✅ |
| **AI 自然语言查询** | ❌ | ✅ |
| **导出数据（CSV/PDF）** | ❌ | ✅ |
| **多实例切换** | ❌ | ✅ |
| **无广告** | ✅ | ✅ |

**定价理由**：
- 对齐 Mytess 的 $9.99 一次性（用户已接受这个价位）
- 不做订阅（用户讨厌订阅）
- 核心数据展示免费——这是隐私敏感型用户最看重的

### 4.2 备选：全免费 + 捐赠

- 参照 `matedroid` / `t-buddy` 模式
- 优势：用户增长快、社区口碑好
- 劣势：无收入，难以持续投入

### 4.3 决策

**推荐 Freemium**——既能服务免费用户（隐私优先），又能有适度收入支持开发。最终决策待 Jovi 确认。

## 5. 品牌策略

### 5.1 命名原则

- ❌ 不用 "TeslaMate" 作为主名（TeslaMate 官方已警告有恶意 App 冒用）
- ❌ 不用 "Tesla" 作为主名（商标风险）
- ✅ 用独立品牌名 + "for TeslaMate" 副标题

### 5.2 候选品牌名（待 Jovi 决策）

| 候选 | 寓意 | 可用性 |
|---|---|---|
| **Tessera** | Tesla + Era，"特斯拉数据时代" | 待查 |
| **VoltLens** | Volt + Lens，"电压之眼" | 待查 |
| **ChargeQ** | Charge + IQ，"充电智商" | 待查 |
| **MateLink** | TeslaMate + Link | 待查 |
| **TeslaPulse** | Tesla + Pulse，"特斯拉脉搏" | 待查 |

### 5.3 法律免责

App Store / Google Play 描述必须包含：
- "Not affiliated with Tesla, Inc."
- "Tesla is a registered trademark of Tesla, Inc."
- "Requires self-hosted TeslaMate instance"
- "Your data stays on your server — this app does not collect or transmit your data to third parties"

## 6. 成功指标（KPI）

### 6.1 业务指标

| 指标 | M3 目标 | M6 目标 | M12 目标 |
|---|---|---|---|
| iOS 下载量 | 500 | 5,000 | 30,000 |
| Android 下载量 | 300 | 3,000 | 20,000 |
| 月活用户（MAU） | 200 | 2,000 | 15,000 |
| PRO 转化率 | 2% | 5% | 8% |
| App Store 评分 | 4.0+ | 4.5+ | 4.7+ |

### 6.2 产品质量指标

| 指标 | 目标 |
|---|---|
| 启动到首屏可交互时间 | < 2s（中端机型） |
| Dashboard 数据刷新延迟 | < 1s（本地 TeslaMate） |
| 崩溃率 | < 0.5% |
| ANR（Android）/ 主线程卡顿（iOS） | < 0.1% |

### 6.3 用户满意度指标

| 指标 | 目标 |
|---|---|
| App Store 评分 | ≥ 4.5 |
| Google Play 评分 | ≥ 4.4 |
| 用户反馈回复率 | 100%（7 天内） |
| GitHub Issues 响应时间 | < 48h |

## 7. 利益相关方

| 角色 | 关注点 |
|---|---|
| **产品负责人（Jovi）** | 产品方向、商业决策、品牌 |
| **开发（待定）** | 技术实现、架构、代码质量 |
| **设计（待定/Jovi 兼）** | UI/UX、品牌视觉 |
| **TeslaMate 上游** | API 兼容性、数据模型变更 |
| **用户社区** | 功能反馈、Bug 报告、本地化贡献 |

## 8. 假设与约束

### 8.1 假设

1. 用户已自托管 TeslaMate + TeslaMateApi（通过引导文档辅助部署）
2. TeslaMate API 保持向后兼容（`/api/v1/cars` 等端点稳定）
3. Tesla Fleet API 申请周期可接受（如需车端命令功能）
4. Apple Developer 账号（$99/年）和 Google Play 账号（$25 一次性）可获取

### 8.2 约束

1. **法律约束**：不得冒用 Tesla / TeslaMate 商标
2. **隐私约束**：App 不经我们服务器中转用户数据
3. **技术约束**：iOS 构建需 Mac + Xcode
4. **时间约束**：MVP 3 个月内交付
5. **成本约束**：初期单人/小团队开发，预算有限

## 9. 风险概述

详见 `glm_08_风险评估.md`。Top 3 风险：

1. **TeslaMateApi 命令端点对 2024+ 新车失效**——第一版只做读，规避
2. **iOS 审核被拒**（车辆类 App 严审）——描述明确免责 + 强调"requires self-hosted"
3. **中国地图合规**——使用高德 SDK 需处理坐标纠偏（GCJ-02）

## 10. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_02_功能需求.md` 编写功能清单
- ⏭ 之后 `glm_03` 信息架构
- ⏭ 全部 PRD 完成后进入 UI 设计稿阶段
