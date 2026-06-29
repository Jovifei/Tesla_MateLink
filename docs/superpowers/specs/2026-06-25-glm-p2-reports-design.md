---
comet_change: glm-p2-reports
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-25-glm-p2-reports
status: final
---

# glm-p2-reports — 年度报告 + 数据导出

## Context / Goals

- T-101~T-104: 年度驾驶报告（统计汇总、趋势图表、习惯分析、PDF 生成）
- T-201~T-203: 数据导出（CSV/JSON/Excel）

## Decisions

- D-1: 年度报告用 Swift Charts / MPAndroidChart
- D-2: PDF 生成用 UIKit (iOS) / Android Print
- D-3: CSV/JSON 用原生 API，Excel 用 xlsxwriter/minipoi
- D-4: 导出范围默认最近 1 年，可自定义

## Tasks (7)

- T-101: 年度统计汇总
- T-102: 月度趋势图表
- T-103: 驾驶习惯分析
- T-104: PDF 报告生成
- T-201: 导出格式选择 UI
- T-202: 导出范围选择
- T-203: 导出文件分享
