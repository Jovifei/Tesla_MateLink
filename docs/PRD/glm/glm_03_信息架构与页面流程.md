# glm_03 · 信息架构与页面流程

## 1. 信息架构总览

```
App
├── Onboarding (首次启动)
│   ├── Welcome
│   ├── Connect to TeslaMate (F-001)
│   └── Permission Setup (通知/位置)
│
├── Main Tabs (底部 Tab Bar)
│   ├── ① Dashboard (F-003) ── 默认首页
│   ├── ② Drives (F-006)
│   ├── ③ Charges (F-008)
│   └── ④ More
│       ├── Battery Health (F-010)
│       ├── Updates (F-011)
│       ├── Statistics (F-107, P1)
│       ├── Visited Regions (F-106, P1)
│       └── Settings
│
├── Detail Pages (Push)
│   ├── Drive Detail (F-007)
│   ├── Charge Detail (F-009)
│   └── Car Switcher (F-002)
│
└── System
    ├── Push Notifications (F-104, P1)
    ├── Widget Config (F-103, P1)
    └── Apple Watch Glance (F-201, P2)
```

## 2. 页面清单

### 2.1 全部页面

| ID | 页面 | 层级 | 触发 | 功能 ID |
|---|---|---|---|---|
| P-001 | Welcome | Onboarding | 首次启动 | — |
| P-002 | Server Config | Onboarding | Welcome 下一步 | F-001 |
| P-003 | Connection Test Result | Onboarding | Server Config 提交 | F-001 |
| P-004 | Dashboard | Tab 1 | Tab Bar / 默认 | F-003, F-004, F-005 |
| P-005 | Drive List | Tab 2 | Tab Bar | F-006 |
| P-006 | Drive Detail | Push | 点击列表项 | F-007 |
| P-007 | Charge List | Tab 3 | Tab Bar | F-008 |
| P-008 | Charge Detail | Push | 点击列表项 | F-009 |
| P-009 | More | Tab 4 | Tab Bar | — |
| P-010 | Battery Health | Push | More 点击 | F-010 |
| P-011 | Updates | Push | More 点击 | F-011 |
| P-012 | Settings | Push | More 点击 | F-013 |
| P-013 | Car Switcher | Modal | Dashboard 顶部车辆名点击 | F-002 |
| P-014 | Mock Mode Preview | Modal | Settings 开启 Mock | F-014 |
| P-015 | About | Push | Settings → About | — |

### 2.2 P1 新增页面

| ID | 页面 | 层级 | 功能 ID |
|---|---|---|---|
| P-101 | Tariff Config | Push (Settings) | F-102 |
| P-102 | Statistics | Push (More) | F-107 |
| P-103 | Visited Regions | Push (More) | F-106 |
| P-104 | Notification Settings | Push (Settings) | F-104 |
| P-105 | Widget Config | System | F-103 |

### 2.3 P2 新增页面

| ID | 页面 | 层级 | 功能 ID |
|---|---|---|---|
| P-201 | Heatmap | Push (More) | F-202 |
| P-202 | Top Destinations | Push (More) | F-203 |
| P-203 | Vampire Drain | Push (More) | F-204 |
| P-204 | Annual Report | Push (More) | F-206 |
| P-205 | Data Export | Push (Settings) | F-207 |

## 3. 核心用户流程

### 3.1 Flow A · 首次启动 → 看到 Dashboard

```
[P-001 Welcome]
      │
      ▼
[P-002 Server Config]
  输入 URL + Token
      │
      ▼
  点击 "Test Connection"
      │
      ├── 失败 ──► 显示错误，停留在 P-002
      │
      ▼ 成功
[P-003 Connection Test Result]
  显示找到 N 辆车
      │
      ▼
[P-004 Dashboard]
  显示第一辆车的实时状态
```

**时长目标**：从启动到 Dashboard < 30s（含用户输入）

### 3.2 Flow B · 查看行程详情

```
[P-004 Dashboard]
      │
      ▼ 点击 Tab 2
[P-005 Drive List]
  显示最近 20 条行程
      │
      ▼ 上拉加载
  显示第 21-40 条
      │
      ▼ 点击某条
[P-006 Drive Detail]
  显示轨迹 + 曲线 + 统计
      │
      ▼ 返回
[P-005 Drive List]
```

**时长目标**：从点击列表项到详情可交互 < 1s

### 3.3 Flow C · 切换车辆

