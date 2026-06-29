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
- `CancellationException` 必须 re-throw
- 失败时降级到 Room，不向上抛异常
- 日志记录失败原因（Timber/Log）

### 设置开关

`SettingsDataStore` 增加 `useRealDataSource: Flow<Boolean>`，默认 `false`。设置页增加 Switch。

## Data Flow

```
[用户在设置页切换 Real]
      │
      ▼
SettingsDataStore.useRealDataSource = true
      │
      ▼
DelegatingCarRepository.delegate → RealCarRepository
      │
      ▼
RealCarRepository.getDrives(carId)
      │
      ├─ TeslaMateApi.getDrives() → Response<DrivesResponse>
      │     │ DrivesResponse.drives.map { it.toEntity() }
      │     ▼
      │   DriveDao.upsertAll(entities)
      │     │ DriveDao.getAllForCar().map { it.toDomain() }
      │     ▼
      │   返回 List<Drive>
      │
      └─ (失败) DriveDao.getAllForCar() → 返回缓存
```

## Risks

- **无真实后端**：无法集成测试。用 MockWebServer 模拟 API 响应，单元测试覆盖映射 + 降级。
- **Response/Entity 字段不对齐**：需核对每个 model。若字段缺失，映射时用默认值。
- **分页**：API 支持 page/show 参数，本轮不实现自动分页，只取第一页（默认足够，后续优化）。
- **Hilt 代理 vs 切换**：代理模式运行时切换，但首次访问设置有阻塞风险——用 Flow + 默认 false 规避。
