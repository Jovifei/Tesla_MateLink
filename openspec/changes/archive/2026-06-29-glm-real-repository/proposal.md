## Why

app_glm/android 当前 `CarRepository` 仅 Mock 实现，整个 app 跑假数据。`TeslaMateApi`（16 端点）、Retrofit、OkHttp、Room 全部已配置好，独缺 `RealCarRepository` 把 API 与缓存串联。这是 app 从"能编译"到"能真正对接 TeslaMate 后端"的关键缺口。

## What Changes

- **新增** `RealCarRepository.kt`：实现 `CarRepository` 接口，对接 `TeslaMateApi`，含 Room 缓存与离线降级
- **新增** API Response → Room Entity 映射函数（若现有 `toDomain()` 不够用）
- **修改** `AppModule.kt`：增加 `@RealImpl` 限定的 `CarRepository` 绑定
- **修改** `SettingsDataStore` + `SettingsScreen`：增加 Mock/Real 数据源切换开关
- **新增** 单元测试：用 MockWebServer 验证 API 调用与缓存降级

## Capabilities

### New Capabilities

- `real-data-source`: 对接 TeslaMateApi 的真实数据源，含 Room 缓存、离线降级、Mock/Real 运行时切换

### Modified Capabilities

无（Repository 接口不变，对 ViewModel 透明）。

## Impact

- 新增文件：`RealCarRepository.kt`、映射函数、单元测试
- 修改文件：`AppModule.kt`、`SettingsDataStore.kt`、`SettingsScreen.kt`、`SettingsViewModel.kt`
- 依赖：复用现有 Retrofit/OkHttp/Room/Gson，无新增依赖
- 风险：无真实后端，集成测试缺位；用 MockWebServer + 单元测试覆盖
