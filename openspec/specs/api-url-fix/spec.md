# api-url-fix Specification

## Purpose
TBD - created by archiving change glm-review-fix-critical. Update Purpose after archive.
## Requirements
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

