## Context / Goals

多实例切换，让用户管理多辆 Tesla。

## Decisions

- D-1: Instance 数据模型：id, name, serverUrl, apiToken, carId
- D-2: iOS 用 AppGroup UserDefaults 存实例列表，Android 用 DataStore
- D-3: 切换实例时重新加载所有数据（不跨实例缓存）
- D-4: Widget/通知使用当前活跃实例

## Tasks (4)

- T-101: 多实例数据模型
- T-102: 实例切换 UI
- T-103: 数据隔离
- T-104: Widget/通知按实例过滤