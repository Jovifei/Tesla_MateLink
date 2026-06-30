# TODO-glm — app_glm 工程待完成与优化清单

> 最后更新: 2026-06-30
> 基于 opus 需求符合性审核结果整理
> 审核覆盖范围：app_glm/ 全部（iOS + Android + Web）

---

## ✅ 已完成修复

| # | 修复 | Commit |
|---|---|---|
| 1 | Android network_security_config（移除 usesCleartextTraffic） | 9d7d87e |
| 2 | Android UrlSecurity 运行时拦截公网 HTTP + 测试 | 02b8de9 |
| 3 | iOS Keychain WhenUnlockedThisDeviceOnly（阻止 iCloud 同步） | 2c847e9 |
| 4 | iOS Onboarding 持久化（onboardingDone + serverURL + apiToken） | 79fdd37 |
| 5 | iOS Onboarding ping path 修复（api/ping → /api/ping） | 79fdd37 |
| 6 | Gradle wrapper 补全 | 5aecd48 |
| 7 | iOS 详情页图表 "Simulated data" 标注 | 5aecd48 |

---

## 🔴 高优先级（功能缺失，影响用户体验）

### Android

| # | 问题 | 模块 | 难度 | 详情 |
|---|---|---|---|---|
| G-1 | 8 个分析页硬编码 mock | Analytics | 中 | BatteryHealthScreen / HeatmapScreen / EfficiencyScreen / VampireScreen / RangeScreen / DestinationsScreen / CostScreen / UpdatesScreen — 全用 `remember(){}` 随机数据，未接 ViewModel/Repository |
| G-2 | CI/CD 完全缺失 | DevOps | 小 | 无 `.github/workflows/`，无 detekt/ktlint 配置 |
| G-3 | 上架配置缺失 | Release | 中 | 无 play publisher 插件、隐私政策、应用截图 |
| G-4 | Room schema JSON 未生成 | Data | 小 | schemas/ 目录为空（需编译一次） |

### iOS

| # | 问题 | 模块 | 难度 | 详情 |
|---|---|---|---|---|
| G-5 | Drive/Charge 详情页图表用 sin()/random() 假数据 | Drives/Charges | 中 | generateDataPoints() 用数学函数生成 30 个数据点，需 API 时间序列端点 |
| G-6 | Settings 缺单位/时区设置 UI | Settings | 小 | UnitSystem enum 存在但 SettingsView 无 Picker；时区完全缺失 |
| G-7 | 7 天电量趋势图用硬编码数据 | Dashboard | 小 | BatteryTrendCard 用 `[75,72,68,70,73,76,78]` |
| G-8 | BatteryHealth 缺 range loss 维度 | Battery | 小 | 只有 capacity degradation，无续航里程损失计算 |

### Web

| # | 问题 | 模块 | 难度 | 详情 |
|---|---|---|---|---|
| G-9 | Web 端仅为翻译占位 | Web | 大 | 仅 3 个 i18n JSON 文件，无实际工程、无构建脚本 |

---

## 🟡 中优先级（跨平台一致性）

| # | 问题 | 影响 | 详情 |
|---|---|---|---|
| X-1 | CarStatus schema 双端分裂 | 数据模型 | iOS 扁平 33 字段 vs Android 嵌套 9 子对象 |
| X-2 | 字段命名不一致 | 序列化 | id vs carId, color vs exteriorColor, wheel vs wheelType |
| X-3 | 空值策略不一致 | 运行时 | iOS 偏非空，Android 全可空 |
| X-4 | shared/api-types.ts 未被双端引用 | 架构 | 仅 Web 用，非真正共享 |
| X-5 | ISO8601 解析双端实现方式不同 | 代码风格 | iOS 统一 ISO8601Parser，Android 分散在 4 文件 |

---

## 🟢 低优先级（代码质量）

