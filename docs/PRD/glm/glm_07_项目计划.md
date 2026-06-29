# glm_07 · 项目计划

## 1. 里程碑总览

| 里程碑 | 时间 | 交付物 | 验收标准 |
|---|---|---|---|
| **M0** PRD 与设计冻结 | Week 0 | 本套 PRD + UI 设计稿 | Jovi 签字确认 |
| **M1** MVP Alpha | Week 6 | 内测 APK / TestFlight | 核心功能可用 |
| **M2** MVP Beta | Week 10 | 公测 TestFlight / Google Play 内测 | 50 名内测用户 |
| **M3** iOS 上架 | Week 12 | App Store 正式版 | App Store 审核通过 |
| **M4** Android 上架 | Week 12 | Google Play 正式版 | Google Play 审核通过 |
| **M5** v1.1 中国本地化 | Week 16 | 高德地图 + 分时电价 | 中国用户体验达标 |
| **M6** v1.2 高级分析 + 3D 车辆 + Watch | Week 22 | 热力图 + 3D 车辆 + Apple Watch + 年度报告 | — |
| **M7** v2.0 车端命令 | Week 24 | 基于 vehicle-command SDK | Fleet API 申请完成 |

> **关于 24 周的澄清**（回应 mimo 反驳）：
> - **MVP 阶段 = M1-M4 = 12 周**（与 mimo 一致），目标是 App Store + Google Play 上架
> - **后续 12 周 = M5-M7**，是 v1.1 / v1.2 / v2.0 的迭代，**不属于 MVP**
> - mimo 的"12 周上架"对应本计划的 M1-M4，两者实际一致

## 2. 阶段详细分解

### 2.1 M0 · PRD 与设计冻结（Week 0）

| 任务 | 工时 | 交付 |
|---|---|---|
| 完成 PRD 全套文档 | 3d | `docs/PRD/glm/*.md` |
| 收集竞品截图库 | 1d | `docs/design/screenshots/` |
| 绘制 UI 线框图（Figma） | 5d | Figma 文件 |
| 设计稿高保真（Figma） | 7d | Figma 文件 |
| 品牌设计（Logo + 配色 + 字体） | 3d | Brand Kit |
| 技术方案评审 | 1d | 评审纪要 |
| Jovi 签字 | — | 确认邮件 |

**关键决策**（M0 必须确认）：
- [ ] 品牌名
- [ ] 商业模式（Freemium vs 全免费）
- [ ] 技术方案（RN+Expo 确认）
- [ ] 视觉风格（赛博朋克 vs 现代 vs Apple-like）

### 2.2 M1 · MVP Alpha（Week 1-6）

#### Week 1 · 项目初始化

| 任务 | 工时 |
|---|---|
| 创建 GitHub 仓库 + 初始化 Expo 项目 | 0.5d |
| 配置 TypeScript + ESLint + Prettier | 0.5d |
| 配置 React Navigation + 底部 Tab | 0.5d |
| 配置 React Query + Zustand + MMKV | 0.5d |
| 配置 Sentry + PostHog | 0.5d |
| 搭建本地 TeslaMate + TeslaMateApi（测试环境） | 1d |
| 编写 API Client（TypeScript） | 1d |
| 编写 Mock Client + Mock 数据 | 1d |
| CI/CD（GitHub Actions） | 1d |
| 文档：README + ARCHITECTURE | 0.5d |

#### Week 2 · Onboarding + Dashboard

| 任务 | 工时 |
|---|---|
| P-001 Welcome 页 | 0.5d |
| P-002 Server Config 页 | 1d |
| P-003 Connection Test | 0.5d |
| P-004 Dashboard 基础布局 | 1d |
| 实时状态卡片（F-003） | 1.5d |
| 状态徽章逻辑 | 0.5d |
| 地图缩略图（F-005） | 1d |
| 下拉刷新 | 0.5d |
| Mock 模式集成（F-014） | 0.5d |

#### Week 3 · 行程模块

| 任务 | 工时 |
|---|---|
| P-005 Drive List 页（F-006） | 1.5d |
| 分页 + 无限滚动 | 0.5d |
| 日期范围筛选 | 0.5d |
| P-006 Drive Detail 页（F-007） | 2d |
| 完整轨迹地图 + Polyline | 1d |
| 轨迹抽稀算法（借鉴 matedroid） | 0.5d |
| 图表标签切换（速度/功率/海拔/温度/胎压） | 1.5d |
| 空状态 + 加载状态 | 0.5d |

#### Week 4 · 充电模块

