# Brainstorm Summary

- Change: mimo-mvp
- Date: 2026-06-23

## Confirmed Technical Approach

1. **开发顺序**：先 iOS → 后 Android
2. **Web 交互原型先行**：React + Vite + Tailwind，确认交互后再写原生 App
3. **iOS 图表**：Swift Charts（Apple 原生，无第三方依赖）
4. **品牌名**：Tesla_MateLink
5. **视觉风格**：Apple-Like + 车色强调色
6. **API 通信**：HTTP 轮询 5s（非 MQTT）
7. **地图**：高德（中国）+ 系统地图（海外）
8. **车辆图**：2D 颜色匹配（3D 留 v1.2）

## Key Trade-offs and Risks

- 单人双端开发周期长 → 先 iOS 有 t-buddy 参考，可加速
- App Store 审核被拒风险（车辆类严审）→ 明确免责
- Tesla 商标侵权风险 → 不用 Tesla 作主名
- 高德地图 SDK 集成复杂 → 先做海外版，中国本地化后做

## Testing Strategy

- Web 原型：手动测试所有页面交互
- iOS：XCTest 单元测试 + XCUITest UI 测试 + 真机测试
- Android：JUnit 单元测试 + Espresso UI 测试 + 真机测试
- 性能：Instruments (iOS) / Profiler (Android)

## Spec Patches

None - specs are complete.
