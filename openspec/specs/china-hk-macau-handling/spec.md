# china-hk-macau-handling Specification

## Purpose
TBD - created by archiving change glm-p1-china. Update Purpose after archive.
## Requirements
### Requirement: HK/Macau 坐标跳过 GCJ-02 偏移
GCJ02Converter SHALL 识别香港和澳门特别行政区的坐标，并在 `wgs84ToGcj02()` 调用时跳过偏移转换（这两地区实际使用 WGS-84）。

#### Scenario: 香港坐标
- **WHEN** 输入坐标在 HK 范围（lat 22.15-22.55, lng 113.83-114.42）
- **THEN** `wgs84ToGcj02()` 返回原坐标，不应用偏移

#### Scenario: 澳门坐标
- **WHEN** 输入坐标在 Macau 范围（lat 22.10-22.22, lng 113.52-113.60）
- **THEN** `wgs84ToGcj02()` 返回原坐标，不应用偏移

#### Scenario: 中国大陆坐标
- **WHEN** 输入坐标在中国大陆（不在 HK/Macau 范围内）
- **THEN** `wgs84ToGcj02()` 应用 GCJ-02 偏移转换

#### Scenario: 中国境外坐标
- **WHEN** 输入坐标在中国境外（isInChina() 返回 false）
- **THEN** `wgs84ToGcj02()` 返回原坐标