| 任务 | 工时 |
|---|---|
| P-007 Charge List 页（F-008） | 1.5d |
| P-008 Charge Detail 页（F-009） | 2d |
| 充电曲线图（功率/电压/温度） | 1d |
| 当前充电卡片（实时） | 1d |
| AC/DC 筛选 | 0.5d |
| 成本显示 | 0.5d |

#### Week 5 · 电池健康 + 更新 + 多车

| 任务 | 工时 |
|---|---|
| P-010 Battery Health 页（F-010） | 2d |
| 衰减曲线图 | 1d |
| P-011 Updates 页（F-011） | 0.5d |
| P-013 Car Switcher（F-002） | 1d |
| P-012 Settings 页（F-013） | 1.5d |
| 浅色/深色主题（F-012） | 0.5d |

#### Week 6 · 2D 车辆图 + 离线缓存 + 打磨

| 任务 | 工时 |
|---|---|
| F-004 2D 车辆图组件 | 1d |
| 车型轮廓图采购/制作（5 车型 × 3 角度） | 1d（可从 matedroid 借鉴） |
| 动态着色（tintColor） | 0.5d |
| 长按切换角度 | 0.5d |
| F-015 离线缓存 | 1d |
| 全流程 Bug 修复 | 1.5d |
| Alpha 构建（TestFlight + APK） | 0.5d |

**M1 验收**：
- [ ] 内测 5 名用户能完成 Onboarding → Dashboard → Drives → Charges 全流程
- [ ] 5s 轮询稳定，无崩溃
- [ ] 2D 车辆图加载 < 200ms
- [ ] Mock 模式可演示

> **变更说明**：3D 车辆降级为 F-205（v1.2），MVP 用 2D 方案，节省 3-4 天工期。

### 2.3 M2 · MVP Beta（Week 7-10）

#### Week 7 · 测试与修复

| 任务 | 工时 |
|---|---|
| 单元测试（核心逻辑） | 2d |
| E2E 测试（Detox） | 2d |
| 性能优化（启动/滚动） | 1d |

#### Week 8 · iOS 上架准备

| 任务 | 工时 |
|---|---|
| App Store Connect 配置 | 0.5d |
| App 截图（6.7" + 5.5"） | 1d |
| App 描述 + 免责声明 | 0.5d |
| 隐私政策 URL | 0.5d |
| IAP 配置（PRO 解锁） | 1d |
| App Store 审核提交 | 0.5d |
| 审核反馈处理（预期 2 轮） | 1.5d |

#### Week 9 · Android 上架准备

| 任务 | 工时 |
|---|---|
| Google Play Console 配置 | 0.5d |
| Play Store 截图 + 描述 | 1d |
| 数据安全表 | 0.5d |
| 内购配置 | 0.5d |
| Google Play 内测发布 | 0.5d |
| 公测发布 | 0.5d |

#### Week 10 · Beta 反馈收集

| 任务 | 工时 |
|---|---|
| 50 名内测用户邀请 | 1d |
| 收集反馈（GitHub Issues） | 持续 |
| Bug 分类与修复 | 4d |
| Beta 2 构建 | 1d |

**M2 验收**：
- [ ] 50 名内测用户
- [ ] App Store / Google Play 通过审核
- [ ] 崩溃率 < 1%
- [ ] 用户反馈 NPS ≥ 30

### 2.4 M3 + M4 · 正式上架（Week 11-12）

| 任务 | 工时 |
|---|---|
| 最终 Bug 修复 | 3d |
| 正式版构建（iOS + Android） | 1d |
| App Store 正式发布 | 0.5d |
| Google Play 正式发布 | 0.5d |
| 发布公告（Twitter / Reddit / Discord） | 1d |

**M3+M4 验收**：
- [ ] App Store 上架
- [ ] Google Play 上架
- [ ] 首周下载量 ≥ 500
- [ ] 首周评分 ≥ 4.0

### 2.5 M5 · v1.1 中国本地化（Week 13-16）

| 任务 | 工时 |
|---|---|
| F-101 高德地图 SDK 集成 | 2d |
| GCJ-02 坐标纠偏算法 | 0.5d |
| 国内外地图切换逻辑 | 0.5d |
| F-102 分时电价配置 | 2d |
| 充电成本重算逻辑 | 1d |
| 充电桩性价比榜 | 1d |
| F-103 iOS Widget（WidgetKit） | 3d |
| F-103 Android Widget | 2d |
| F-104 推送通知（APNs + FCM） | 4d |
| F-105 行程效率评分 | 2d |
| F-106 访问过的地区 | 2d |
| F-107 统计概览 | 2d |
| F-108 多语言（5 种） | 3d |
| 测试与上架 | 3d |

