# glm_09 · 设计参考

## 1. 设计参考来源

| 仓库 | 路径 | 参考价值 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/` | Android 原生 UI 设计稿 |
| TeslamateCyberUI | `docs/git_ref/glm/repos/TeslamateCyberUI/screenshots/` | 赛博朋克风格 UI |
| teslamate-modern-dashboard | `docs/git_ref/glm/repos/teslamate-modern-dashboard/screenshot.png` | 现代响应式布局 |
| Tesla-app (RN) | `docs/git_ref/glm/repos/Tesla-app/` | RN + Three.js 3D 车辆 |
| Tesla_Clone_Swiftui | `docs/git_ref/openclaw/Tesla_Clone_Swiftui/` | iOS SwiftUI 原生实现 |
| teslamate-moblie | `docs/git_ref/openclaw/teslamate-moblie/` | 中文跨平台竞品 |
| hedgiemate | `docs/git_ref/mimo/hedgiemate/docs/images/screenshots/` | iOS 全平台功能参考 |
| mytess-teslamate-app | `docs/git_ref/mimo/mytess-teslamate-app/` | iOS 部署向导 |
| teslamate-chinese-dashboards | `docs/git_ref/mimo/teslamate-chinese-dashboards/screenshots/` | 中国本地化 UI |

## 2. UI 设计风格选项

### 2.1 风格 A · Apple-Like（推荐）

**特征**：
- 大圆角卡片（16-20pt radius）
- 毛玻璃背景（`expo-blur`）
- 系统字体（SF Pro / PingFang SC）
- 浅色/深色主题跟随系统
- 配色克制：黑/白/灰 + 1 个强调色

**参考**：
- iOS 官方 Tesla App 截图
- `Tesla_Clone_Swiftui` 的 SwiftUI 风格

**适用场景**：大众用户，接受度高

### 2.2 风格 B · 赛博朋克

**特征**：
- 霓虹色（青色 `#00f0ff` 主色）
- 深黑背景 `#222222`
- 等宽科幻字体（JetBrains Mono）
- 发光边框 + 扫描线动画
- 数据感强

**参考**：
- `TeslamateCyberUI`

**适用场景**：技术极客用户，可作为可选主题

### 2.3 风格 C · 现代 Material Design 3

**特征**：
- Material You 动态调色板
- 基于车色的主题色
- 充足留白
- 卡片 + 阴影

**参考**：
- `matedroid` 的主题系统

**适用场景**：Android 用户熟悉

### 2.4 推荐：风格 A + 基于 车色的强调色

**理由**：
- 大众接受度最高
- 基于 `exterior_color` 的强调色保留个性化
- 浅色/深色主题覆盖大部分场景
- 赛博朋克作为可选主题（P1+）

## 3. 配色系统

### 3.1 主色调（Light）

```
Background         #FFFFFF
Surface            #F5F5F7
Card               #FFFFFF + 阴影
Primary Text       #1D1D1F
Secondary Text     #6E6E73
Separator          #D2D2D7
Accent             #007AFF (iOS Blue) 或基于车色
Success            #34C759
Warning            #FF9500
Error              #FF3B30
```

### 3.2 主色调（Dark）

```
Background         #000000
Surface            #1C1C1E
Card               #2C2C2E
Primary Text       #FFFFFF
Secondary Text     #98989D
Separator          #38383A
Accent             #0A84FF 或基于车色
Success            #30D158
Warning            #FF9F0A
Error              #FF453A
```

### 3.3 基于车色的强调色映射

| exterior_color | 强调色（Light） | 强调色（Dark） |
|---|---|---|
| DeepBlue | #1E3A8A | #3B82F6 |
| RedMultiCoat | #B91C1C | #EF4444 |
| PearlWhite | #6B7280 | #9CA3AF |
| MidnightSilver | #4B5563 | #6B7280 |
| SolidBlack | #18181B | #3F3F46 |
| StealthGrey | #374151 | #6B7280 |

## 4. 字体系统

### 4.1 字体族

| 用途 | 字体 |
|---|---|
| 英文 | Inter（开源） |
| 中文 | 系统默认（iOS PingFang SC / Android Source Han Sans） |
| 数字（大数字显示） | JetBrains Mono |
| 等宽（代码/数据） | JetBrains Mono |

### 4.2 字号层级

