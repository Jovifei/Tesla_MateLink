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
- T-203: 导出文件分享