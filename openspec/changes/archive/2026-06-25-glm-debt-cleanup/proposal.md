## Why

4 轮审查发现 5 个 technical debt（非阻塞，但影响用户体验和代码质量）：
1. Widget 读 carImageData 但 Dashboard 从未写入 → 永远显示占位
2. Android StatisticsScreen 100% 硬编码 mock 数据 → 无真实数据
3. Notification T-206 更新 + T-207 电池健康只有壳 → 无触发逻辑
4. StatisticsView O(n²) → 10000 驱动时 ~36 万次比较/渲染
5. Kotlin 屏未本地化硬编码英文 → 非英文用户看到英文

## What Changes

| # | 文件 | 修复 |
|---|---|---|
| 1 | iOS DashboardView.swift | refresh() 写 carImageData 到 AppGroup |
| 2 | Android StatisticsScreen.kt | 接入 CarRepository 替换 mock |
| 3 | iOS NotificationManager.swift | T-206 更新检测 + T-207 电池阈值逻辑 |
| 4 | iOS StatisticsView.swift | aggregateMonth() 结果缓存 |
| 5 | Kotlin 屏 *.kt | 补齐 i18n stringResource 引用 |

## Capabilities

无新增 capability，全部是已有功能的修复/完善。