| 层级 | 字号 | 行高 | 用途 |
|---|---|---|---|
| Display | 48pt | 56pt | Dashboard 电量百分比 |
| Title 1 | 28pt | 34pt | 页面标题 |
| Title 2 | 22pt | 28pt | 卡片标题 |
| Headline | 17pt | 22pt | 列表项主标题 |
| Body | 16pt | 22pt | 正文 |
| Callout | 15pt | 20pt | 辅助说明 |
| Subhead | 14pt | 20pt | 次要信息 |
| Footnote | 12pt | 16pt | 时间戳/单位 |
| Caption | 11pt | 13pt | 最小文字 |

## 5. 间距与布局

### 5.1 间距系统（8pt grid）

```
xs: 4pt
sm: 8pt
md: 16pt
lg: 24pt
xl: 32pt
xxl: 48pt
```

### 5.2 卡片

- 圆角：16pt
- 内边距：16pt
- 阴影：浅色模式 `0 2 8 rgba(0,0,0,0.06)`，深色模式无阴影（用 Surface 色区分）

### 5.3 列表项

- 高度：72pt（含间距）
- 内边距：16pt 左右
- 分隔线：1pt `Separator` 色

### 5.4 底部 Tab Bar

- 高度：83pt（含 safe area）
- 图标：24pt
- 文字：10pt
- 选中态：Accent 色

## 6. 图标系统

### 6.1 图标库

- **推荐**：SF Symbols（iOS）+ Material Symbols（Android）—— 通过 `react-native-vector-icons` 统一调用
- **备选**：Lucide（跨平台一致）

### 6.2 自定义图标

| 图标 | 用途 |
|---|---|
| 车辆剪影（按车型） | Dashboard 顶部 |
| 电池（带百分比填充） | Dashboard |
| 充电闪电 | 充电相关 |
| 胎压警报 | 胎压告警 |
| Sentry 模式 | Sentry 状态 |

## 7. 页面设计参考清单

### 7.1 Dashboard

| 参考来源 | 截图路径 | 借鉴点 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/main-dashboard.jpg` | 整体布局 + 3D 车辆图 |
| teslamate-moblie | `docs/git_ref/openclaw/teslamate-moblie/README.md` 内截图 | 中文 UI + 卡片布局 |
| HedgieMate | `docs/git_ref/mimo/hedgiemate/docs/images/screenshots/app-preview-1.png` | iOS 原生风格 |
| TeslamateCyberUI | `docs/git_ref/glm/repos/TeslamateCyberUI/screenshots/home.webp` | 赛博朋克备选 |

### 7.2 行程详情

| 参考来源 | 截图路径 | 借鉴点 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/drive-details.jpg` | 地图 + 曲线布局 |
| TeslamateCyberUI | `docs/git_ref/glm/repos/TeslamateCyberUI/screenshots/drive-detail.webp` | 赛博朋克曲线 |

### 7.3 充电详情

| 参考来源 | 截图路径 | 借鉴点 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/charge-details.jpg` | 充电曲线 |
| TeslamateCyberUI | `docs/git_ref/glm/repos/TeslamateCyberUI/screenshots/charge-detail.webp` | 赛博朋克充电 |

### 7.4 电池健康

| 参考来源 | 截图路径 | 借鉴点 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/battery-health.jpg` | 衰减曲线布局 |

### 7.5 行程列表

| 参考来源 | 截图路径 | 借鉴点 |
|---|---|---|
| matedroid | `docs/git_ref/glm/repos/matedroid/docs/screenshots/drives.jpg` | 列表项设计 |
| TeslamateCyberUI | `docs/git_ref/glm/repos/TeslamateCyberUI/screenshots/drive-list.webp` | 赛博朋克列表 |

### 7.6 iOS 原生风格

| 参考来源 | 路径 | 借鉴点 |
|---|---|---|
| Tesla_Clone_Swiftui | `docs/git_ref/openclaw/Tesla_Clone_Swiftui/TeslaApp/Views/` | SwiftUI 组件实现 |
| Tesla_Clone_Swiftui | `docs/git_ref/openclaw/Tesla_Clone_Swiftui/TeslaApp/Components/` | 自定义组件 |

## 8. 3D 车辆模型需求

### 8.1 模型清单

| 车型 | 变体 | 文件 |
|---|---|---|
| Model 3 | Highland + 原 | `model3-highland.glb`, `model3-original.glb` |
| Model Y | 标准 + Performance | `modelY-standard.glb`, `modelY-performance.glb` |
| Model S | Plaid | `modelS-plaid.glb` |
| Model X | Plaid | `modelX-plaid.glb` |
| Cybertruck | 标准 | `cybertruck.glb` |

### 8.2 模型规格