```
[P-004 Dashboard]
  顶部车辆名 "Model 3 - Lightning"
      │
      ▼ 点击
[P-013 Car Switcher] (Modal)
  显示所有车辆卡片
  ┌─────────────────┐
  │ ✓ Model 3       │
  │   Lightning     │
  ├─────────────────┤
  │   Model Y       │
  │   Thunder       │
  └─────────────────┘
      │
      ▼ 点击 Model Y
[P-004 Dashboard]
  显示 Model Y 的状态
```

### 3.4 Flow D · 充电中查看进度

```
[后台收到推送 / 用户主动打开 App]
      │
      ▼
[P-004 Dashboard]
  状态徽章: "Charging"
  充电进度卡片:
    ┌────────────────────────┐
    │  ████████░░░░  62%     │
    │  +24.5 kWh · 35 min    │
    │  11.2 kW · 230V · 49A  │
    └────────────────────────┘
      │
      ▼ 下拉刷新
  立即拉取最新 status
```

### 3.5 Flow E · 离线查看历史

```
[用户在地铁无网络]
      │
      ▼ 打开 App
[P-004 Dashboard]
  顶部黄色 banner: "Offline - showing last known status"
  数据来自缓存（5 分钟前的）
      │
      ▼ 点击 Tab 2
[P-005 Drive List]
  顶部 banner: "Offline"
  列表数据来自缓存（24h 内的）
      │
      ▼ 点击某条
[P-006 Drive Detail]
  若缓存有 → 显示
  若缓存无 → 显示 "This drive is not cached"
```

### 3.6 Flow F · 启用 Mock 模式

```
[P-012 Settings]
      │
      ▼ 开启 "Mock Mode" 开关
[P-014 Mock Mode Preview] (Modal)
  显示提示: "Mock mode enabled. All data is fake."
      │
      ▼ 关闭 Modal
[P-004 Dashboard]
  显示 Mock 数据（虚拟车辆 "Demo Car"）
```

## 4. 页面详细设计

### 4.1 P-004 Dashboard

```
┌─────────────────────────────────┐
│ ☰  Model 3 - Lightning    ⚙️    │  ← 顶部栏（车辆名 + 设置）
├─────────────────────────────────┤
│                                 │
│         [3D Vehicle]            │  ← 3D 车辆展示区
│                                 │
│      🔋 62%  ·  318 km          │  ← 电量 + 续航
│                                 │
├─────────────────────────────────┤
│  ● Charging · 35 min remaining  │  ← 状态徽章
├─────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐    │
│  │ 📍 Home  │  │ 🛣️ 12,345│    │  ← 位置 + 里程
│  │ Shanghai │  │ km        │    │
│  └──────────┘  └──────────┘    │
│  ┌──────────┐  ┌──────────┐    │
│  │ 🌡️ 22°C  │  │ 🛞 2.4 bar│    │  ← 温度 + 胎压
│  │ in / 8 out│  │ 4 tires   │    │
│  └──────────┘  └──────────┘    │
├─────────────────────────────────┤
│  ┌─────────────────────────┐    │  ← 充电中额外卡片
│  │ ⚡ Charging Progress    │    │
│  │ ████████░░░░  62%       │    │
│  │ +24.5 kWh · 11.2 kW     │    │
│  └─────────────────────────┘    │
├─────────────────────────────────┤
│  ① Dashboard  ② Drives  ③ ⚡  ④ More │  ← 底部 Tab
└─────────────────────────────────┘
```

### 4.2 P-005 Drive List

```
┌─────────────────────────────────┐
│  ← Drives           [Filter]    │
├─────────────────────────────────┤
│  Today · 3 drives               │
├─────────────────────────────────┤
│  🚗 14:30 → 15:45   18.2 km     │
│     Home → Office                │
│     12.3 kWh · 168 Wh/km        │
├─────────────────────────────────┤
│  🚗 08:15 → 08:45    8.5 km     │
│     Kid's School → Office        │
│     5.8 kWh · 172 Wh/km         │
├─────────────────────────────────┤
│  Yesterday · 2 drives            │
├─────────────────────────────────┤
│  🚗 18:00 → 18:30   12.0 km     │
│     Office → Home                │
│     7.5 kWh · 165 Wh/km         │
├─────────────────────────────────┤
│  ...                             │
│  [上拉加载更多]                  │
└─────────────────────────────────┘
```

### 4.3 P-006 Drive Detail