### 2.6 M6 · v1.2 高级分析 + 3D 车辆 + Apple Watch（Week 17-22）

| 任务 | 工时 |
|---|---|
| F-201 电池热力图 | 2d |
| F-202 Top 目的地 | 1d |
| F-203 Vampire Drain 分析 | 2d |
| F-204 充电曲线分析 | 2d |
| F-205 3D 车辆展示（Three.js + R3F） | 4d |
| F-206 年度报告 | 3d |
| F-207 数据导出（CSV/PDF） | 3d |
| F-208 多实例切换 | 2d |
| **F-209 Apple Watch App**（Jovi 重新升级到 P2） | **9d** |
| 测试与上架（含 Watch App 独立审核） | 5d |

### 2.7 M7 · v2.0 车端命令 + AI（Week 21-24）

| 任务 | 工时 |
|---|---|
| Tesla Developer 注册 + Fleet API 申请 | 外部（数周） |
| F-301 vehicle-command SDK 集成 | 5d |
| OAuth 流程 | 3d |
| 命令 UI（wake/lock/charge_start/climate_on） | 3d |
| F-302 AI 自然语言查询（基于 teslamate-mcp 思路） | 5d |
| F-303 多车对比 | 3d |
| F-304 驾驶行为评分 | 3d |
| F-305 地理围栏管理 | 2d |
| 测试与上架 | 3d |

## 3. 资源需求

### 3.1 人力

| 角色 | 人数 | 工期 | 备注 |
|---|---|---|---|
| 产品 + 设计 | 1（Jovi） | M0 | PRD + UI 设计 |
| 主开发（RN） | 1 | M0-M7 | 全栈 RN |
| iOS 顾问（兼职） | 0.2 | M2-M3 | iOS 审核咨询 |
| 设计（兼职） | 0.5 | M0 | 品牌设计 |
| 测试（兼职） | 0.3 | M1-M2 | 内测组织 |

**总人力**：约 1.5 FTE × 24 周 = 36 人周

### 3.2 外部资源

| 资源 | 费用 | 说明 |
|---|---|---|
| Apple Developer | $99/年 | 必须 |
| Google Play | $25 一次性 | 必须 |
| Sentry | $0-26/月 | 免费版够用 |
| PostHog 自托管 | $5-20/月 | VPS 费用 |
| 3D 车辆模型（v1.2+） | $200-500 | 5 个车型 GLTF（MVP 不需要） |
| 2D 车辆轮廓图（MVP） | $50-100 | 5 车型 × 3 角度 PNG（可从 matedroid 借鉴） |
| 高德地图 SDK | 免费 | 个人开发者 |
| Tesla Developer | 免费 | Fleet API 申请 |
| EAS Build | $0-99/年 | 免费版 15 次/月 |

**总预算**：约 $500-1000（一次性） + $300/年（持续）

### 3.3 硬件

| 设备 | 用途 | 状态 |
|---|---|---|
| Mac（M1+） | iOS 构建 | 需采购或租用 |
| iPhone（测试） | iOS 实测 | 需采购 |
| Android 手机（测试） | Android 实测 | 已有 |
| NAS / VPS | 本地 TeslaMate 测试 | 已有 |

## 4. 风险缓冲

### 4.1 工期缓冲

- 每个里程碑预留 20% 缓冲时间
- 关键路径任务（上架审核、iOS 审核）预留 1 周额外时间

### 4.2 降级方案

| 风险 | 降级 |
|---|---|
| 3D 车辆延期 | 已降级为 F-205（v1.2），MVP 用 2D 图片（已实施） |
| iOS 审核被拒 | 优先上架 Android，iOS 延后 |
| Fleet API 申请延期 | v2.0 推迟，先做 v1.3 其他功能 |

## 5. 决策点

### 5.1 M0 决策点（Week 0 末）

- [ ] 品牌名确认
- [ ] 商业模式确认
- [ ] 视觉风格确认
- [ ] 技术方案确认

### 5.2 M1 决策点（Week 6 末）

- [ ] 是否在 v1.2 启动 3D 车辆方向
- [ ] 是否调整 MVP 功能范围

### 5.3 M2 决策点（Week 10 末）

- [ ] 是否正式上架
- [ ] 商业模式是否调整（如全免费）

### 5.4 M5 决策点（Week 16 末）

- [ ] 是否继续 v1.2 投入
- [ ] 是否启动 v2.0 车端命令方向

## 6. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_08_风险评估.md`
