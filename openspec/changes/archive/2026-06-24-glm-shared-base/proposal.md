# Proposal: MateLink Shared Base (Web-First 前置)

## Summary

创建 Web 和 Mobile 共用的最小共享层：Mock 数据 + API 类型定义。Web 仪表盘可以基于此立即开始开发。不含 Android/iOS 工程。

## Why

Jovi 决策：Web 交互原型先行，确认 UX 后再启动 App。Web 不需要 Android/iOS 工程，只需要 Mock 数据 + API 类型就能开始。

## Goals

- 共享 `mock_data.json`（2 辆车 + 30 天数据）
- API 类型定义（TypeScript, Kotlin data class, Swift Codable 三份但设计一致）
- 为 Web 开发提供开箱即用的基础

## Non-Goals

- Android/iOS 工程初始化 → glm-mvp-foundation (Web 确认后)
- CI/CD 搭建 → 同上

## Scope

极简：2 个工作日，3 个文件。

## Acceptance

- [ ] `mock_data.json` 可被 Web 和 Mobile 双端解析
- [ ] API 类型定义完整（16 endpoints × 三端）
