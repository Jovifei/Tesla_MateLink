## ADDED Requirements

### Requirement: Sentry 通知
iOS SHALL 在 sentry_armed → alert 事件触发时发送本地推送通知。

#### Scenario: Sentry 告警
- **WHEN** 后台轮询检测到 sentryMode 状态从 armed 变为 alert
- **THEN** 系统发送标题为 "Sentry Alert" 的本地通知

### Requirement: 充电完成通知
iOS SHALL 在充电从 charging → complete 状态变化时发送本地通知。

#### Scenario: 充电完成
- **WHEN** 后台轮询检测到 chargingState 从 "Charging" 变为 "Complete"
- **THEN** 系统发送含充电量和费用的本地通知

### Requirement: TPMS 胎压低告警
iOS SHALL 在任何一个轮胎压力低于阈值时发送通知。

#### Scenario: 胎压低
- **WHEN** 后台轮询检测到任意 tpmsPressure 值 < 2.0 bar
- **THEN** 系统发送含具体轮胎和压力值的告警通知

### Requirement: 后台轮询
iOS SHALL 使用 BackgroundTasks 框架定期轮询 TeslaMateApi 状态变化。

#### Scenario: 后台刷新
- **WHEN** 应用在后台且 BGAppRefreshTask 被调度
- **THEN** 系统调用 notificationManager.checkForUpdates() 检测状态变化