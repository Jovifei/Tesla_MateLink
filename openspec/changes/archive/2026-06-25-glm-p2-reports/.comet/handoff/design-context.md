# Comet Design Handoff

- Change: glm-p2-reports
- Phase: design
- Mode: compact
- Context hash: 98d6ac3f7feeba31f74040d9f0ba9d57804d654d8f5786cf7354cc0c1a136095

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p2-reports/proposal.md

- Source: openspec/changes/glm-p2-reports/proposal.md
- Lines: 1-25
- SHA256: 07eb86eb4c6c8f43279b2bc033f52776f392bfd57b4645e1ba685c10f49adeba

```md
## Why

用户需要查看年度驾驶总结和导出数据。年度报告提供年度驾驶统计、能耗趋势、充电习惯分析。数据导出让用户备份或分析原始数据。

## What Changes

### 年度报告
- **T-101**: 年度统计汇总（总里程、能耗、费用、充电次数）
- **T-102**: 月度趋势图表（折线图 + 柱状图）
- **T-103**: 驾驶习惯分析（高效驾驶占比、平均速度、常用路线）
- **T-104**: PDF 报告生成

### 数据导出
- **T-201**: 导出格式选择（CSV/JSON/Excel）
- **T-202**: 导出范围选择（日期范围、数据类型）
- **T-203**: 导出文件分享（ShareSheet）

## Capabilities

- `annual-report`: 年度驾驶报告
- `data-export`: 数据导出功能

## Non-Goals
- 云同步/备份
- 实时数据流
- 第三方分析平台集成```

## openspec/changes/glm-p2-reports/design.md

- Source: openspec/changes/glm-p2-reports/design.md
- Lines: 1-19
- SHA256: 74be9b36f2bc2ce19dfc89bf9a0fdfa8943ed07bfbd64ae39962ce897dd1ed7e

```md
## Context / Goals

P2 数据分析功能。年度报告 + 数据导出。

## Decisions

- D-1: 年度报告用 Swift Charts / MPAndroidChart 绘制图表
- D-2: PDF 生成用 UIKit (iOS) / Android Print (Android)
- D-3: CSV/JSON 导出用原生 API，Excel 用第三方库（xlsxwriter/minipoi）
- D-4: 导出范围默认最近 1 年，可自定义

## Tasks (7)

- T-101: 年度统计汇总
- T-102: 月度趋势图表
- T-103: 驾驶习惯分析
- T-104: PDF 报告生成
- T-201: 导出格式选择 UI
- T-202: 导出范围选择
- T-203: 导出文件分享```

## openspec/changes/glm-p2-reports/tasks.md

- Source: openspec/changes/glm-p2-reports/tasks.md
- Lines: 1-11
- SHA256: f2df354537ab686e810e746ef9ef6d2685db9ad0a2c2e2f8c24711a9afa11fc2

```md
## 1. 年度报告

- [ ] T-101 年度统计汇总（总里程、能耗、费用、充电次数）
- [ ] T-102 月度趋势图表（折线图 + 柱状图）
- [ ] T-103 驾驶习惯分析（高效驾驶占比、平均速度、常用路线）
- [ ] T-104 PDF 报告生成

## 2. 数据导出

- [ ] T-201 导出格式选择 UI（CSV/JSON/Excel）
- [ ] T-202 导出范围选择（日期范围、数据类型）
- [ ] T-203 导出文件分享（ShareSheet）```