- 格式：GLTF/GLB（二进制）
- 多边形数：≤ 50,000（高画质）/ ≤ 20,000（中）/ ≤ 8,000（低）
- 材质：PBR
- 可动画部位：车门（开关）、车窗（升降）、轮毂（旋转）
- 文件大小：≤ 5MB/模型

### 8.3 车色材质

动态调整 `body` mesh 的 `material.color`：
```typescript
const colorMap: Record<string, string> = {
  'DeepBlue': '#1E3A8A',
  'RedMultiCoat': '#B91C1C',
  'PearlWhite': '#E5E7EB',
  'MidnightSilver': '#4B5563',
  'SolidBlack': '#18181B',
  'StealthGrey': '#374151',
};
```

### 8.4 模型来源

| 方案 | 成本 | 质量 |
|---|---|---|
| 自己用 Blender 建 | 免费 | 中（需建模技能） |
| Sketchfab 购买 | $20-100/个 | 高 |
| 委托外包 | $200-500 总 | 高 |
| 用 Tesla 官方公开模型 | 免费 | 不可商用 |

**推荐**：Sketchfab 购买 + 自己优化多边形数。

## 9. 动效规范

### 9.1 页面切换

- Push：右滑入（300ms ease-out）
- Pop：左滑出（300ms ease-out）
- Modal：底部滑入（400ms spring）

### 9.2 列表项

- 加载：淡入 + 上移（200ms）
- 删除：左滑显示删除按钮
- 长按：震动反馈（iOS Haptic / Android Vibrate）

### 9.3 Dashboard

- 状态徽章变化：颜色渐变（500ms）
- 电量数字：滚动动画（`react-native-reanimated`）
- 3D 车辆：单指旋转跟随、松开回弹

### 9.4 图表

- 加载：从左到右绘制曲线（800ms）
- 标签切换：交叉淡入淡出（200ms）

### 9.5 减少动效

支持系统"Reduce Motion"开关：
- 禁用所有非必要动画
- 保留必要的状态反馈（颜色变化）

## 10. 品牌设计

### 10.1 Logo 设计原则

- 简洁（< 3 个元素）
- 在小尺寸（16×16）下可识别
- 不含 Tesla T 字标
- 不含 TeslaMate 官方 logo
- 暗色/亮色版本

### 10.2 候选 Logo 概念

| 概念 | 描述 |
|---|---|
| 抽象电池 | 电池轮廓 + 数据曲线 |
| 闪电 + 数据点 | 充电闪电 + 散点图 |
| 抽象车辆 + 仪表盘 | 车辆轮廓 + 圆形仪表 |
| 字母 T + 波形 | T 字 + 数据波形 |

### 10.3 配色应用

- Logo 主色：Accent 色（基于品牌，非车色）
- App 图标：Logo + 渐变背景
- 启动屏：Logo + App 名称

## 11. 设计交付物清单

### 11.1 M0 交付

- [ ] Brand Kit（Logo + 配色 + 字体）
- [ ] UI Kit（通用组件库）
- [ ] 10+ 页面高保真设计稿（Figma）
- [ ] 交互原型（Figma）
- [ ] App 图标（iOS + Android）
- [ ] 启动屏
- [ ] App Store 截图（6.7" + 5.5"）
- [ ] Google Play 截图

### 11.2 后续交付

- [ ] 赛博朋克主题（v1.1）
- [ ] Apple Watch 界面（v1.2）
- [ ] 年度报告模板（v1.2）

## 12. 下一步

- ✅ 本文档完成
- ✅ PRD 全套 10 份文档完成
- ⏭ 进入 UI 设计稿阶段（Figma）
- ⏭ 或直接进入开发阶段（若 Jovi 同意）

---

## 🎉 PRD 全套完成

| 文档 | 状态 |
|---|---|
| glm_00_README.md | ✅ |
| glm_01_产品概述.md | ✅ |
| glm_02_功能需求.md | ✅ |
| glm_03_信息架构与页面流程.md | ✅ |
| glm_04_技术架构.md | ✅ |
| glm_05_数据模型与API.md | ✅ |
| glm_06_非功能需求.md | ✅ |
| glm_07_项目计划.md | ✅ |
| glm_08_风险评估.md | ✅ |
| glm_09_设计参考.md | ✅ |

**建议下一步**：
1. Jovi 审阅 PRD，确认关键决策点（品牌名、商业模式、视觉风格）
2. 启动 UI 设计稿（Figma）
3. 或直接进入 M1 开发（若 PRD 满足要求）