```
┌─────────────────────────────────┐
│  ← Drive Detail                 │
├─────────────────────────────────┤
│  📅 2026-06-22 14:30 - 15:45    │
│  ⏱️ 1h 15min · 🛣️ 18.2 km       │
├─────────────────────────────────┤
│                                 │
│      [完整轨迹地图]              │
│      起点 → 终点                 │
│                                 │
├─────────────────────────────────┤
│  ┌────────┬────────┬────────┐   │
│  │Avg Spd │Max Spd │Energy  │   │
│  │45 km/h │82 km/h │12.3 kWh│   │
│  └────────┴────────┴────────┘   │
│  ┌────────┬────────┬────────┐   │
│  │Effic.  │Out Temp│Elev Δ  │   │
│  │168Wh/km│ 28°C   │ +120 m │   │
│  └────────┴────────┴────────┘   │
├─────────────────────────────────┤
│  [Speed] [Power] [Alt] [Temp] [Tire] │  ← 图表标签
├─────────────────────────────────┤
│                                 │
│      [选中标签的曲线图]          │
│                                 │
└─────────────────────────────────┘
```

### 4.4 P-012 Settings

```
┌─────────────────────────────────┐
│  ← Settings                     │
├─────────────────────────────────┤
│  CONNECTION                     │
│  Server URL          teslamate.example.com │
│  API Token           •••••••••  │
│  [Test Connection]              │
│  Car Selected        Model 3    │
├─────────────────────────────────┤
│  PREFERENCES                    │
│  Units               km / °C    │
│  Timezone            Asia/Shanghai│
│  Theme               System     │
│  Language            中文       │
├─────────────────────────────────┤
│  CHINA LOCALIZATION (P1)        │
│  Map Provider        Amap / Google│
│  Time-of-Use Tariff  [On]      │
├─────────────────────────────────┤
│  DEVELOPMENT                    │
│  Mock Mode           [Off]      │
│  Debug Logging       [Off]      │
├─────────────────────────────────┤
│  ABOUT                          │
│  Version             1.0.0      │
│  Open Source License            │
│  Privacy Policy                 │
│  GitHub Repo                    │
└─────────────────────────────────┘
```

## 5. 导航模式

### 5.1 底部 Tab Bar

4 个 Tab，符合主流 App 习惯：
- ① Dashboard（首页）
- ② Drives
- ③ Charges
- ④ More

颜色：跟随主题，选中态高亮色基于车色（参考 matedroid 的"基于车色的主题"）。

### 5.2 Push 导航

详情页用 `react-navigation` 的 Stack 导航，左上角返回按钮。

### 5.3 Modal

车辆切换、Mock 模式预览等临时性页面用 Modal（从底部滑入或中心弹出）。

## 6. 状态管理

### 6.1 全局状态

| 状态 | 存储 | 用途 |
|---|---|---|
| 服务器配置（URL + Token） | expo-secure-store | API 调用 |
| 当前车辆 ID | MMKV | 多车切换 |
| 主题模式 | MMKV | 浅色/深色 |
| 单位偏好 | MMKV | km/mile, °C/°F |
| 语言 | MMKV | i18n |
| Mock 模式开关 | MMKV | 开发调试 |

### 6.2 页面状态

| 状态 | 管理 | 示例 |
|---|---|---|
| Dashboard 实时状态 | React Query（5s refetch） | car status |
| 列表数据 | React Query（infinite query） | drives/charges 列表 |
| 详情数据 | React Query（staleTime 5min） | 单条 drive/charge |

## 7. 交互细节

### 7.1 下拉刷新

所有列表页和 Dashboard 支持下拉刷新：
- 动画：自定义 RefreshControl
- 触发：手动下拉
- 反馈：立即拉取最新数据，不等待定时器

### 7.2 空状态

每个列表页都要有空状态：
- Drive List 空：插画 + "No drives yet. Go for a drive!"
- Charge List 空：插画 + "No charges yet. Time to plug in!"
- Battery Health 数据不足：插画 + "Need more data. Drive for a few weeks."

### 7.3 加载状态

- 首次加载：骨架屏（Skeleton）
- 刷新：顶部 Loading 指示器
- 详情加载：卡片骨架屏

### 7.4 错误状态

- 网络错误：Toast + "Retry" 按钮
- 服务器错误：全屏错误页 + 错误码 + "Retry"
- 认证失败：弹窗提示 + 跳转 Settings

## 8. 无障碍（Accessibility）

| 要素 | 要求 |
|---|---|
| 字体大小 | 支持系统字体缩放（Dynamic Type / Font Scale） |
| 颜色对比度 | WCAG AA 标准（4.5:1） |
| VoiceOver / TalkBack | 所有可交互元素有 label |
| 触摸目标 | ≥ 44pt × 44pt（iOS）/ 48dp × 48dp（Android） |
| 减少动效 | 支持系统"减少动效"开关 |

## 9. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_04_技术架构.md`
