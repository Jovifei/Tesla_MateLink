# Brainstorm Summary

- Change: stitch-core-navigation
- Date: 2026-07-05

## 确认的技术方案

- **Theme**: 保留 MaterialTheme 骨架，替换 lightColorScheme 颜色为 Stitch 28 色调色板，全局 shadowElevation=0
- **Card**: StitchCard 用 Surface 实现（非 Material3 Card），1px #E5E5E5 边框 + 8px 圆角 + 24px padding + 白底 + 零阴影
- **Tokens**: shared/design-tokens.json 单一源（28色 + 7字体级 + 5间距 + 组件 spec）
- **Fonts**: Inter 用于 UI 文字，JetBrains Mono Regular+Medium 用于数值（打包到 res/font/ 和 iOS bundle）
- **Navigation**: 保留 Jetpack Navigation + SwiftUI TabView，仅替换视觉
- **Data Layer**: DashboardViewModel/DriveViewModel/ChargeViewModel 不动，仅 UI 层重写
- **iOS**: 对照 Android 逐页实现，共享 design-tokens.json

## 关键取舍与风险

- MaterialTheme 颜色语义与 Stitch 不完全对应 → 映射表处理，primaryContainer 等不用的颜色设为 surface
- JetBrains Mono 中文字符回退 → 中文标签用 Inter，纯数值用 JetBrains Mono
- iOS 无 Mac 编译 → Android 先完成作参考，iOS 按 spec 对照实现

## 测试策略

- Compose screenshot test 对比 Stitch HTML
- Theme 单元测试验证颜色值
- ViewModel 现有测试保留（数据层不变）

## Spec Patch

无
