---
change: mimo-mvp
design-doc: docs/superpowers/specs/2026-06-23-mimo-mvp-design.md
base-ref: a8425c4774279be108f831fe2dea6c576a3eddca
archived-with: 2026-06-24-mimo-mvp
---

# MateLink MVP 实施计划 — Android 优先

## 调整原因

Jovi 暂无 Mac，无法进行 iOS 原生开发。改为 **Android 先行**。

## 实施顺序

```
Phase 0: Web 原型 ← 已完成 ✅ (http://localhost:5176)
Phase 1: Android App ← 当前执行
  │  T-701~T-724 (24 任务)
  │  参考 matedroid (★67, Kotlin, 已上架 Play Store)
  ▼
Phase 2: iOS App ← 等有 Mac 后执行
Phase 3: Release
```

## Android 详细步骤

### Step 1: 项目初始化 (T-701~T-702)
1. 创建 Android 项目（Kotlin + Jetpack Compose + Material 3）
2. 配置 Gradle 依赖

### Step 2: 数据层 (T-703~T-706)
3. API Client (Retrofit + OkHttp)
4. 数据模型
5. Token 安全存储
6. Room 离线缓存

### Step 3: 基础 UI (T-707~T-710)
7. 底部导航
8. 主题系统
9. 首次配置
10. 设置页

### Step 4: 核心功能 (T-711~T-714)
11. Dashboard
12. Charges + Current Charge
13. Drives + Detail
14. Battery Health

### Step 5: 高级功能 (T-715~T-721)
15. Mileage 钻取
16. Stats for Nerds
17. Sentry Events
18. Countries Visited
19. Software Versions
20. Trips
21. 多车切换

### Step 6: 本地化 (T-722~T-723)
22. 中国本地化
23. Mock 模式

### Step 7: 测试 (T-724)
24. 真机测试

## 参考仓库

- `docs/git_ref/mimo/matedroid/` — 主要参考，完整的 Kotlin + Compose 项目
- `docs/git_ref/mimo/teslamateapi/` — API 端点参考
