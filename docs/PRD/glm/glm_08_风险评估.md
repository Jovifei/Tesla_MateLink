# glm_08 · 风险评估

> **关于风险清单完整性的说明**（回应 mimo 反驳）：
> mimo 建议精简为 5 个核心风险，本 PRD **不同意**。理由：
> - PRD 的风险清单是给项目团队看的，不是给市场材料看的
> - 5 个风险覆盖不够：mimo 漏了 Tesla 商标侵权（历史上有律师函）、AGPL 传染性、GDPR/PIPL 合规、TeslaMate 名冒用——都是可能直接让项目下架的红线
> - 最佳实践是"Top 5 突出 + 完整清单附录"，不是二选一
>
> 本文档结构：§1 Top 5 关键风险（突出） → §2 风险矩阵 → §3 完整清单（30+ 项，附录） → §4 监控 → §5 应急

## 1. Top 5 关键风险（最重要）

### 🥇 R-L01 · Tesla 商标侵权

**风险描述**：Tesla, Inc. 对商标保护严格，若 App 命名/图标让人误以为是官方，可能收到下架通知。

**应对**：
- ✅ 独立品牌名（待定：Tessera / VoltLens / ChargeQ）
- ✅ 图标不用 Tesla T 字标
- ✅ App Store 描述首行："Not affiliated with Tesla, Inc."
- ✅ App 内 About 页明确免责

### 🥈 R-T01 · TeslaMateApi 命令端点对 2024+ 新车失效

**风险描述**：TeslaMateApi 的 `/command` 端点依赖 Tesla Owner API，2024+ 新车已强制 Vehicle Command Protocol，旧端点会失败。

**应对**：
- ✅ 第一版只做读，不碰命令
- ⏳ v2.0 用官方 `teslamotors/vehicle-command` SDK
- ⏳ 提前申请 Tesla Developer + Fleet API

### 🥉 R-P05 · 功能蔓延

**风险描述**：开源项目常见——用户提各种功能需求，开发无序扩张，导致核心体验下降。

**应对**：
- ✅ 严格按 PRD 路线图
- ✅ GitHub Issues 评估后再决定
- ✅ "Not now" 清单：明确哪些功能不做（见 `glm_02` §5）

### 4️⃣ R-L04 · 数据隐私违规

**风险描述**：App 若不当收集用户/车辆数据，可能违反 GDPR / CCPA / PIPL，面临罚款。

**应对**：
- ✅ App 不收集用户数据（核心原则）
- ✅ MVP 用 Sentry + 平台内置分析（不引入 PostHog）
- ✅ Sentry 崩溃报告可选
- ✅ 隐私政策明确"不收集"

### 5️⃣ R-P01 · 差异化不足

**风险描述**：已有 MateDroid（Android）、Mytess/HedgieMate/T-Buddy（iOS）、teslamate-moblie（中文跨平台）等竞品，若 App 无明显差异化，难以获客。

**应对**：
- ✅ 中国本地化（高德 + 分时电价）——多数竞品不做
- ✅ 跨平台开源——填补空白
- ⏳ v1.2 3D 车辆展示——视觉差异化
- ⏳ v2.0 AI 自然语言查询——借鉴 teslamate-mcp 思路

---

## 2. 风险矩阵

| 严重度 | 含义 |
|---|---|
| 🔴 高 | 可能导致项目延期 / 失败 / 法律风险 |
| 🟡 中 | 可能导致功能缺失 / 体验下降 |
| 🟢 低 | 影响有限，有明确缓解方案 |

## 3. 完整风险清单（附录）

### 2.1 技术风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-T01 | **TeslaMateApi 命令端点对 2024+ 新车失效** | 🟡 | 高 | 第一版只做读；v2.0 用 vehicle-command SDK |
| R-T02 | **3D 车辆渲染在中低端机卡顿** | 🟡 | 中 | 分级降级：3D → 2D 图片；性能检测 + 自动降级 |
| R-T03 | **React Native 0.76 新架构兼容性问题** | 🟢 | 中 | 锁定 0.76.x；关键依赖验证后再升级 |
| R-T04 | **高德地图 SDK 在 RN 集成困难** | 🟡 | 中 | 用社区 `react-native-amap3d`；备选：WebView 嵌入 |
| R-T05 | **TeslaMate 数据模型变更**（上游 breaking change） | 🟡 | 低 | 锁定 TeslaMateApi 版本；字段可选化；版本检测 |
| R-T06 | **离线缓存数据不一致** | 🟢 | 中 | React Query invalidate + MMKV TTL + 用户手动刷新 |
| R-T07 | **MQTT 直连方案复杂**（v2.0） | 🟢 | — | v1.x 用轮询；MQTT 作为优化项 |
| R-T08 | **App 启动时间超标** | 🟡 | 中 | Bundle 拆分 + 懒加载 + Hermes + Reanimated 3 |

### 2.2 产品风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-P01 | **与现有竞品差异化不足** | 🔴 | 高 | 强化中国本地化 + 3D 车辆 + AI 查询（v2.0） |
| R-P02 | **用户认为需要 Tesla 账号** | 🟡 | 中 | Onboarding 明确说明"仅需 TeslaMate URL + Token" |
| R-P03 | **用户不知如何部署 TeslaMate** | 🟡 | 高 | 文档：引导用户到 TeslaMate 官方 + 写部署教程 |
| R-P04 | **PRO 功能定价过高** | 🟢 | 低 | 对标 Mytess $9.99；可限时优惠 |
| R-P05 | **功能蔓延**（Scope Creep） | 🔴 | 高 | 严格按 PRD 路线图；GitHub Issues 评估再决定 |
| R-P06 | **App Store 审核被拒**（车辆类严审） | 🔴 | 中 | 明确免责 + 强调"requires self-hosted" + "not affiliated" + 预留 1 周缓冲 |

