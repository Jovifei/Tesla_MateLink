# android-gcj02 Specification

## Purpose
TBD - created by archiving change glm-review-fix-critical. Update Purpose after archive.
## Requirements
### Requirement: WGS-84 到 GCJ-02 坐标转换
系统 SHALL 提供 `GCJ02Converter` 对象，支持 WGS-84 ↔ GCJ-02 双向坐标转换，包含中国境内判断。

#### Scenario: 中国境内坐标转换
- **WHEN** 输入 WGS-84 坐标 (39.9087, 116.3975)（北京）
- **THEN** `wgs84ToGcj02()` 返回偏移后的 GCJ-02 坐标，偏移量 ~100-700m

#### Scenario: 中国境外坐标不变
- **WHEN** 输入 WGS-84 坐标 (48.8566, 2.3522)（巴黎）
- **THEN** `wgs84ToGcj02()` 返回原坐标无偏移

#### Scenario: GCJ-02 反算 WGS-84
- **WHEN** 输入 GCJ-02 坐标
- **THEN** `gcj02ToWgs84()` 返回对应的 WGS-84 坐标，精度 < 0.5m

