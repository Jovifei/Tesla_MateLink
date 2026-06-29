## 1. 前置条件

- [x] T-001 申请高德地图 API Key（https://lbs.amap.com/）

## 2. Android 地图切换（6h）

- [x] T-005 Dashboard 地图组件切换为高德（AmapPointView）
- [x] T-006 DriveDetail 路线地图切换为高德（AmapRouteView）
- [x] T-007 ChargeDetail 充电位置地图切换为高德（AmapPointView）
- [x] T-007b TripDetail 路线地图切换为高德（AmapRouteView）
- [x] T-007c WhereWasI 位置历史地图切换为高德（AmapPointView）
- [x] T-007d RegionsVisited 区域地图切换为高德（AmapComposeView 多点标注）
- [x] T-008 TopDestinations — Android 端不存在，无需切换（Web 端无 osmdroid）

## 3. iOS 地图切换 + 新页面（10-12h）

- [x] T-010c MAMapView UIViewRepresentable 封装（AMapRepresentable.swift）
- [x] T-010d AmapRouteView 路线专用视图
- [x] T-010e AmapMultiPointView 多点视图
- [x] T-013 Dashboard 地图组件切换为高德（内部替换）
- [x] T-014 DriveDetail 新增路线地图 section
- [x] T-015 ChargeDetail 新增充电位置地图 section
- [x] T-015b 新建 TripDetail 页面（路线地图 + 时间线）
- [x] T-015c 新建 WhereWasI 页面（位置历史地图）
- [x] T-015d 新建 RegionsVisited 页面（区域地图 + 多点标注）
- [x] T-016 新建 TopDestinations 页面（目的地地图 + 列表）

## 4. Android 真机测试（3 天）

- [x] T-050 准备 Android 测试设备（2+ 台不同厂商/版本）
- [x] T-051 安装并启动 App（adb install → 首次引导）
- [x] T-052 测试 Dashboard 功能（状态/电量/位置/地图）
- [x] T-053 测试充电历史功能（列表/详情/分时电价）
- [x] T-054 测试驾驶历史功能（行程/路线/统计）
- [x] T-055 测试电池健康功能（SOH/容量/温度）
- [x] T-056 测试高德地图显示（缩放/定位/路线/纠偏）
- [x] T-057 测试中文 UI（所有页面/语言切换）
- [x] T-058 测试分时电价（配置/计算/保存）
- [x] T-059 记录并修复发现的 bug

## 5. iOS 真机测试（3 天）

- [x] T-060 准备 iOS 测试设备（需要 Mac + iPhone）
- [x] T-061 安装并启动 App
- [x] T-062 测试 Dashboard 功能
- [x] T-063 测试充电历史功能
- [x] T-064 测试驾驶历史功能
- [x] T-065 测试电池健康功能
- [x] T-066 测试高德地图显示
- [x] T-067 测试中文 UI
- [x] T-068 测试分时电价
- [x] T-069 记录并修复发现的 bug

## 6. Web 端测试（1 天）

- [x] T-080 测试 react-i18next 语言切换
- [x] T-081 测试地图组件响应式布局
- [x] T-082 测试深色模式适配
- [x] T-083 测试分时电价配置页面

## 7. 异常/边界测试（1.5 天）

- [x] T-090 测试 GPS 信号丢失时地图行为
- [x] T-091 测试弱网环境下地图加载
- [x] T-092 测试高德 API Key 无效/过期时降级行为
- [x] T-093 测试跨时区地图切换
- [x] T-094 测试应用后台被杀死后地图状态恢复

## 8. 最终验证（3 天）

- [x] T-100 三端功能一致性验证（Android/iOS/Web 对比表）
- [x] T-101 性能测试（启动时间、帧率、内存、大量轨迹点渲染）
- [x] T-102 编写测试报告（通过率、遗留 bug、风险项）
