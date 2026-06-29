# Comet Design Handoff

- Change: glm-real-repository
- Phase: design
- Mode: compact
- Context hash: a735477df3a188faa19efada971fcaf5c598104be9fa56a691961e692da2cd8d

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/glm-real-repository/proposal.md

- Source: openspec/changes/glm-real-repository/proposal.md
- Lines: 1-28
- SHA256: 72e285b3e528ffeb353890f392b37570a10b0b5b8cb649d8f34e3b1fc92b2466

```md
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
```

## openspec/changes/glm-real-repository/design.md

- Source: openspec/changes/glm-real-repository/design.md
- Lines: 1-119
- SHA256: 0e5ed3b36038acd8badde7f03498e4d87a81fb0ca6b93cb3ab1bf3cfe0e8dd64

[TRUNCATED]

```md
## Architecture Decisions

### 1. 缓存策略：Network-First with Cache Fallback

```
ViewModel 调用 getDrives(carId)
        │
        ▼
RealCarRepository
        │
        ├─ 1. 调用 TeslaMateApi.getDrives()  (网络)
        │     ├─ 成功 → 写入 Room → 返回 domain List
        │     └─ 失败/超时 → 进入 step 2
        │
        └─ 2. 读 Room DriveDao.getAllForCar()  (缓存降级)
              ├─ 有缓存 → 返回缓存
              └─ 无缓存 → 返回 emptyList (不崩溃)
```

**为何选 Network-First**：车辆数据时效性高（行程/充电记录），优先拉新数据；网络失败时降级到缓存，保证可用性。

### 2. 注解切换：@MockImpl / @RealImpl

```kotlin
@Provides @Singleton @MockImpl
fun provideMockCarRepository(...): CarRepository

@Provides @Singleton @RealImpl
fun provideRealCarRepository(...): CarRepository

// 默认绑定，根据设置动态选择
@Provides @Singleton
fun provideCarRepository(
    settings: SettingsDataStore,
    @MockImpl mock: CarRepository,
    @RealImpl real: CarRepository
): CarRepository = if (settings.useRealDataSourceBlocking()) real else mock
```

切换无需重启 app（Hilt 单例重建需重启，但本设计改为 Repository 内部代理）。

### 3. 代理模式（避免重启）

实际采用**代理 Repository** 包裹 Mock 和 Real，运行时按设置转发，无需重建 Hilt 图：

```kotlin
@Singleton
class DelegatingCarRepository @Inject constructor(
    private val settings: SettingsDataStore,
    @MockImpl private val mock: CarRepository,
    @RealImpl private val real: CarRepository
) : CarRepository {
    private val delegate get() = if (settings.useRealDataSourceFlow.value) real else mock
    // 转发所有方法到 delegate
}
```

## Approach

### 数据流

```
TeslaMateApi (Retrofit) → Response<Model>
        │ toEntity()
        ▼
    Room Dao (upsert)  ←── 写缓存
        │ toDomain()
        ▼
    CarRepository 返回 domain List<Drive> 等
```

### 关键映射

复用现有 `CarRaw.toDomain()` / `DriveRaw.toDomain()` / `ChargeRaw.toDomain()` / `UpdateRaw.toDomain()`（已在 `domain/model/CarModels.kt`）。

如 API Response → Room Entity 映射不存在，新增 `CarRaw.toEntity()` 等扩展函数。

### 错误处理

- API 调用包裹 `try/catch`，捕获 `IOException` / `HttpException`
```

Full source: openspec/changes/glm-real-repository/design.md

## openspec/changes/glm-real-repository/tasks.md

- Source: openspec/changes/glm-real-repository/tasks.md
- Lines: 1-25
- SHA256: 6b224b049b9b1bb246540371ef2a8eb638e784846f22806fc17c8c4b8de687cc

```md
## 1. 数据层映射

- [ ] T-001 核对 API Response model (CarRaw/DriveRaw/ChargeRaw/UpdateRaw) 与 Room Entity 字段对齐，补全缺失的 `toEntity()` 映射函数

## 2. RealCarRepository 实现

- [ ] T-002 新建 `RealCarRepository.kt`，实现 `CarRepository` 接口，注入 `TeslaMateApi` + DAOs
- [ ] T-003 实现 Network-First 缓存策略：API 成功写 Room，失败降级读 Room，CancellationException re-throw
- [ ] T-004 实现各方法：refreshCars / getDrives / getCharges / getBatteryHealth / getUpdates（getCars/getCar 走 Flow）

## 3. DI 与运行时切换

- [ ] T-005 在 `AppModule.kt` 增加 `@RealImpl` 绑定 RealCarRepository
- [ ] T-006 新建 `DelegatingCarRepository`，按设置在 Mock/Real 间转发
- [ ] T-007 `SettingsDataStore` 增加 `useRealDataSource: Flow<Boolean>`，默认 false

## 4. UI 开关

- [ ] T-008 `SettingsScreen` + `SettingsViewModel` 增加数据源切换 Switch

## 5. 测试

- [ ] T-009 单元测试：MockWebServer 验证 RealCarRepository API 调用 + 缓存写入
- [ ] T-010 单元测试：API 失败时降级到 Room，返回缓存不崩溃
- [ ] T-011 单元测试：DelegatingCarRepository 按设置正确转发
```

