# Brainstorm Summary

- Change: glm-review-fix-critical
- Date: 2026-06-24

## Confirmed Technical Approach

10 项技术决策已确认。D-7 地图引擎选择修正为高德 AMap（用户指定）。

## Key Trade-offs and Risks

- 高德 SDK 需要 API key + 企业认证，自托管场景需用户自行申请
- AppSettings 合并涉及 SettingsScreen.kt 和 SettingsDataStore.kt 重构，回归风险中等
- GCJ-02 移植已在 iOS 验证，Android 移植风险低

## Testing Strategy

编译验证 + 手动冒烟测试（无自动化测试基础设施）

## Spec Patches

None — 本次为 bug 修复，不改变 spec 行为约定
