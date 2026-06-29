# android-stats-drilldown Specification

## Purpose
TBD - created by archiving change glm-review-fix-critical. Update Purpose after archive.
## Requirements
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

