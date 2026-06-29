---
change: mimo-mvp
design-doc: docs/superpowers/specs/2026-06-23-mimo-mvp-design.md
base-ref: a8425c4774279be108f831fe2dea6c576a3eddca
---

# MateLink MVP 实施计划 — Web 原型优先

## 总体策略

```
Phase 0: Shared Base (T-000~T-003)     → Mock 数据 + API 类型
Phase 0: Web 原型 (TW-001~TW-802)      → 18 页完整交互 → Jovi 确认
Phase 1: iOS App (T-101~T-605)         → 原生实现
Phase 2: Android App (T-701~T-708)     → 原生实现
Phase 3: Release (T-801~T-805)         → 上架
```

**当前聚焦**：Phase 0（Shared Base + Web 原型）

---

## 执行顺序

### Step 1: Shared Base
1. T-000: 创建目录结构
2. T-001: 创建 mock_data.json（2 辆车 + 30 天全字段数据）
3. T-002: 定义 api-types.ts
4. T-003: API 端点文档

### Step 2: Web 项目初始化
5. TW-001: Vite + React + TS + Tailwind 初始化
6. TW-002: 路由 + Zustand 状态管理
7. TW-003: Mock 数据集成 + API Client
8. TW-004: Layout（侧边栏 + 响应式）

### Step 3: Dashboard（核心页面）
9. TW-101~TW-110: Dashboard 全部交互

### Step 4: 驾驶模块
10. TW-201~TW-207: 驾驶列表 + 详情 + 天气 + 时间线

### Step 5: 充电模块
11. TW-301~TW-305: 充电列表 + 详情 + Current Charge

### Step 6: 电池 + 更新
12. TW-401~TW-402: 电池健康 + 软件版本

### Step 7: 统计分析
13. TW-501~TW-506: Mileage + Stats + Heatmap + Top Dest + Efficiency + Countries

### Step 8: 监控 + 高级
14. TW-601~TW-602: Sentry Events + Trips

### Step 9: 设置 + 配置
15. TW-701~TW-705: 设置 + 首次配置 + 分时电价

### Step 10: 部署 + 确认
16. TW-801: Docker 配置
17. TW-802: Jovi 确认所有交互

---

## 技术栈确认

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18+ | UI 框架 |
| TypeScript | 5+ | 类型安全 |
| Vite | 5+ | 构建工具 |
| Tailwind CSS | 3+ | 样式 |
| Recharts | 2+ | 图表 |
| Leaflet | 1.9+ | 地图 |
| React Router | 6+ | 路由 |
| Zustand | 4+ | 状态管理 |

---

## 每个页面的验收标准

每个页面必须：
1. 使用 Mock 数据正确渲染
2. 所有交互可操作（点击、hover、缩放、筛选）
3. 响应式布局（桌面 + 移动端）
4. 亮色/暗色主题切换
5. Loading / Empty / Error 三种状态
