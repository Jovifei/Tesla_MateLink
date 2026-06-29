# Comet Design Handoff

- Change: glm-p2-multi-instance
- Phase: design
- Mode: compact
- Context hash: 4f250e9b5a01825f8fbb984a908fe9cd388affb7f232c453ba1e5ea786f3a53b

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-p2-multi-instance/proposal.md

- Source: openspec/changes/glm-p2-multi-instance/proposal.md
- Lines: 1-18
- SHA256: 22588b653f9ef4e6f770bf6ebcaf3c14a5765d63f0c77d6ed482d5004138213f

```md
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
- 多用户登录系统```

## openspec/changes/glm-p2-multi-instance/design.md

- Source: openspec/changes/glm-p2-multi-instance/design.md
- Lines: 1-16
- SHA256: 549e3f57bf06cdbbd8a981a0028500acb83ad0d0614032cd65356dd89627a1bc

```md
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
- T-104: Widget/通知按实例过滤```

## openspec/changes/glm-p2-multi-instance/tasks.md

- Source: openspec/changes/glm-p2-multi-instance/tasks.md
- Lines: 1-5
- SHA256: 702263631ebcd4ffebb0840fdc4d7a18d8cfeccc2ac72b13a364c6666c4aa96a

```md
## 1. 多实例切换

- [ ] T-101 多实例数据模型（Instance: id, name, serverUrl, apiToken, carId）
- [ ] T-102 实例切换 UI（设置页实例管理 + 快速切换）
- [ ] T-103 数据隔离（每个实例独立缓存，切换时重新加载）
- [ ] T-104 Widget/通知按当前活跃实例过滤```

