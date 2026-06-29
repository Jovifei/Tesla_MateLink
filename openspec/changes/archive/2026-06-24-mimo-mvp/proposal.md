## Why

TeslaMate 是一个优秀的自托管 Tesla 数据记录器，但没有官方移动端 App。车主只能通过浏览器访问 Grafana 仪表盘，手机体验极差。中国用户还面临地图不可用（OpenStreetMap 国内数据稀疏）、无中文界面等问题。

本 change（mimo-mvp）构建 **MateLink** App 的第一个可上架版本，让 TeslaMate 用户在手机上以原生体验查看车辆数据。

**品牌名**：MateLink
**商业模式**：前期免费，后期可能收费多功能
**视觉风格**：Apple-Like + 基于车色的强调色

## What Changes

- **新建 Android App**（Kotlin + Jetpack Compose）：Dashboard、充电/驾驶历史、电池健康、设置
- **新建 iOS App**（Swift + SwiftUI）：同上功能，共享 API 层设计
- **中国本地化**：高德地图（GCJ-02 纠偏）、分时电价、中文 UI
- **Mock 模式**：内置虚拟数据，无需 TeslaMate 实例即可预览
- **离线缓存**：最近 30 天数据可离线查看
- **后端依赖**：用户自托管 TeslaMateApi v1.21+（不自建后端）

## Capabilities

### New Capabilities

- `dashboard`: 实时车辆状态展示（电量、续航、位置、胎压、2D 车辆图）
- `charge-history`: 充电记录列表 + 详情（功率曲线、地图、费用）
- `drive-history`: 驾驶记录列表 + 详情（轨迹地图、速度/功率曲线）
- `battery-health`: 电池衰减趋势、容量对比、健康度百分比
- `china-localization`: 高德地图集成、GCJ-02 坐标纠偏、分时电价计算、中文 UI
- `mock-mode`: 内置虚拟数据模式，支持 UI 预览和截图
- `offline-cache`: 离线数据缓存，弱网/无网可查看历史
- `settings`: 服务器配置、主题切换单位偏好、多车切换
- `app-foundation`: 项目骨架、导航、主题、API Client、错误处理

### Modified Capabilities

（无，这是全新项目）

## Impact

- **新建代码库**：`E:\project\tesla_master\app_mimo\`（Android + iOS 双端）
- **外部依赖**：TeslaMateApi v1.21+（Docker 部署）、高德地图 SDK
- **上架目标**：App Store + Google Play
- **参考仓库**：matedroid (Android)、t-buddy (iOS)、teslamateapi (API)、teslamate-chinese-dashboards (中国本地化)
