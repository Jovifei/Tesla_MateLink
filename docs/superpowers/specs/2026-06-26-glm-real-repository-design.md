---
comet_change: glm-real-repository
role: technical-design
canonical_spec: openspec
archived-with: 2026-06-29-glm-real-repository
status: final
---

# RealCarRepository 技术设计

## 1. 背景与目标

app_glm/android 的 `CarRepository` 当前仅 Mock 实现，整个 app 跑假数据。`TeslaMateApi`（16 端点）、Retrofit、OkHttp、Room 全部已配置，独缺 `RealCarRepository` 把 API 与缓存串联。

**目标**：实现 RealCarRepository 对接 TeslaMateApi，含 Room 缓存与离线降级，通过代理模式运行时切换 Mock/Real，让 app 能对接真实 TeslaMate 后端。

## 2. 架构

```
ViewModel → DelegatingCarRepository (implements CarRepository)
                │ delegate = if(useReal) real else mock
                ├─ @MockImpl → MockCarRepository (现有, 不动)
                └─ @RealImpl → RealCarRepository (新建)
                                  │
                                  ├─ TeslaMateApi (Retrofit, 现有)
                                  └─ Room DAOs (DriveDao/ChargeDao, 现有)
```

### 设计决策

**代理模式 vs Hilt 重建 vs 直接替换**：选代理模式。
- 运行时切换无需重启 app 或重建 Hilt 图
- ViewModel 零感知（CarRepository 接口不变）
- 代价：每次调用多一次设置读取，用 `StateFlow` 缓存规避

**Network-First vs Cache-First**：选 Network-First。
- 车辆数据（行程/充电）时效性高，优先拉新
- 网络失败降级到 Room 缓存，保证离线可用

**UI 开关**：默认 Mock，设置页暴露 Switch。
- 无真实后端时 app 不崩溃
- 有后端时用户可切 Real

## 3. 组件

### 3.1 RealCarRepository

```kotlin
@Singleton
class RealCarRepository @Inject constructor(
    private val api: TeslaMateApi,
    private val driveDao: DriveDao,
    private val chargeDao: ChargeDao,
    // 其他 DAOs 按需
) : CarRepository {

    override fun getCars(): Flow<List<Car>> = flow {
        // 先发射缓存，再拉网络刷新
        // ...
    }

    override suspend fun refreshCars(): List<Car> {
        return try {
            val response = api.getCars()
            if (response.isSuccessful) {
                val cars = response.body()!!.cars.map { it.toDomain() }
                // 写缓存（如有 CarDao）
                cars
            } else {
                emptyList() // 或读缓存
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 降级：读缓存
            emptyList()
        }
    }

    override suspend fun getDrives(carId: Int): List<Drive> {
        return try {
            val response = api.getDrives(carId)
            if (response.isSuccessful) {
                val entities = response.body()!!.drives.map { it.toEntity() }
                driveDao.upsertAll(entities)
                entities.map { it.toDomain() }
            } else {
                driveDao.getAllForCar(carId).map { it.toDomain() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            driveDao.getAllForCar(carId).map { it.toDomain() }
        }
    }
    // getCharges / getBatteryHealth / getUpdates 同理
}
```

### 3.2 DelegatingCarRepository

```kotlin
@Singleton
class DelegatingCarRepository @Inject constructor(
    private val settings: SettingsDataStore,
    @MockImpl private val mock: CarRepository,
    @RealImpl private val real: CarRepository
) : CarRepository {

    private val delegate: CarRepository
        get() = if (settings.useRealDataSourceSnapshot()) real else mock

    override fun getCars() = delegate.getCars()
    override fun getCar(carId: Int) = delegate.getCar(carId)
    override suspend fun refreshCars() = delegate.refreshCars()
    override suspend fun getDrives(carId: Int) = delegate.getDrives(carId)
    override suspend fun getCharges(carId: Int) = delegate.getCharges(carId)
    override suspend fun getBatteryHealth(carId: Int) = delegate.getBatteryHealth(carId)
    override suspend fun getUpdates(carId: Int) = delegate.getUpdates(carId)
}
```

### 3.3 AppModule 绑定

```kotlin
@Provides @Singleton @RealImpl
fun provideRealCarRepository(
    api: TeslaMateApi,
    driveDao: DriveDao,
    chargeDao: ChargeDao
): CarRepository = RealCarRepository(api, driveDao, chargeDao)

@Provides @Singleton
fun provideCarRepository(
    delegating: DelegatingCarRepository
): CarRepository = delegating
```

### 3.4 SettingsDataStore

```kotlin
val useRealDataSource: Flow<Boolean> = context.settingsDataStore.data
    .map { it[USE_REAL_DATA_SOURCE_KEY] ?: false }

private val useRealSnapshot = MutableStateFlow(false) // 启动时初始化
fun useRealDataSourceSnapshot(): Boolean = useRealSnapshot.value
suspend fun setUseRealDataSource(value: Boolean) { ... }
```

## 4. 数据流

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
      ├─ TeslaMateApi.getDrives(carId) → Response<DrivesResponse>
      │     │ response.drives.map { it.toEntity() }
      │     ▼
      │   DriveDao.upsertAll(entities)
      │     │ DriveDao.getAllForCar(carId).map { it.toDomain() }
      │     ▼
      │   返回 List<Drive>
      │
      └─ (失败) DriveDao.getAllForCar(carId) → 返回缓存
```

## 5. 映射函数

复用现有 `CarRaw.toDomain()` / `DriveRaw.toDomain()` / `ChargeRaw.toDomain()` / `UpdateRaw.toDomain()`（在 `domain/model/CarModels.kt`）。

新增 `toEntity()` 映射（API Response → Room Entity），若字段缺失用默认值。

## 6. 错误处理

- API 调用包裹 try/catch，捕获 IOException / HttpException
- **CancellationException 必须 re-throw**（结构化并发要求）
- 失败降级到 Room，不向上抛异常
- 日志记录失败原因

## 7. 测试策略

| 测试 | 方法 |
|------|------|
| RealCarRepository API 调用 | MockWebServer 模拟响应，验证 DAO 写入 |
| 降级到缓存 | MockWebServer 返回错误，验证返回 Room 缓存 |
| 代理转发 | DelegatingCarRepository 按设置调用正确 delegate |
| 映射正确性 | Response → Entity → Domain 字段对齐 |

## 8. 范围与非目标

**包含**：RealCarRepository、DelegatingCarRepository、@RealImpl 绑定、设置开关、单元测试

**非目标**：
- RealStatusRepository（实时状态走单独 5s 轮询，本轮不动）
- ViewModel 层改动（接口不变）
- 后端 API 兼容性适配
- 真实后端联调测试（无运行中后端）
- 自动分页（只取第一页）

## 9. 风险

| 风险 | 缓解 |
|------|------|
| 无真实后端 | MockWebServer 单元测试覆盖 |
| Response/Entity 字段不对齐 | T-001 先核对，缺失字段用默认值 |
| 代理层性能 | StateFlow 缓存设置值 |
| 分页 | 本轮只取第一页，后续优化 |
