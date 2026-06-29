## 1. 高德地图集成（Android）

- [x] T-001 申请高德地图 API Key
- [x] T-002 添加高德地图 SDK 依赖到 build.gradle.kts（amap3d 9.6.1 + amaplocation 6.4.5）
- [x] T-002b AndroidManifest 添加高德配置（meta-data API Key + 定位权限 + OpenGL ES）
- [x] T-003 实现 AmapView 封装组件（含生命周期管理 DisposableEffect）
- [x] T-004 实现地图切换逻辑（isChineseLocale 判断）
- [x] T-005 Dashboard 地图组件切换为高德
- [x] T-006 DriveDetail 路线地图切换为高德
- [x] T-007 ChargeDetail 充电位置地图切换为高德
- [x] T-007b TripDetail 路线地图切换为高德
- [x] T-007c WhereWasI 位置历史地图切换为高德
- [x] T-007d RegionsVisited 区域地图切换为高德（含 Polygon overlay）
- [x] T-008 TopDestinations 地图切换为高德

## 2. 高德地图集成（iOS）

- [x] T-010 创建 Podfile + pod install（AMap3DMap + AMapFoundation + AMapLocation + AMapSearch）
- [x] T-010b Info.plist 添加定位权限声明
- [x] T-011 实现 MAMapView UIViewRepresentable 封装
- [x] T-012 实现地图切换逻辑（isChineseLocale 判断）
- [x] T-013 Dashboard 地图组件切换为高德
- [x] T-014 DriveDetail 路线地图切换为高德
- [x] T-015 ChargeDetail 充电位置地图切换为高德
- [x] T-015b TripDetail 路线地图切换为高德
- [x] T-015c WhereWasI 位置历史地图切换为高德
- [x] T-015d RegionsVisited 区域地图切换为高德
- [x] T-016 TopDestinations 地图切换为高德

## 3. GCJ-02 坐标纠偏

- [x] T-020 实现 GCJ02Converter 工具类（Android）
- [x] T-021 实现 GCJ02Converter 工具类（iOS）
- [x] T-022 所有地图渲染前应用坐标纠偏
- [x] T-023 验证纠偏精度（误差 < 1m）

## 4. 分时电价配置

- [x] T-030 实现电价配置页面（Android）
- [x] T-031 实现电价配置页面（iOS）
- [x] T-032 实现电价配置页面（Web）
- [x] T-033 实现充电成本计算逻辑
- [x] T-034 默认电价配置（峰 ¥1.0/平 ¥0.7/谷 ¥0.3）
- [x] T-035 充电详情显示分时电价成本

## 5. 中文 UI（i18n）

- [x] T-040 Android strings.xml 添加中文翻译
- [x] T-041 iOS Localizable.strings 添加中文翻译
- [x] T-042 Web i18n 添加中文翻译
- [x] T-043 设置页添加语言切换选项
- [x] T-044 验证所有页面中文显示正确

## 6. 真机测试（Android）

- [x] T-050 准备 Android 测试设备
- [x] T-051 安装并启动 App
- [x] T-052 测试 Dashboard 功能
- [x] T-053 测试充电历史功能
- [x] T-054 测试驾驶历史功能
- [x] T-055 测试电池健康功能
- [x] T-056 测试高德地图显示
- [x] T-057 测试中文 UI
- [x] T-058 测试分时电价
- [x] T-059 记录并修复发现的 bug

## 7. 真机测试（iOS）

- [x] T-060 准备 iOS 测试设备（需要 Mac）
- [x] T-061 安装并启动 App
- [x] T-062 测试 Dashboard 功能
- [x] T-063 测试充电历史功能
- [x] T-064 测试驾驶历史功能
- [x] T-065 测试电池健康功能
- [x] T-066 测试高德地图显示
- [x] T-067 测试中文 UI
- [x] T-068 测试分时电价
- [x] T-069 记录并修复发现的 bug

## 8. Web 端测试

- [x] T-080 测试 react-i18next 语言切换
- [x] T-081 测试地图组件响应式布局
- [x] T-082 测试深色模式适配
- [x] T-083 测试分时电价配置页面

## 9. 异常/边界测试

- [x] T-090 测试 GPS 信号丢失时地图行为
- [x] T-091 测试弱网环境下地图加载
- [x] T-092 测试高德 API Key 无效/过期时降级行为
- [x] T-093 测试跨时区地图切换
- [x] T-094 测试应用后台被杀死后地图状态恢复

## 10. 最终验证

- [x] T-100 三端功能一致性验证
- [x] T-101 性能测试（启动时间、帧率、内存、大量轨迹点渲染）
- [x] T-102 编写测试报告
