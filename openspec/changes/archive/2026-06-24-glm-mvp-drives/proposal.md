# Proposal: MateLink iOS Drives

## Summary
行程列表 + 详情页 (F-006/007)

## Goals
- DriveListView: 日期分组 + 效率颜色编码
- DriveDetailView: 5 曲线 (速度/功率/海拔/温度/胎压) + 电量变化 + Swift Charts Brush
- 轨迹抽稀 (RouteSimplifier)

## Scope
`Features/Drives/DriveDetailView.swift`
`Core/Utils/RouteSimplifier.swift`

## Dependencies
- glm-mvp-foundation

## Reference
- 直抄 matedroid `DriveDetailScreen.kt`
- 参考 Web `DriveDetail.tsx`