| # | 问题 | 模块 |
|---|---|---|
| Q-1 | Android Mock 数据量不足（drives=6 需 60，charges=4 需 30） | Data |
| Q-2 | iOS API Client 未显式封装 16 端点（仅通用 fetch） | API |
| Q-3 | iOS 离线缓存用 JSON 文件而非 Core Data，TTL 1 天非 30 天 | Storage |
| Q-4 | iOS Dashboard 不读缓存、无 Offline banner | Dashboard |
| Q-5 | iOS Theme 缺"跟随系统"第三态（仅 isDarkMode 二态） | Theme |
| Q-6 | iOS 2D 车辆图为 SF Symbol 简化（非真实 2D 图） | Dashboard |

---

## 📋 按目录快速修复清单

### app_glm/android/

```
app/src/main/java/com/teslamatelink/
├── data/
│   └── [ ] G-1: 8 个硬编码分析页接 Repository
│       ├── ui/battery/BatteryHealthScreen.kt
│       ├── ui/heatmap/HeatmapScreen.kt
│       ├── ui/efficiency/EfficiencyScreen.kt
│       ├── ui/vampire/VampireScreen.kt
│       ├── ui/range/RangeScreen.kt
│       ├── ui/destinations/DestinationsScreen.kt
│       ├── ui/cost/CostScreen.kt
│       └── ui/updates/UpdatesScreen.kt
├── [ ] G-2: 创建 .github/workflows/android.yml
├── [ ] G-3: 添加 play publisher 插件配置
└── [ ] G-4: 运行 gradle build 生成 Room schema JSON
```

### app_glm/ios/

```
MateLink/
├── Core/
│   ├── API/
│   │   └── [ ] Q-2: 显式封装 16 端点方法
│   └── Storage/
│       └── [ ] Q-3: 离线缓存改用 Core Data + 30 天 TTL
├── Features/
│   ├── Dashboard/
│   │   └── [ ] G-7: 7 天趋势从 API 获取
│   │   └── [ ] Q-4: Dashboard 读缓存 + Offline banner
│   │   └── [ ] Q-6: 2D 车辆图替换 SF Symbol
│   ├── Drives/
│   │   └── [ ] G-5: 图表接真实 API 时间序列数据
│   ├── Charges/
│   │   └── [ ] G-5: 图表接真实 API 时间序列数据
│   ├── Battery/
│   │   └── [ ] G-8: 补 range loss 维度
│   ├── Settings/
│   │   └── [ ] G-6: 添加单位 Picker + 时区设置
│   └── Theme/
│       └── [ ] Q-5: 添加"跟随系统"第三态
```

### app_glm/web_matelink/

```
[ ] G-9: Web 端需要完整工程化（当前仅翻译文件）
```

---

## 📊 审核符合率参考

| 模块 | 符合率 | 关键缺失 |
|---|---|---|
| iOS Foundation + Dashboard | 75% | 离线缓存不符、Onboarding 持久化（已修）、Theme 缺第三态 |
| iOS Drives + Charges + Analytics | 79% | 假数据图表（已标注）、缺 range loss |
| Android 全量 | 79% | 8 个硬编码页、CI/CD 缺失、上架配置缺失 |
| P1 中国/通知/打磨 | 100% | 全部完成 |
| P2 3D/Watch/Reports | 47% | Reports/Export 在 app_mimo（非 app_glm） |
| 跨平台 + 安全 | 30% | 数据模型分裂、Web 缺失 |

---

## 🔧 执行建议

**第一梯队（1 天，立即可做）：**
- G-6, G-7, G-8, Q-5 → 小 UI 改动

**第二梯队（2-3 天，需要数据层）：**
- G-1（8 个硬编码页接 Repository）→ 最大工作量但最有价值
- G-5（图表接真实数据）→ 需 API 时间序列端点

**第三梯队（需要工具链）：**
- G-2（CI/CD）→ 需 GitHub Actions 配置
- G-3（上架）→ 需 Google Play 开发者账号
- G-4（Room schema）→ 需 gradle 环境编译
