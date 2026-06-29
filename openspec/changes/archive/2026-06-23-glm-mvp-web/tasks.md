# Tasks: MateLink Web Dashboard

## Phase: Setup (Week 3)

- [x] **TW-001** — 初始化 Vite + React + TS 工程
  - `npm create vite@latest web_matelink -- --template react-ts`
  - 配置 Tailwind CSS + Recharts + React Router + TanStack Query
  - 配置 ESLint + Prettier + `mock_data.json` 复制
  - 借鉴：teslamate-modern-dashboard package.json
  - 验证：`npm run dev` → Mock 模式首页显示

- [x] **TW-002** — API Client + Mock Client (Web)
  - `src/api/client.ts`: fetch 封装（Bearer Token、错误模型、超时 10s）
  - `src/api/teslamate.ts`: 16 个端点 TypeScript 封装
  - `src/api/mock.ts`: Mock 实现（读 mock_data.json）
  - `src/api/types.ts`: 全部 API 类型
  - 借鉴：glm-mvp-foundation Android API Client 设计
  - 验证：Mock 模式下 getCars() 返回 2 辆车

- [x] **TW-003** — Layout + 侧边栏 + 路由
  - Collapsible 侧边栏 (12 菜单项)
  - React Router 路由配置
  - 响应式（侧边栏可折叠，适配 1024px+ 宽屏）
  - 借鉴：TeslamateCyberUI 侧边栏布局
  - 验证：点击菜单 → 正确路由

## Phase: Core Pages (Week 4)

- [x] **TW-004** — Dashboard 页
  - 4 张 StatCard (电量/续航/温度/胎压)
  - Recharts 7d 趋势图 (电量/温度)
  - 借鉴：teslamate-modern-dashboard Dashboard
  - 验证：Mock 数据正确加载

- [x] **TW-005** — Drives 列表 + Drive Detail
  - 列表：日期、距离、能耗、效率
  - 详情：Leaflet 全屏轨迹地图 + 速度/功率/海拔曲线 (Recharts)
  - 借鉴：TeslamateCyberUI drive-list / drive-detail
  - 验证：点击某条 → 全屏地图 + 多条曲线

- [x] **TW-006** — Charges 列表 + Charge Detail
  - 列表：日期、能量、成本、AC/DC
  - 详情：充电功率曲线 (Recharts Area chart)
  - 借鉴：TeslamateCyberUI charge-list / charge-detail
  - 验证：Curve 可缩放 (Recharts Brush)

- [x] **TW-007** — Battery Health 页
  - 当前健康度大数字 + Gauge 图
  - 衰减趋势折线图 (里程轴)
  - 借鉴：matedroid BatteryHealth
  - 验证：衰减曲线正确 (容量随时间下降)

## Phase: Advanced Pages (Week 5) — 10 页

- [x] **TW-008** — Statistics 钻取 (Year→Month→Day→Drive)
  - 4 级面包屑导航 + 每级数据聚合
  - 借鉴：matedroid MileageScreen
  - 验证：完整 Year→Month→Day→Drive 流程

- [x] **TW-009** — Drive Heatmap 页
  - GitHub 风格 15d×24h 热力图
  - hover tooltip + click → 跳转当日 Drive List
  - 借鉴：teslamate-modern-dashboard
  - 验证：hover tooltip + click 跳转

- [x] **TW-010** — Top Destinations 页
  - Leaflet Cluster Map + 右侧 Top20 表格
  - Marker 大小/颜色编码 + 表格排序
  - 验证：地图渲染 + popup + 排序

- [x] **TW-011** — Efficiency Curve 页
  - 散点回归 + 温度颜色编码 + 区域选择
  - 借鉴：teslamate/grafana/dashboards/efficiency.json
  - 验证：散点 hover + 框选

- [x] **TW-011a** — Drive Detail 增强 ★
  - 5 条曲线标签切换 (速度/功率/海拔/温度/胎压)
  - 电量变化显示 + 最高速度
  - 地图 hover 轨迹点 tooltip
  - 验证：5 标签切换 + Brush 缩放

- [x] **TW-011b** — Charge Detail 增强 ★
  - 3 条曲线 (功率/电压/温度) + Brush
  - 充电效率 (AC→DC 损耗%)
  - 充电桩品牌/类型
  - 验证：3 标签 + Brush

- [x] **TW-011c** — Vampire Drain 页 ★ 新增
  - 停车掉电汇总 + 趋势图
  - 长停/短停对比 + 温度 vs Drain 关联图
  - 借鉴：TeslaMate grafana vampire-drain.json
  - 验证：趋势图 + 温度关联

- [x] **TW-011d** — Timeline 页 ★ 新增
  - 车辆状态时间轴 (drive/charge/park/sleep 颜色编码)
  - 日/周/月切换 + tooltip + click 跳转
  - 借鉴：TeslaMate grafana timeline.json
  - 验证：hover + click 跳转

- [x] **TW-011e** — Firmware Updates 页 ★ 新增
  - 版本列表 + 更新频率图 + 最长运行版本 Badge
  - 借鉴：matedroid + PRD F-011
  - 验证：列表 + 图表

- [x] **TW-011f** — Projected Range 页 ★ 新增
  - 预估 vs 实际续航对比 + 温度段分组
  - 借鉴：TeslaMate grafana projected-range.json
  - 验证：趋势图 + 温度段切换

- [x] **TW-011g** — Charging Cost 页 ★ 新增
  - 月度成本汇总 + 家庭AC vs 超充DC 构成
  - 分时电价计算 + 充电桩性价比榜
  - 借鉴：chinese-dashboards + TeslaMate charg-stats
  - 验证：柱状图 + 榜单

## Phase: Settings + Deploy (Week 6)

- [x] **TW-012** — Settings 页 + Mock 切换
  - TeslaMateApi URL + Token 配置
  - Mock Mode 开关（不重启生效）
  - 浅色/深色主题切换
  - 验证：修改 URL → 刷新数据 → 使用新 URL

- [x] **TW-013** — Docker + CI/CD
  - Dockerfile (nginx + gzip)
  - docker-compose.yml (add to TeslaMate stack)
  - GitHub Actions: lint + build (PR)
  - 验证：`docker build -t matelink/web . && docker run -p 3000:80`

---

## Dependencies

- **gm-mvp-foundation** (共享 mock_data.json + API Client 设计)
- 不依赖其他 mobile change (#2-5)

## Estimated Effort

| 阶段 | 工时 |
|---|---|
| Setup (TW-001 ~ TW-003) | 3d |
| Core Pages (TW-004 ~ TW-007) | 5d |
| Web-Only Pages (TW-008 ~ TW-011) | 5d |
| Settings + Deploy (TW-012 ~ TW-013) | 2d |
| **总计** | **15d (3 周)** |
