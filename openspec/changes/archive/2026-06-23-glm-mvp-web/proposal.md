# Proposal: MateLink Web Dashboard

## Summary

基于 React 18 + Vite 的独立 Web 前端仪表盘，复用 TeslaMateApi 的数据，在桌面浏览器提供大屏深度分析体验。可独立于移动 App 部署和运行。

## Why

- 移动端看数据受限于屏幕尺寸，大量 Grafana 仪表盘在手机上体验差
- Web 端可以借大屏展示更丰富的图表、热力图、地图分析，是移动 App 的完美补充
- 提供 Mock 模式 + Docker 部署方案，用户可在 2 分钟内体验

## Goals

- 实现 12 个页面（Dashboard / Drives / Drive Detail / Charges / Charge Detail / Battery Health / Statistics / Heatmap / Top Destinations / Efficiency Curve / Settings / About）
- 直连 TeslaMateApi（与移动端共享 API 设计）
- 内置 Mock 模式
- Docker 一键部署（nginx + 静态资源）

## Non-Goals

- SSR / 服务端渲染（纯静态即可）
- 用户账号体系
- 移动端响应式（主要是桌面大屏）
- 替代 TeslaMate 的 Grafana（尊重上游）

## Scope

| 项 | 值 |
|---|---|
| 工程目录 | `app_glm/web/` 或独立 `E:\project\tesla_master\web_matelink/` |
| 依赖 | glm-mvp-foundation (共享 API 客户端设计 + Mock 数据) |
| 周期 | W3-W6（与 mobile #2-5 可并行） |

## Reference

- 主要借鉴：`teslamate-modern-dashboard` (React + Vite + Recharts + Docker)
- UI 借鉴：`TeslamateCyberUI` (Tailwind 配色)
- 数据借鉴：`teslamate/grafana/dashboards/*.json` (图表 SQL → TypeScript)

## Acceptance

- [ ] `npm run dev` 启动 Mock 模式，12 个页面可访问
- [ ] `docker run` 部署生产版本
- [ ] 所有图表数据来自 TeslaMateApi（非 Mock 模式）
