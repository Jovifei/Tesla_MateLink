## Why

多 Tesla 车主需要在同一设备上管理多辆车。当前应用只支持单车辆连接。多实例切换让用户在不同 TeslaMate 服务器或不同车辆之间快速切换。

## What Changes

- **T-101**: 多实例数据模型（Instance 配置：URL + Token + 车辆名）
- **T-102**: 实例切换 UI（设置页新增实例管理）
- **T-103**: 数据隔离（每个实例独立缓存）
- **T-104**: 实例间数据不混（Widget/通知按当前实例过滤）

## Capabilities

- `multi-instance`: 多 Tesla 实例切换和数据隔离

## Non-Goals
- 云端实例同步
- 实例间数据合并
- 多用户登录系统