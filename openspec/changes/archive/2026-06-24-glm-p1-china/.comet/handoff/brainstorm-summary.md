# Brainstorm Summary

- Change: glm-p1-china
- Date: 2026-06-24

## Confirmed Technical Approach

4 designer 团队（D1 Android、D2 iOS、D3 YAGNI、D4 Parity）独立设计后裁决得 7 项决策：

- D-1: iOS 不引入 AMap SDK，继续用 MapKit + GCJ02Converter（iOS 17 Apple Maps 中国大陆已用高德瓦片）
- D-2: Android 端口 `app_mimo/.../AmapComposeView.kt` 现成实现，不重写
- D-3: TOU 配置 UI 砍到 v2，v1 用 hardcoded 默认费率（峰 ¥1.0 / 平 ¥0.7 / 谷 ¥0.3）
- D-4: GCJ-02 转换只在显示层，存储用 WGS-84
- D-5: HK/Macau 跳过 GCJ-02 偏移（这两地用 WGS-84）
- D-6: 拒绝 MapDisplay 抽象接口（YAGNI）
- D-7: 拒绝 build flavor 拆分（单一 flavor + runtime locale check）

## Key Trade-offs and Risks

- AMap Android API key 是阻塞项（Jovi 后续提供）；代码先做完，缺 key 时显示 "Map unavailable" 占位
- AMap SDK 增加 ~8MB APK，可接受
- HK/Macau bounding box 简化为矩形（v2 可改为精确多边形）
- iOS MapKit + GCJ02 已验证，无额外风险

## Testing Strategy

- 手动冒烟测试（无自动化测试基础设施）
- T-301 GCJ-02 输出一致性验证：选 3 个标定点（北京/上海/HK/海外），iOS Android 输出对照，误差 <1m
- AMap 渲染验证：缺 key 时占位、有 key 时 marker 在正确位置

## Spec Patches

无 — 3 个 spec 在 open 阶段已完整定义验收场景，无需补充。