### 2.3 合规与法律风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-L01 | **Tesla 商标侵权** | 🔴 | 低 | 不用 Tesla 作主名/图标；描述含免责 |
| R-L02 | **TeslaMate 名称冒用** | 🟡 | 低 | 不用 TeslaMate 作主名；用 "for TeslaMate" 副标题 |
| R-L03 | **AGPL 传染**（修改 TeslaMate 源码） | 🔴 | 低 | 不修改 TeslaMate 源码；仅消费 API |
| R-L04 | **数据隐私违规**（GDPR/CCPA/PIPL） | 🔴 | 低 | App 不收集用户数据；隐私政策明确 |
| R-L05 | **高德地图合规**（中国） | 🟡 | 低 | 用官方 SDK + 注册 bundle id |
| R-L06 | **开源许可证违规** | 🟡 | 中 | 自动生成 NOTICE；复核所有依赖 LICENSE |
| R-L07 | **App Store 条款违规** | 🔴 | 低 | 仔细阅读 App Store Review Guidelines；避免 4.0+ 评级风险 |

### 2.4 运营风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-O01 | **TeslaMate 上游停止维护** | 🔴 | 低 | 自建 fork 能力；社区已活跃 8 年，风险低 |
| R-O02 | **Tesla Fleet API 政策收紧** | 🔴 | 中 | 持续关注 Tesla Developer 公告；准备 Plan B |
| R-O03 | **Apple Developer 账号被封** | 🔴 | 低 | 遵守条款；备份账号（极端情况） |
| R-O04 | **用户反馈处理不及时** | 🟡 | 中 | GitHub Issues + 48h 响应承诺 |
| R-O05 | **负面评价影响下载** | 🟡 | 中 | 主动联系差评用户；快速修复 Bug |
| R-O06 | **服务器成本超支**（PostHog 自托管） | 🟢 | 低 | 免费版够用；监控用量 |

### 2.5 资源风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-R01 | **开发人员流失** | 🔴 | 中 | 文档完备；架构清晰；代码 review |
| R-R02 | **Mac 设备缺失**（iOS 构建） | 🟡 | 中 | 采购 / 租用 Mac mini / EAS Build 云构建 |
| R-R03 | **3D 模型外包延期** | 🟡 | 中 | 提前启动；备选 2D 降级 |
| R-R04 | **Fleet API 申请周期长** | 🔴 | 高 | v2.0 不在关键路径；提前申请 |
| R-R05 | **预算超支** | 🟡 | 低 | 优先免费方案；付费功能延后 |

### 2.6 市场风险

| ID | 风险 | 严重度 | 概率 | 应对策略 |
|---|---|---|---|---|
| R-M01 | **TeslaMate 用户基数小** | 🟡 | 中 | App 也兼容 MyTeslaMate（云托管 TeslaMate）用户 |
| R-M02 | **中国用户 TeslaMate 渗透率低** | 🟡 | 高 | 写中文 TeslaMate 部署教程；对接国内社区 |
| R-M03 | **官方 Tesla App 功能增强** | 🟡 | 低 | 聚焦"历史数据分析"——官方 App 短期不会做 |
| R-M04 | **Tesla 官方开放数据 API** | 🟢 | 低 | 反而是机会——可以直接对接，减少对 TeslaMate 依赖 |

## 3.1 完整风险清单说明

本节为完整风险清单（附录），与 §1 Top 5 互为补充。Top 5 是"必须立即关注"，完整清单是"定期 review 参考"。

## 4. 风险监控

### 4.1 监控频率

| 风险类别 | 频率 |
|---|---|
| 技术 | 每周 review |
| 产品 | 每两周 review |
| 合规 | 每月 review |
| 运营 | 每月 review |
| 资源 | 每月 review |
| 市场 | 每季度 review |

### 4.2 风险升级

- 🟢 低风险：记录在案，无需升级
- 🟡 中风险：在项目周会讨论
- 🔴 高风险：立即通知 Jovi，制定应对方案

### 4.3 风险触发条件

| 风险 ID | 触发条件 | 应对动作 |
|---|---|---|
| R-T01 | v2.0 命令测试失败 | 推迟 v2.0 / 用官方 SDK |
| R-T02 | 中端机 3D fps < 20 | 默认关闭 3D，改为可选 |
| R-L01 | 收到 Tesla 律师函 | 立即下架 + 法律咨询 |
| R-L03 | 修改了 TeslaMate 源码 | 开源修改部分 / 撤回修改 |
| R-P05 | 单周接受 > 3 个新功能 | 暂停接受，评估范围 |
| R-O02 | Tesla API 政策变更 | 评估影响 + 调整路线图 |

## 5. 应急预案

### 5.1 iOS 审核被拒

```
Day 0: 收到拒绝通知
Day 1: 分析拒绝原因（Guideline 2.0/4.0/5.0 等）
Day 2-3: 修改 App + 重新提交
Day 4-7: 等待审核
若 2 次仍被拒: 联系 Apple Resolution Center 沟通
```

### 5.2 服务器/服务宕机

- **TeslaMate 官方**：用户自托管，与我们无关
- **Sentry 宕机**：不影响 App，仅崩溃报告缺失
- **PostHog 宕机**：不影响 App，仅分析数据缺失
- **EAS Build 宕机**：改用本地构建

### 5.3 重大 Bug 紧急修复

- 评估严重度（崩溃/数据错误/体验问题）
- 24h 内发布 hotfix（iOS Expedited Review + Android 立即发布）

## 6. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_09_设计参考.md`
