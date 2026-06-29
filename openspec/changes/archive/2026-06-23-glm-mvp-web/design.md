# Design: MateLink Web Dashboard

## 1. Architecture

```
web_matelink/
├── public/
│   └── mock_data.json           # 共享 Mock 数据
├── src/
│   ├── api/                     # ★ API Client (fetch)
│   │   ├── client.ts            # HTTP 封装 (Bearer Token)
│   │   ├── teslamate.ts         # 16 endpoints
│   │   ├── mock.ts              # Mock 实现
│   │   └── types.ts             # API 类型
│   ├── components/              # 通用组件
│   │   ├── Layout/              # 侧边栏 + 顶栏
│   │   ├── Map/                 # Leaflet 地图
│   │   ├── Chart/               # Recharts 封装
│   │   ├── StatCard/            # 统计卡片
│   │   └── Skeleton/            # 加载骨架
│   ├── pages/                   # 12 页面
│   │   ├── Dashboard.tsx
│   │   ├── Drives.tsx
│   │   ├── DriveDetail.tsx
│   │   ├── Charges.tsx
│   │   ├── ChargeDetail.tsx
│   │   ├── BatteryHealth.tsx
│   │   ├── Statistics.tsx       # ★ 钻取 (Year→Month→Day→Drive)
│   │   ├── Heatmap.tsx          # ★ GitHub 风格热力图
│   │   ├── TopDestinations.tsx  # ★ 大屏地图标记
│   │   ├── Efficiency.tsx       # ★ 速度-效率曲线
│   │   ├── Settings.tsx
│   │   └── About.tsx
│   ├── hooks/                   # useCarStatus / useDrives / ...
│   ├── store/                   # React Query config
│   ├── theme/                   # 配色
│   └── App.tsx                  # 路由入口
├── Dockerfile                   # nginx + static
├── nginx.conf
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

## 2. Tech Stack (Pure Web)

| 层 | 选型 | 原因 |
|---|---|---|
| Framework | React 18 + Vite | 快速 HMR + 理想构建速度 |
| Language | TypeScript 5 | 类型安全 |
| CSS | Tailwind CSS 3 | Utility-first，快速布局 |
| Charts | Recharts | 声明式 SVG，React 集成好 |
| Map | Leaflet + OSM tiles | 开源免费，无须 API Key |
| Router | React Router 6 | 标准方案 |
| State (server) | TanStack Query 5 | 缓存 + 分页 + 自动 invalidate |
| State (client) | Zustand | 设置/主题/车选择 |
| Mock | 内置 (import mock_data.json) | 开发期不依赖 TeslaMate |
| Lint | ESLint + Prettier | 标准 |
| Build | Vite (ESBuild + Rollup) | 快 |
| Deploy | Docker (nginx + gzip) | 一键 |

## 3. Page Design (16 pages — 全面覆盖 TeslaMate 数据)

> **对比基准**：matedroid (Android, 67★)、teslamate-modern-dashboard (Web, 3★)、TeslamateCyberUI (Web, 37★)、TeslaMate Grafana dashboards (20+ 内置面板)
> **策略**：每个页面标注「展示内容表」、「交互行为表」、「社区对照（缺什么）」三部分。

### 3.0 页面总览

| # | 页面 | 路由 | P0 | 借鉴来源 |
|---|---|---|---|---|
| 1 | Dashboard | `/` | ✅ 已有 | matedroid + modern-dashboard + CyberUI |
| 2 | Drive List | `/drives` | ✅ 已有 | matedroid + CyberUI |
| 3 | Drive Detail | `/drives/:id` | 🛠 需补充 | matedroid (速度/功率/海拔曲线) |
| 4 | Charge List | `/charges` | ✅ 已有 | matedroid + CyberUI |
| 5 | Charge Detail | `/charges/:id` | 🛠 需补充 | matedroid (功率/电压/温度曲线) |
| 6 | Battery Health | `/battery` | ✅ 已有 | matedroid + TeslaMate grafana |
| 7 | Statistics Drill | `/statistics` | 🛠 占位 | matedroid (MileageScreen 钻取) |
| 8 | Drive Heatmap | `/heatmap` | 🛠 占位 | modern-dashboard (GitHub 风格) |
| 9 | Top Destinations | `/destinations` | 🛠 占位 | modern-dashboard |
| 10 | Efficiency Curve | `/efficiency` | 🛠 占位 | TeslaMate grafana efficiency.json |
| 11 | Vampire Drain ★ | `/vampire` | 🆕 新增 | TeslaMate grafana vampire-drain.json |
| 12 | Timeline ★ | `/timeline` | 🆕 新增 | TeslaMate grafana timeline.json |
| 13 | Firmware Updates ★ | `/updates` | 🆕 新增 | matedroid + PRD F-011 |
| 14 | Projected Range ★ | `/range` | 🆕 新增 | TeslaMate grafana projected-range.json |
| 15 | Charging Cost ★ | `/cost` | 🆕 新增 | TeslaMate grafana charging-stats.json |
| 16 | Settings | `/settings` | ✅ 已有 | matedroid + CyberUI |
| 17 | About | `/about` | ✅ 已有 | — |

> ★ = 本轮新发现的缺口页面，源于对 TeslaMate Grafana 20+ 内置面板的逐项对照
> 10→12 页 → **16 页**，尽可能多展示 Tesla 数据

---

### 3.1 Dashboard `/` ✅

**展示内容表**：
| 区域 | 数据项 | 数据来源 | 社区对照 |
|---|---|---|---|
| 顶栏 | 车辆名 + 固件版本 | GET /cars + /status (car_version) | matedroid 顶部同名 + 版本号 |
| 车图区 | 2D 车辆图（按车色+轮毂着色） | Car.car_exterior | matedroid 2D 图 + CyberUI 3D 图 |
| 状态徽章 | online/driving/charging/asleep/offline + since 时间 | GET /status | matedroid 状态徽章 |
| 核心卡片 | 电量% + 进度条 + 续航 km | GET /status | matedroid + modern-dashboard |
| 核心卡片 | 里程表读数 (odometer) | GET /status | matedroid |
| 核心卡片 | 车锁状态 (locked/unlocked) ★ 新增 | GET /status (locked) | matedroid 显示 lock status |
| 4 格信息 | 位置 (lat/lng 反地理编码) | GET /status | matedroid |
| 4 格信息 | 车内温度 / 车外温度 | GET /status | matedroid |
| 4 格信息 | 空调状态 (ON/OFF + 设定温度) | GET /status | matedroid |
| 4 格信息 | Sentry 模式 | GET /status | matedroid |
| 胎压卡片 | 四轮胎压 bar | GET /status | matedroid |
| 充电卡片 | 充电功率 kW + 已充 kWh + 剩余时间 + 电压/电流 ★ 新增电压电流 | GET /status + /charges/current | matedroid 充电详情 |
| 趋势图 | 7d 电池%趋势线 | 本地聚合 /drives | modern-dashboard |
| 趋势图 | 7d 能耗趋势线 ★ 新增 | 本地聚合 /drives | modern-dashboard |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 5s 自动轮询 | 自动刷新 status + 充电卡片 | matedroid (API polling, not MQTT) |
| 下拉刷新 | 立即拉取最新 status | matedroid |
| 点击车辆名 ▼ | 弹出车辆切换 Modal（列表选车 + 每车关键数据） | matedroid Car Switcher |
| 点击状态徽章 | 切换高亮 + 显示 since 时间详情 | matedroid |
| hover 卡片 | CSS 放大 + 阴影 | CyberUI |
| 点击充电卡片 | 跳转 Charge Detail | matedroid |

---

### 3.2 Drive List `/drives` ✅

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 日期分组 (Today/Yesterday/日期) | GET /drives | matedroid + CyberUI |
| 起止地址 (反地理编码) | Drive.start_address / end_address | matedroid |
| 距离 km | Drive.distance_km | matedroid |
| 时长 min | Drive.duration_min | matedroid |
| 能耗 kWh | Drive.consumption_kWh | matedroid |
| 效率 Wh/km + 颜色编码 (<150 绿/150-200 黄/>200 红) ★ 新增颜色 | Drive.efficiency | matedroid 效率颜色 |
| 起止电量% | Drive.start_battery_level / end_battery_level ★ 新增 | matedroid |
| 室外温度 | Drive.outside_temp_avg ★ 新增 | matedroid |
| 效率趋势图 (顶部) | 月度效率折线 ★ 新增 | CyberUI 效率趋势 |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 上拉加载更多 | 分页 20 条 | matedroid |
| 顶部筛选 (距离: All/<20km/20-50km/>50km) ★ 新增 | 按距离分类 | matedroid (commute/day trip/road trip) |
| 点击某条 | 进入 Drive Detail | matedroid |

---

### 3.3 Drive Detail `/drives/:id` 🛠

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 日期 + 起止时间 + 总时长 | Drive | matedroid |
| 起止地址 | Drive | matedroid |
| 完整轨迹地图 (Leaflet Polyline + 起止标记) | Drive.positions[] | matedroid + CyberUI |
| 距离 / 平均速度 / 最高速度 ★ | positions 聚合 | matedroid |
| 能耗 / 效率 / 室外温度 | Drive | matedroid |
| 图表标签切换 ★ 新增全部5条曲线： | | |
| · 速度曲线 (km/h vs time) | positions.speed | matedroid |
| · 功率曲线 (kW vs time) | positions.power | matedroid |
| · 海拔曲线 (m vs time) | positions.elevation | matedroid |
| · 温度曲线 (内/外 °C vs time) ★ 新增 | positions.inside_temp | TeslaMate grafana trip.json |
| · 胎压曲线 (4线 bar vs time) ★ 新增 | positions.tire_pressure | TeslaMate grafana trip.json |
| 电量变化 | start_battery_level → end_battery_level ★ 新增 | matedroid |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 地图缩放/拖拽 | Leaflet 标准交互 | matedroid |
| 图表标签切换 | Recharts 5 标签 Tab | matedroid |
| 图表缩放 (Brush) | Recharts Brush 选段时间 | CyberUI |
| 图表 hover Tooltip | 显示精确数值 | CyberUI |
| 地图 hover 轨迹点 | 显示该点 speed + time ★ 新增 | matedroid |

---

### 3.4 Charge List `/charges` ✅

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 月度充电柱状图 (kWh + 次数) | GET /charges 聚合 | CyberUI |
| 充电类型标识 (AC 🔌 / DC ⚡ + 边框色) | Charge.charge_type | matedroid |
| 充电地址 | Charge.address | matedroid |
| 充入电量 kWh | Charge.charge_energy_added | matedroid |
| 费用 (¥/＄) | Charge.cost | matedroid |
| 起止电量% | Charge.start/end_battery_level | matedroid |
| 充电时长 ★ 新增 | end_date - start_date | matedroid |
| 充电功率 ★ 新增 | charger_power | matedroid |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 上拉加载更多 | 分页 20 条 | matedroid |
| AC/DC/All 筛选 | 按钮组切换 | matedroid |
| 点击某条 | 进入 Charge Detail | matedroid |

---

### 3.5 Charge Detail `/charges/:id` 🛠

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 日期 + 时长 + 地址 | Charge | matedroid |
| 充电位置地图 | Charge position | matedroid |
| 充入能量 / 费用 / 起止电量 / 续航增加 | Charge | matedroid |
| 图表标签切换 ★ 全部3条曲线： | | |
| · 功率曲线 (kW vs time) | charges[].charger_power | matedroid |
| · 电压曲线 (V vs time) ★ 新增 | charges[].charger_voltage | matedroid |
| · 温度曲线 (outside_temp vs time) ★ 新增 | charges[].outside_temp | matedroid |
| 充电效率 (AC→DC 损耗%) ★ 新增 | charge_energy_added / charge_energy_used | TeslaMate grafana |
| 充电桩信息 (品牌/类型) ★ 新增 | fast_charger_brand / fast_charger_type | matedroid |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 图表标签切换 | Recharts 3 标签 Tab | matedroid |
| 图表缩放 (Brush) | Recharts Brush 选时 | CyberUI |
| 图表 hover Tooltip | 精确数值 | CyberUI |

---

### 3.6 Battery Health `/battery` ✅

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 健康度% + 大数字 + 环形进度 | GET /battery-health | matedroid |
| 状态评级 (Excellent/Good/Fair/Poor) | 基于 degradation% | matedroid |
| 总里程 | BatteryHealth.mileage_km | matedroid |
| 出厂容量 vs 当前容量 (kWh) | BatteryHealth | matedroid |
| 出厂续航 vs 当前续航 (km) ★ 新增 | BatteryHealth | matedroid |
| 衰减趋势图 (容量 vs 时间/里程) | BatteryHealth.history[] | matedroid |
| 续航损失 (km) + 百分比 ★ 新增 | BatteryHealth.range_loss_percent | TeslaMate grafana |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| 趋势图 hover Tooltip | 日期 + 容量 + 里程 | matedroid |
| 趋势图缩放 | Recharts Brush | matedroid |

---

### 3.7 Statistics Drill `/statistics` 🛠

**展示内容表**（借鉴 matedroid MileageScreen 钻取模式）：
| 层级 | 数据项 | 数据来源 |
|---|---|---|
| Year View | 12 张月度卡片 (月里程/能耗/充电次数/费用) | GET /drives + /charges 聚合 |
| Year View | 月度里程柱状图 | 聚合 |
| Month → Day | 30 天里程热力图 + 每日汇总 | 聚合 |
| Day → Drive | 当日行程列表 | 聚合 |
| Drive → Detail | 跳转 Drive Detail | GET /drives/:id |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| Year→Month 点击 | 下钻到月度视图 | matedroid MileageScreen |
| Month→Day 点击 | 下钻到日视图 | matedroid |
| Day→Drive 点击 | 跳转 Drive Detail | matedroid |
| 面包屑导航 | 逐级返回 | matedroid |

---

### 3.8 Drive Heatmap `/heatmap` 🛠

**展示内容表**（借鉴 modern-dashboard GitHub 风格热力图）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 15 天 × 24 小时网格 | GET /drives 聚合 | modern-dashboard |
| 颜色映射 (浅灰→浅蓝→深蓝) | 驾驶距离 | modern-dashboard |
| 图例 + 日期标签 | — | modern-dashboard |

**交互行为表**：
| 交互 | 行为 | 社区对照 |
|---|---|---|
| hover 单元格 | Tooltip "14:00-15:00 · 23.5km · 45min" | modern-dashboard |
| click 单元格 | 跳转当日 Drive List | modern-dashboard |

---

### 3.9 Top Destinations `/destinations` 🛠

**展示内容表**：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 全屏 Leaflet 地图 + Cluster markers | GET /drives (聚合位置) | modern-dashboard |
| Marker 大小 = 访问次数 | 聚合 | modern-dashboard |
| Marker 颜色 = 总驾驶距离 | 聚合 | — |
| 右侧 Top 20 表格 | 地址 + 次数 + 总距离 + 平均效率 ★ 新增效率 | — |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| hover marker | popup "15 次 · 总 234 km · 平均 142 Wh/km" |
| click marker | 缩放地图 + 高亮 |
| 表格排序 | 点击列头切换排序 |

---

### 3.10 Efficiency Curve `/efficiency` 🛠

**展示内容表**（借鉴 TeslaMate grafana efficiency.json SQL）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 散点图 (速度 vs 效率) | GET /drives (每条=1点) | TeslaMate efficiency.json |
| 回归拟合线 | 本地计算 | — |
| 温度颜色编码 (<0°C蓝 / 0-20绿 / >20红) | Drive.outside_temp_avg | — |
| 图例 + 速度区间统计表 | 0-30/30-60/60-90/90-120/120+ | — |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| hover 散点 | Tooltip "速度 85km/h · 效率 142Wh/km · 28°C" |
| 框选散点区域 | 高亮选中 + 显示该区域统计 |
| 温度图例切换 | 筛选特定温度范围 |

---

### 3.11 Vampire Drain `/vampire` ★ 新增

**展示内容表**（借鉴 TeslaMate grafana vampire-drain.json）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 停车掉电汇总 (总损失 kWh / km / %) | GET /drives 聚合 (停车期间) | TeslaMate vamp-drain.json |
| Vampire Drain 趋势图 (每日损失 vs 时间) | 聚合 | TeslaMate vamp-drain.json |
| 长停 vs 短停对比 ★ | >12h / 1-12h / <1h 分组 | TeslaMate |
| 温度 vs Drain 关联图 ★ | 室外温度 vs 掉电速率 | TeslaMate |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| 趋势图 hover | 日期 + 损失 kWh + 温度 |
| 时间范围切换 | 30d / 90d / 1y |
| 温度关联图 hover | 温度 + 损失 |

---

### 3.12 Timeline `/timeline` ★ 新增

**展示内容表**（借鉴 TeslaMate grafana timeline.json）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 时间轴 (车辆状态变化) | GET /drives + /charges 拼接 | TeslaMate timeline.json |
| 颜色编码 (drive=蓝 / charge=橙 / park=灰 / sleep=深灰) | Drive + Charge 时间段 | TeslaMate |
| 每个时段 Tooltip | 类型 + 时长 + 距离/能量 | TeslaMate |
| 日/周/月切换 | 聚合粒度 | — |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| hover 时段 | Tooltip "14:30-15:45 Drive · 23.5km · 142Wh/km" |
| 时间范围切换 | 今日 / 本周 / 本月 |
| click 时段 | Jump to Drive/Charge Detail |

---

### 3.13 Firmware Updates `/updates` ★ 新增

**展示内容表**（借鉴 matedroid + PRD F-011）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 版本列表 (version + 日期 + 安装时长) | GET /updates | matedroid |
| 最长运行版本 Badge ★ | 聚合 | matedroid |
| 更新频率图 (月度) ★ | 聚合 | matedroid |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| click 版本 | 展开详情 (安装时间 + 运行天数) |
| 频率图 hover | 月份 + 更新次数 |

---

### 3.14 Projected Range `/range` ★ 新增

**展示内容表**（借鉴 TeslaMate grafana projected-range.json）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 预估续航 vs 实际续航对比 | GET /drives (end_range vs start_range) | TeslaMate proj-range |
| 续航衰减趋势 (与时间/里程) | 聚合 | TeslaMate |
| 不同温度下的续航比较 ★ | 按温度段分组 | — |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| 趋势图 hover | 日期 + 预估 + 实际 + 差值 |
| 温度段切换 | 筛选 -10-0°C / 0-10°C / 10-20°C / 20-30°C / 30+°C |

---

### 3.15 Charging Cost `/cost` ★ 新增

**展示内容表**（借鉴 TeslaMate grafana charging-stats.json + teslamate-chinese-dashboards）：
| 数据项 | 数据来源 | 社区对照 |
|---|---|---|
| 月度充电成本汇总 | GET /charges 聚合 | TeslaMate charg-stats |
| 成本构成 (家庭 AC vs 超充 DC) | 按 charge_type 分组 | TeslaMate |
| 分时电价节省计算 ★ 中国版独有 | 峰平谷电价对比 | chinese-dashboards |
| 充电桩性价比榜 (¥/度排序) ★ | 聚合 | chinese-dashboards |
| 免费超充统计 (若 free_supercharging=true) ★ | Car.car_settings | — |

**交互行为表**：
| 交互 | 行为 |
|---|---|
| 月度柱状图 click | 展开该月充电明细 |
| 分时电价开关 | 切换峰平谷 vs 统一电价 |
| 充电桩榜单排序 | price/kWh 升序 |

---

### 3.16 Settings `/settings` ✅ + About `/about` ✅

(保持现有实现，补充 Mock 模式确认弹窗)

## 4. Color Scheme (Apple-Like)

借鉴 `TeslamateCyberUI` 的 Tailwind 配置：

| Color | Hex (Light) | Hex (Dark) |
|---|---|---|
| Background | #F5F5F7 (gray-100) | #1A1A2E (slate-950) |
| Card | #FFFFFF | #16213E |
| Primary | #1E88E5 (blue-600) | #42A5F5 |
| Success | #43A047 | #66BB6A |
| Warning | #FB8C00 | #FFA726 |

## 5. Deployment

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```yaml
# docker-compose (add to existing TeslaMate stack)
services:
  matelink-web:
    image: matelink/web:latest
    restart: always
    ports:
      - "3000:80"
    environment:
      - TESLAMATE_API_URL=https://teslamate.example.com/api/v1
      - API_TOKEN=${MATELINK_API_TOKEN}
```
