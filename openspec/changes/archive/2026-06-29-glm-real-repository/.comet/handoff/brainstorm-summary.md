# Brainstorm Summary

- Change: glm-real-repository
- Date: 2026-06-26

## Confirmed Technical Approach

代理模式 (DelegatingCarRepository) + Network-First 缓存 + UI 开关。

- DelegatingCarRepository 包裹 @MockImpl 和 @RealImpl，按 SettingsDataStore.useRealDataSource 运行时转发，无需重启 app
- RealCarRepository 实现 CarRepository 接口，对接 TeslaMateApi（16 端点）+ Room DAOs
- Network-First：API 成功写 Room 并返回；失败降级读 Room；CancellationException re-throw
- 默认 Mock（useRealDataSource=false），设置页暴露 Switch 让用户切 Real

## Key Trade-offs and Risks

- 多一层代理：每次调用多一次 Flow 读取（用 StateFlow 缓存设置值规避）
- 无真实后端：无法集成测试，用 MockWebServer 单元测试覆盖
- Response/Entity 字段可能不对齐：T-001 先核对，缺失字段用默认值
- 分页：本轮只取第一页（page=null），后续优化

## Testing Strategy

- MockWebServer 验证 RealCarRepository API 调用 + Room 缓存写入
- 降级测试：API 失败时返回 Room 缓存，不崩溃
- 代理测试：DelegatingCarRepository 按设置正确转发 Mock/Real

## Spec Patches

None — real-data-source 是新 capability，acceptance scenarios 已在 proposal.md 中定义，无需回写 delta spec。
