## 1. iOS 修复与增强

- [x] T-101 提取 `TariffConfigView.swift` 硬编码中文字符串到 `Localizable.strings`（en + zh-Hans）
- [x] T-102 `GCJ02Converter.swift` 增加 `isInSpecialRegion()` 检测 HK/Macau 并跳过偏移
- [x] T-103 修复 `TariffConfigView.swift` DatePicker locale 改为 `Locale.current`，不硬编码 `zh_Hans_CN`
- [x] T-104 `CostView.swift` 接入 `TariffConfig.priceForHour()` 显示 TOU 估算成本

## 2. Android 高德地图集成

- [x] T-201 添加 `com.amap.api:3dmap:9.6.1` + `com.amap.api:location:6.4.5` 到 `gradle/libs.versions.toml` 和 `app/build.gradle.kts`
- [x] T-202 `AndroidManifest.xml` 添加 AMap API key meta-data + 定位权限 + OpenGL ES 配置
- [x] T-203 端口 `app_mimo` 的 `AmapComposeView.kt` 到 `app_glm/android/.../ui/components/`，包名改为 `com.teslamatelink`
- [x] T-204 创建 `util/MapUtils.kt` 含 `isChineseLocale()` helper
- [x] T-205 `DashboardScreen.kt` 的 `LocationCard` 替换文本占位为 `AmapComposeView`（仅 zh locale，缺 key 显示 fallback）

## 3. Android TOU 默认费率

- [x] T-206 创建 `data/model/TariffConfig.kt`（hardcoded 默认费率，无配置 UI）
- [x] T-207 `CostScreen.kt` 显示 TOU 估算成本（hardcode 默认）

## 4. 共享验证

- [x] T-301 验证 iOS/Android `GCJ02Converter` 输出一致（同坐标输入 → 同输出，误差 <1m）
- [x] T-302 更新 `app_glm/shared/api-types.ts` 添加 TariffConfig schema
