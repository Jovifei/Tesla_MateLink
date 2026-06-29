## Why

mimo-localization-test 已完成 22/70 任务（31%），剩余 48 个任务需要处理：
- Android 地图组件切换（6 个页面）
- iOS 地图组件切换 + 新页面（7 个页面）
- 真机测试（Android + iOS + Web）
- 异常/边界测试
- 最终验证

## What Changes

### Android 地图切换
- Dashboard 地图组件切换为高德
- DriveDetail 路线地图切换为高德
- ChargeDetail 充电位置地图切换为高德
- TripDetail 路线地图切换为高德
- WhereWasI 位置历史地图切换为高德
- RegionsVisited 区域地图切换为高德（需扩展 Polygon 支持）

### iOS 地图切换 + 新页面
- MAMapView UIViewRepresentable 封装
- Dashboard 地图组件切换为高德
- DriveDetail 新增路线地图
- ChargeDetail 新增充电位置地图
- 新建 TripDetail、WhereWasI、RegionsVisited、TopDestinations 页面

### 测试
- Android 真机测试（3 天）
- iOS 真机测试（3 天）
- Web 端测试（1 天）
- 异常/边界测试（1.5 天）
- 最终验证（3 天）

## Capabilities

### New Capabilities

- `android-map-switching`: Android 高德地图组件切换
- `ios-map-switching`: iOS 高德地图组件切换 + 新页面
- `device-testing`: 真机测试策略
- `final-verification`: 三端一致性验证 + 性能测试

### Modified Capabilities

- `dashboard`: 地图组件切换
- `drive-detail`: 路线地图切换
- `charge-detail`: 充电位置地图切换

## Impact

- Android 6 个页面需要修改
- iOS 需要新建 4 个页面 + 修改 3 个页面
- 需要高德 API Key（T-001）
- 需要真机测试设备
