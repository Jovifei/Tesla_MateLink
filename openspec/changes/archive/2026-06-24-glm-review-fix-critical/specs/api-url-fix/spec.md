## ADDED Requirements

### Requirement: API URL 路径规范化
TeslaMateAPI SHALL 自动处理 API path 的前导 `/`，确保所有调用方无论传入 `"api/v1/cars/1/drives"` 还是 `"/api/v1/cars/1/drives"` 都能正确构造 URL。

#### Scenario: 不带前导斜杠的路径
- **WHEN** 调用 `fetch("api/v1/cars/1/drives")`
- **THEN** 构造的 URL 为 `{baseURL}/api/v1/cars/1/drives`

#### Scenario: 带前导斜杠的路径
- **WHEN** 调用 `fetch("/api/v1/cars/1/status")`
- **THEN** 构造的 URL 为 `{baseURL}/api/v1/cars/1/status`

#### Scenario: checkStatus 路径规范化
- **WHEN** 调用 `checkStatus("api/ping")`
- **THEN** 构造的 URL 为 `{baseURL}/api/ping`

## Open Items

> T7 audit 2026-06-24: 以下变体当前未满足。

1. **app_mimo 变体未修复**: `app_mimo/ios/.../ApiClient.swift` 的 `fetch()` 和 `checkStatus()` 仍直接拼接 `baseURL + path`，缺少 `normalizedPath` 前导 `/` 处理。仅 `app_glm` 变体已修复。
2. **影响评估**: 如果 app_mimo 仍作为活跃变体使用，其 API 调用在 path 无前导 `/` 时会产生错误 URL（如 `http://hostapi/v1/...`）。
