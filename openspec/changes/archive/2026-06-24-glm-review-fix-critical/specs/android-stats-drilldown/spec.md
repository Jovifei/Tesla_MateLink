## ADDED Requirements

### Requirement: Statistics 三级下钻导航
Android Statistics SHALL 支持 Year → Month → Day → DriveDetail 四级 NavigationStack 导航。

#### Scenario: 年份网格展示
- **WHEN** 用户进入 Statistics 页面
- **THEN** 展示当前年份的 12 个月网格，每月显示行程次数、总里程

#### Scenario: 点击月份进入日视图
- **WHEN** 用户点击某个月份卡片
- **THEN** 导航到该月的日视图，显示每天行程摘要列表

#### Scenario: 点击日期进入行程详情
- **WHEN** 用户点击某个日期的行程
- **THEN** 导航到 DriveDetailScreen，传入 driveId

#### Scenario: 数据加载
- **WHEN** Statistics 页面初始化
- **THEN** 从 CarRepository 加载当年所有 drives 数据并聚合

## Open Items

> T7 audit 2026-06-24: 以下场景当前未满足，需后续 change 补全。

1. **数据加载场景未满足**: `StatisticsScreen.kt`（app_glm 和 app/ 两个变体）使用 100% 硬编码 mock 数据（`MonthSummary` / `MonthStat` + `remember { listOf(...) }`），无 `CarRepository` 调用。
2. **无 ViewModel / Repository 层**: 项目中不存在 `StatisticsViewModel`，也不存在 `CarRepository`（仅有 `CarDao` / `CarLocalDataSource`）。统计数据的真实数据管道需从零构建。
3. **MonthDetailScreen / DayDetailScreen 已创建**: 文件存在且可编译，但同样使用 mock 数据，未接入 Repository。
4. **需后续工作**: 创建 `StatisticsViewModel` → 注入 `CarDao` → 实现 drives 聚合逻辑 → 替换 StatisticsScreen / MonthDetailScreen / DayDetailScreen 中的硬编码数据。
