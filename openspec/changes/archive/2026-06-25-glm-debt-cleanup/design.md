## Context

5 个 technical debt，全部来自审查标记，无架构变更。

## Decisions

- D-1: Dashboard 写 AppGroup 用 JPEG base64 编码（与 Widget 读取格式一致）
- D-2: StatisticsScreen 注入 CarRepository 通过 Hilt ViewModel
- D-3: T-206 用 lastNotifiedVersion 追踪，T-207 用 lastNotifiedSoH 追踪
- D-4: @State var months: [MonthStat] 在 load() 中一次性计算
- D-5: 补齐 strings.xml 键值，composable 中用 stringResource()

## Migration Plan

每 task 独立 commit，失败可 git revert。