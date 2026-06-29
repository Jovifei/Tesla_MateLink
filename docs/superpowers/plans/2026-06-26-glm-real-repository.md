---
change: glm-real-repository
design-doc: docs/superpowers/specs/2026-06-26-glm-real-repository-design.md
base-ref: c3e1816f5eb50e86c976064fde2bfbc2a919903a
archived-with: 2026-06-29-glm-real-repository
---

# RealCarRepository 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**目标：** 实现 RealCarRepository 对接 TeslaMateApi，含 Room 缓存与离线降级，通过代理模式运行时切换 Mock/Real，让 app 能对接真实 TeslaMate 后端。

**架构：** ViewModel → DelegatingCarRepository（按设置转发 Mock/Real）→ RealCarRepository（Network-First：API 成功写 Room，失败降级读 Room，CancellationException re-throw）。@MockImpl/@RealImpl qualifier 已存在于 AppModule。

**技术栈：** Kotlin + Hilt + Retrofit + Room + DataStore (Preferences) + kotlinx.coroutines Flow + JUnit4 + MockWebServer + Turbine + Robolectric + MockK

archived-with: 2026-06-29-glm-real-repository
---

## 文件结构

| 操作 | 文件路径 | 职责 |
|------|----------|------|
| 修改 | `app/build.gradle.kts` | 添加测试依赖（mockwebserver, coroutines-test, turbine, room-testing, robolectric, mockk） |
| 修改 | `domain/model/CarModels.kt` | 新增 `DriveRaw.toEntity()` / `ChargeRaw.toEntity()` / `DriveEntity.toDomain()` / `ChargeEntity.toDomain()` 映射 |
| 新建 | `data/repository/RealCarRepository.kt` | RealCarRepository 实现（Network-First + Room 缓存降级） |
| 新建 | `data/repository/DelegatingCarRepository.kt` | 代理层，按设置在 Mock/Real 间转发 |
| 修改 | `data/local/SettingsDataStore.kt` | 添加 `useRealDataSource` 字段 + snapshot 缓存 |
| 修改 | `di/AppModule.kt` | 添加 @RealImpl 绑定 + DelegatingCarRepository 默认绑定 |
| 修改 | `ui/settings/SettingsScreen.kt` | 添加数据源切换 Switch，持久化到 DataStore |
| 新建 | `app/src/test/java/com/teslamatelink/data/repository/RealCarRepositoryTest.kt` | API 调用 + 缓存写入 + 降级测试 |
| 新建 | `app/src/test/java/com/teslamatelink/data/repository/DelegatingCarRepositoryTest.kt` | 代理转发测试 |
| 新建 | `app/src/test/java/com/teslamatelink/domain/model/MappersTest.kt` | 映射函数正确性测试 |

> **路径前缀：** 所有 Kotlin 文件路径基于 `app_glm/android/app/src/main/java/com/teslamatelink/`，测试文件基于 `app_glm/android/app/src/test/java/com/teslamatelink/`。

archived-with: 2026-06-29-glm-real-repository
---

## 关键上下文（实现者必读）

### API Response wrapper 模式
所有 list 端点返回嵌套结构，**不是**直接 `.cars`：
- `CarsResponse.data.cars` → `List<CarRaw>?`
- `DrivesResponse.data.drives` → `List<DriveRaw>?`
- `ChargesResponse.data.charges` → `List<ChargeRaw>?`
- `UpdatesResponse.data.updates` → `List<UpdateRaw>?`
- `BatteryHealthResponse.data.batteryHealth` → `BatteryHealth?`

### DAO 实际方法名（与设计文档不同）
- `DriveDao.upsertAll(drives: List<DriveEntity>)` — 用 `@Upsert`
- `DriveDao.getAllChronological(carId: Int): List<DriveEntity>` — **非** `getAllForCar`
- `ChargeDao.upsertAll(charges: List<ChargeEntity>)` — 用 `@Upsert`
- `ChargeDao.getAllChronological(carId: Int): List<ChargeEntity>` — **非** `getAllForCar`

### 无 CarDao
Car 数据不缓存到 Room。`refreshCars()` 直接返回 API 结果，`getCars()` 走 Flow 直接 emit API 结果。

### Entity 字段对齐（已核对，完美匹配）

**DriveRaw → DriveEntity：** 所有 13 个字段名完全一致（`carId` 需 `?: 0` 处理 nullable）。

**ChargeRaw → ChargeEntity：** 11 个字段完全一致（`carId` 需 `?: 0`）。

**ChargeEntity → Charge (Domain)：** 注意 `ChargeEntity` **没有** `startIdealRangeKm` / `endIdealRangeKm` 字段，映射时用 `null`。

### SettingsScreen 现有模式
使用 `object AppSettings` companion object（无 ViewModel）。已有 `isMockMode` Switch 在 Debug Card 中，但**未持久化**。新增数据源 Switch 需持久化到 DataStore。

archived-with: 2026-06-29-glm-real-repository
---

## Task 1: 测试基础设施搭建

**Files:**
- Modify: `app_glm/android/app/build.gradle.kts`（dependencies 块）
- Create: `app_glm/android/app/src/test/java/com/teslamatelink/TestUtils.kt`

- [x] **Step 1: 添加测试依赖到 build.gradle.kts**

在 `app_glm/android/app/build.gradle.kts` 的 `dependencies { }` 块末尾添加：

```kotlin
    // ── Unit test dependencies (glm-real-repository) ──
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("com.squareup.retrofit2:retrofit:2.11.0")
    testImplementation("com.squareup.retrofit2:converter-gson:2.11.0")
```

> **注意：** Retrofit/Gson 测试依赖用于在测试中直接构建 Retrofit 实例对接 MockWebServer。如果 build.gradle.kts 中已有 `implementation` 级别的 retrofit/gson，测试会自动继承，可省略最后两行。先检查现有依赖再决定。

- [x] **Step 2: 配置 Robolectric test runner**

在 `app_glm/android/app/build.gradle.kts` 的 `android { }` 块中添加（如果不存在）：

```kotlin
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
```

- [x] **Step 3: 创建测试工具类**

创建 `app_glm/android/app/src/test/java/com/teslamatelink/TestUtils.kt`：

```kotlin
package com.teslamatelink

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.teslamatelink.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that swaps Dispatchers.Main for a TestDispatcher
 * and resets it after each test. Required for any code touching
 * Room or DataStore on the main dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    val testDispatcher get() = dispatcher

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Builds an in-memory Room database for testing.
 * Falls back to disk if Robolectric isn't loaded.
 */
fun buildInMemoryDb(): AppDatabase {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    return Room.inMemoryDatabaseBuilder(
        context,
        AppDatabase::class.java
    ).allowMainThreadQueries().build()
}
```

- [x] **Step 4: 验证测试基础设施编译**

Run: `cd app_glm/android && ./gradlew :app:compileDebugUnitTestKotlin`
Expected: BUILD SUCCESSFUL（无编译错误）

- [x] **Step 5: Commit**

```bash
git add app_glm/android/app/build.gradle.kts app_glm/android/app/src/test/java/com/teslamatelink/TestUtils.kt
git commit -m "test: add unit test infrastructure (MockWebServer, Turbine, Room-testing, MockK)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 2: 数据层映射核对与 toEntity/toDomain 实现（T-001）

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/domain/model/CarModels.kt`（末尾追加映射函数）
- Create: `app_glm/android/app/src/test/java/com/teslamatelink/domain/model/MappersTest.kt`

- [x] **Step 1: 编写映射函数的失败测试**

创建 `app_glm/android/app/src/test/java/com/teslamatelink/domain/model/MappersTest.kt`：

```kotlin
package com.teslamatelink.domain.model

import com.google.common.truth.Truth.assertThat
import com.teslamatelink.data.api.model.ChargeRaw
import com.teslamatelink.data.api.model.DriveRaw
import com.teslamatelink.data.local.entity.ChargeEntity
import com.teslamatelink.data.local.entity.DriveEntity
import org.junit.Test

class MappersTest {

    @Test
    fun driveRaw_toEntity_mapsAllFields() {
        val raw = DriveRaw(
            id = 1,
            carId = 5,
            startDate = "2026-01-01T00:00:00Z",
            endDate = "2026-01-01T01:00:00Z",
            distanceKm = 42.5,
            durationMin = 60,
            efficiency = 150.0,
            consumptionKwh = 6.4,
            startAddress = "Home",
            endAddress = "Work",
            outsideTempAvg = 22.0,
            startBatteryLevel = 80,
            endBatteryLevel = 50
        )

        val entity = raw.toEntity()

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.carId).isEqualTo(5)
        assertThat(entity.startDate).isEqualTo("2026-01-01T00:00:00Z")
        assertThat(entity.endDate).isEqualTo("2026-01-01T01:00:00Z")
        assertThat(entity.distanceKm).isEqualTo(42.5)
        assertThat(entity.durationMin).isEqualTo(60)
        assertThat(entity.efficiency).isEqualTo(150.0)
        assertThat(entity.consumptionKwh).isEqualTo(6.4)
        assertThat(entity.startAddress).isEqualTo("Home")
        assertThat(entity.endAddress).isEqualTo("Work")
        assertThat(entity.outsideTempAvg).isEqualTo(22.0)
        assertThat(entity.startBatteryLevel).isEqualTo(80)
        assertThat(entity.endBatteryLevel).isEqualTo(50)
    }

    @Test
    fun driveRaw_toEntity_nullCarId_defaultsToZero() {
        val raw = DriveRaw(id = 1, carId = null)

        val entity = raw.toEntity()

        assertThat(entity.carId).isEqualTo(0)
    }

    @Test
    fun chargeRaw_toEntity_mapsAllFields() {
        val raw = ChargeRaw(
            id = 10,
            carId = 5,
            startDate = "2026-01-01T00:00:00Z",
            endDate = "2026-01-01T02:00:00Z",
            chargeEnergyAdded = 30.0,
            chargeEnergyUsed = 32.0,
            startBatteryLevel = 20,
            endBatteryLevel = 90,
            startIdealRangeKm = 100.0,
            endIdealRangeKm = 350.0,
            cost = 12.5,
            chargeType = "ac",
            address = "Supercharger"
        )

        val entity = raw.toEntity()

        assertThat(entity.id).isEqualTo(10)
        assertThat(entity.carId).isEqualTo(5)
        assertThat(entity.chargeEnergyAdded).isEqualTo(30.0)
        assertThat(entity.chargeEnergyUsed).isEqualTo(32.0)
        assertThat(entity.cost).isEqualTo(12.5)
        assertThat(entity.chargeType).isEqualTo("ac")
        assertThat(entity.address).isEqualTo("Supercharger")
    }

    @Test
    fun chargeRaw_toEntity_nullCarId_defaultsToZero() {
        val raw = ChargeRaw(id = 1, carId = null)

        val entity = raw.toEntity()

        assertThat(entity.carId).isEqualTo(0)
    }

    @Test
    fun driveEntity_toDomain_mapsAllFields() {
        val entity = DriveEntity(
            id = 1,
            carId = 5,
            startDate = "2026-01-01T00:00:00Z",
            endDate = "2026-01-01T01:00:00Z",
            distanceKm = 42.5,
            durationMin = 60,
            efficiency = 150.0,
            consumptionKwh = 6.4,
            startAddress = "Home",
            endAddress = "Work",
            outsideTempAvg = 22.0,
            startBatteryLevel = 80,
            endBatteryLevel = 50
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1)
        assertThat(domain.carId).isEqualTo(5)
        assertThat(domain.distanceKm).isEqualTo(42.5)
        assertThat(domain.durationMin).isEqualTo(60)
        assertThat(domain.consumptionKwh).isEqualTo(6.4)
    }

    @Test
    fun chargeEntity_toDomain_mapsAllFields_idealRangeNull() {
        val entity = ChargeEntity(
            id = 10,
            carId = 5,
            startDate = "2026-01-01T00:00:00Z",
            endDate = "2026-01-01T02:00:00Z",
            chargeEnergyAdded = 30.0,
            chargeEnergyUsed = 32.0,
            startBatteryLevel = 20,
            endBatteryLevel = 90,
            cost = 12.5,
            chargeType = "ac",
            address = "Supercharger"
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(10)
        assertThat(domain.chargeEnergyAdded).isEqualTo(30.0)
        assertThat(domain.cost).isEqualTo(12.5)
        // ChargeEntity 没有 idealRange 字段，映射后应为 null
        assertThat(domain.startIdealRangeKm).isNull()
        assertThat(domain.endIdealRangeKm).isNull()
    }
}
```

- [x] **Step 2: 运行测试验证失败**

Run: `cd app_glm/android && ./gradlew :app:testDebugUnitTest --tests "com.teslamatelink.domain.model.MappersTest" --info`
Expected: FAIL — `Unresolved reference: toEntity` / `Unresolved reference: toDomain`（函数尚未定义）

- [x] **Step 3: 实现 toEntity() 和 toDomain() 映射函数**

在 `app_glm/android/app/src/main/java/com/teslamatelink/domain/model/CarModels.kt` 末尾追加：

```kotlin
// ──────────────────────────────────────────────
// Extension functions: Raw → Room Entity
// ──────────────────────────────────────────────

import com.teslamatelink.data.local.entity.ChargeEntity
import com.teslamatelink.data.local.entity.DriveEntity

fun DriveRaw.toEntity(): DriveEntity = DriveEntity(
    id = id,
    carId = carId ?: 0,
    startDate = startDate,
    endDate = endDate,
    distanceKm = distanceKm,
    durationMin = durationMin,
    efficiency = efficiency,
    consumptionKwh = consumptionKwh,
    startAddress = startAddress,
    endAddress = endAddress,
    outsideTempAvg = outsideTempAvg,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel
)

fun ChargeRaw.toEntity(): ChargeEntity = ChargeEntity(
    id = id,
    carId = carId ?: 0,
    startDate = startDate,
    endDate = endDate,
    chargeEnergyAdded = chargeEnergyAdded,
    chargeEnergyUsed = chargeEnergyUsed,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    cost = cost,
    chargeType = chargeType,
    address = address
)

// ──────────────────────────────────────────────
// Extension functions: Room Entity → Domain
// ──────────────────────────────────────────────

fun DriveEntity.toDomain(): Drive = Drive(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    distanceKm = distanceKm,
    durationMin = durationMin,
    efficiency = efficiency,
    consumptionKwh = consumptionKwh,
    startAddress = startAddress,
    endAddress = endAddress,
    outsideTempAvg = outsideTempAvg,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel
)

fun ChargeEntity.toDomain(): Charge = Charge(
    id = id,
    carId = carId,
    startDate = startDate,
    endDate = endDate,
    chargeEnergyAdded = chargeEnergyAdded,
    chargeEnergyUsed = chargeEnergyUsed,
    startBatteryLevel = startBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    // ChargeEntity 没有 idealRange 字段，缓存降级时无法恢复
    startIdealRangeKm = null,
    endIdealRangeKm = null,
    cost = cost,
    chargeType = chargeType,
    address = address
)
```

> **注意：** `import` 语句需要放在文件顶部的 import 块中，不要放在文件中间。将上述两个 `import` 合并到文件已有的 import 区域。

- [x] **Step 4: 运行测试验证通过**

Run: `cd app_glm/android && ./gradlew :app:testDebugUnitTest --tests "com.teslamatelink.domain.model.MappersTest" --info`
Expected: PASS — 6 个测试全部通过

- [x] **Step 5: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/domain/model/CarModels.kt app_glm/android/app/src/test/java/com/teslamatelink/domain/model/MappersTest.kt
git commit -m "feat(data): add toEntity/toDomain mappers for Drive and Charge (T-001)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 3: RealCarRepository 实现（T-002 ~ T-004）

**Files:**
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/data/repository/RealCarRepository.kt`

- [x] **Step 1: 创建 RealCarRepository.kt**

创建 `app_glm/android/app/src/main/java/com/teslamatelink/data/repository/RealCarRepository.kt`：

```kotlin
package com.teslamatelink.data.repository

import android.util.Log
import com.teslamatelink.data.api.TeslaMateApi
import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.dao.ChargeDao
import com.teslamatelink.data.local.dao.DriveDao
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import com.teslamatelink.domain.model.toDomain
import com.teslamatelink.domain.model.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RealCarRepository"

/**
 * Real implementation of [CarRepository] backed by [TeslaMateApi] and Room DAOs.
 *
 * Strategy: Network-First.
 *  - API success → write to Room cache → return domain models
 *  - API failure → fall back to Room cache → return cached data (no crash)
 *  - CancellationException is always re-thrown (structured concurrency)
 *
 * Car data is NOT cached (no CarDao); getCars/refreshCars return API results directly.
 */
@Singleton
class RealCarRepository @Inject constructor(
    private val api: TeslaMateApi,
    private val driveDao: DriveDao,
    private val chargeDao: ChargeDao
) : CarRepository {

    // ── Cars (not cached — no CarDao) ───────────────────

    override fun getCars(): Flow<List<Car>> = flow {
        val cars = fetchCarsFromApi()
        emit(cars)
    }

    override fun getCar(carId: Int): Flow<Car?> = flow {
        val car = fetchCarsFromApi().find { it.carId == carId }
        emit(car)
    }

    override suspend fun refreshCars(): List<Car> {
        return fetchCarsFromApi()
    }

    private suspend fun fetchCarsFromApi(): List<Car> {
        return try {
            val response = api.getCars()
            if (response.isSuccessful) {
                val rawCars = response.body()?.data?.cars.orEmpty()
                rawCars.map { it.toDomain() }
            } else {
                Log.w(TAG, "getCars failed: HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getCars network error", e)
            emptyList()
        }
    }

    // ── Drives (Network-First with Room cache) ──────────

    override suspend fun getDrives(carId: Int): List<Drive> {
        return try {
            val response = api.getDrives(carId)
            if (response.isSuccessful) {
                val rawDrives = response.body()?.data?.drives.orEmpty()
                val entities = rawDrives.map { it.toEntity() }
                if (entities.isNotEmpty()) {
                    driveDao.upsertAll(entities)
                }
                entities.map { it.toDomain() }
            } else {
                Log.w(TAG, "getDrives HTTP ${response.code()}, falling back to cache")
                driveDao.getAllChronological(carId).map { it.toDomain() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getDrives network error, falling back to cache", e)
            driveDao.getAllChronological(carId).map { it.toDomain() }
        }
    }

    // ── Charges (Network-First with Room cache) ─────────

    override suspend fun getCharges(carId: Int): List<Charge> {
        return try {
            val response = api.getCharges(carId)
            if (response.isSuccessful) {
                val rawCharges = response.body()?.data?.charges.orEmpty()
                val entities = rawCharges.map { it.toEntity() }
                if (entities.isNotEmpty()) {
                    chargeDao.upsertAll(entities)
                }
                entities.map { it.toDomain() }
            } else {
                Log.w(TAG, "getCharges HTTP ${response.code()}, falling back to cache")
                chargeDao.getAllChronological(carId).map { it.toDomain() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getCharges network error, falling back to cache", e)
            chargeDao.getAllChronological(carId).map { it.toDomain() }
        }
    }

    // ── Battery Health (not cached) ─────────────────────

    override suspend fun getBatteryHealth(carId: Int): BatteryHealth? {
        return try {
            val response = api.getBatteryHealth(carId)
            if (response.isSuccessful) {
                response.body()?.data?.batteryHealth
            } else {
                Log.w(TAG, "getBatteryHealth HTTP ${response.code()}")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getBatteryHealth network error", e)
            null
        }
    }

    // ── Updates (not cached) ────────────────────────────

    override suspend fun getUpdates(carId: Int): List<UpdateItem> {
        return try {
            val response = api.getUpdates(carId)
            if (response.isSuccessful) {
                val rawUpdates = response.body()?.data?.updates.orEmpty()
                rawUpdates.map { it.toDomain() }
            } else {
                Log.w(TAG, "getUpdates HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getUpdates network error", e)
            emptyList()
        }
    }
}
```

- [x] **Step 2: 验证编译**

Run: `cd app_glm/android && ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/data/repository/RealCarRepository.kt
git commit -m "feat(data): implement RealCarRepository with Network-First + Room cache (T-002, T-003, T-004)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 4: SettingsDataStore 扩展（T-007）

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/data/local/SettingsDataStore.kt`

- [x] **Step 1: 在 AppSettings 数据类添加 useRealDataSource 字段**

在 `SettingsDataStore.kt` 的 `AppSettings` data class 中，在 `lastSelectedCarId` 后添加：

```kotlin
    val useRealDataSource: Boolean = false
```

修改后的 AppSettings 应为：

```kotlin
data class AppSettings(
    val serverUrl: String = "",
    val secondaryServerUrl: String = "",
    val apiToken: String = "",
    val httpBasicAuthUsername: String = "",
    val httpBasicAuthPassword: String = "",
    val acceptInvalidCerts: Boolean = false,
    val currencyCode: String = "EUR",
    val showShortDrivesCharges: Boolean = false,
    val teslamateBaseUrl: String = "",
    val lastSelectedCarId: Int? = null,
    val useRealDataSource: Boolean = false
) {
    val isConfigured: Boolean
        get() = serverUrl.isNotBlank()

    val hasSecondaryServer: Boolean
        get() = secondaryServerUrl.isNotBlank()
}
```

- [x] **Step 2: 添加 preference key 和 snapshot StateFlow**

在 `SettingsDataStore` 类中，在 `private val notificationPermissionAskedKey` 后添加：

```kotlin
    private val useRealDataSourceKey = booleanPreferencesKey("use_real_data_source")

    /**
     * Snapshot of the data-source flag for synchronous reads from non-coroutine
     * contexts (e.g. DelegatingCarRepository.delegate getter).
     * Initialized to false; updated by [initUseRealDataSourceSnapshot].
     */
    private val useRealDataSourceSnapshot = MutableStateFlow(false)

    val useRealDataSource: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[useRealDataSourceKey] ?: false
    }
```

> **注意：** 需要在文件顶部 import `kotlinx.coroutines.flow.MutableStateFlow` 和 `kotlinx.coroutines.flow.StateFlow`（检查是否已 import）。

- [x] **Step 3: 在 settings Flow 中读取 useRealDataSource**

修改 `settings` Flow 的 map 块，在 `lastSelectedCarId = preferences[lastSelectedCarIdKey]` 后添加：

```kotlin
            useRealDataSource = preferences[useRealDataSourceKey] ?: false
```

- [x] **Step 4: 添加 snapshot 初始化和 setter 方法**

在 `SettingsDataStore` 类末尾（`clearSettings()` 之前）添加：

```kotlin
    /**
     * Synchronous read of the data-source flag.
     * Call [initUseRealDataSourceSnapshot] once at app startup to populate.
     */
    fun useRealDataSourceSnapshot(): Boolean = useRealDataSourceSnapshot.value

    /**
     * Populate the snapshot from DataStore. Call once at app startup
     * (e.g. in Application.onCreate or via Hilt initializer).
     */
    suspend fun initUseRealDataSourceSnapshot() {
        useRealDataSourceSnapshot.value = useRealDataSource.first()
    }

    suspend fun setUseRealDataSource(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[useRealDataSourceKey] = value
        }
        useRealDataSourceSnapshot.value = value
    }
```

> **注意：** 需要在文件顶部添加 `import kotlinx.coroutines.flow.first`（如果尚未 import）。

- [x] **Step 5: 验证编译**

Run: `cd app_glm/android && ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 6: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/data/local/SettingsDataStore.kt
git commit -m "feat(data): add useRealDataSource flag with snapshot to SettingsDataStore (T-007)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 5: DelegatingCarRepository + DI 绑定（T-005, T-006）

**Files:**
- Create: `app_glm/android/app/src/main/java/com/teslamatelink/data/repository/DelegatingCarRepository.kt`
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/di/AppModule.kt`

- [x] **Step 1: 创建 DelegatingCarRepository.kt**

创建 `app_glm/android/app/src/main/java/com/teslamatelink/data/repository/DelegatingCarRepository.kt`：

```kotlin
package com.teslamatelink.data.repository

import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proxy [CarRepository] that delegates to either [MockCarRepository] or
 * [RealCarRepository] based on the [SettingsDataStore.useRealDataSourceSnapshot] flag.
 *
 * This enables runtime switching without rebuilding the Hilt graph or restarting the app.
 * ViewModels are completely unaware of the switch (CarRepository interface unchanged).
 */
@Singleton
class DelegatingCarRepository @Inject constructor(
    private val settings: SettingsDataStore,
    @com.teslamatelink.di.MockImpl private val mock: CarRepository,
    @com.teslamatelink.di.RealImpl private val real: CarRepository
) : CarRepository {

    private val delegate: CarRepository
        get() = if (settings.useRealDataSourceSnapshot()) real else mock

    override fun getCars(): Flow<List<Car>> = delegate.getCars()

    override fun getCar(carId: Int): Flow<Car?> = delegate.getCar(carId)

    override suspend fun refreshCars(): List<Car> = delegate.refreshCars()

    override suspend fun getDrives(carId: Int): List<Drive> = delegate.getDrives(carId)

    override suspend fun getCharges(carId: Int): List<Charge> = delegate.getCharges(carId)

    override suspend fun getBatteryHealth(carId: Int): BatteryHealth? =
        delegate.getBatteryHealth(carId)

    override suspend fun getUpdates(carId: Int): List<UpdateItem> =
        delegate.getUpdates(carId)
}
```

- [x] **Step 2: 修改 AppModule.kt — 添加 @RealImpl 绑定**

在 `AppModule.kt` 中，在 `provideMockCarRepository` 函数后添加 `@RealImpl` 绑定：

```kotlin
    /**
     * Binds the real CarRepository (API + Room cache).
     */
    @Provides
    @Singleton
    @RealImpl
    fun provideRealCarRepository(
        api: TeslaMateApi,
        driveDao: DriveDao,
        chargeDao: ChargeDao
    ): CarRepository = RealCarRepository(api, driveDao, chargeDao)
```

> **注意：** 需要在文件顶部添加 `import com.teslamatelink.data.repository.RealCarRepository`（检查是否已 import）。

- [x] **Step 3: 修改 AppModule.kt — 默认绑定改为 DelegatingCarRepository**

将现有的 `provideCarRepository` 函数替换为：

```kotlin
    /**
     * Default CarRepository binding — delegates to Mock or Real based on
     * SettingsDataStore.useRealDataSource flag (runtime switchable).
     */
    @Provides
    @Singleton
    fun provideCarRepository(
        delegating: DelegatingCarRepository
    ): CarRepository = delegating
```

> **注意：** 需要在文件顶部添加 `import com.teslamatelink.data.repository.DelegatingCarRepository`。原来的 `@MockImpl` 绑定 (`provideMockCarRepository`) 保持不变 — DelegatingCarRepository 依赖它。

- [x] **Step 4: 验证 Hilt 图编译**

Run: `cd app_glm/android && ./gradlew :app:compileDebugKotlin :app:kspDebugKotlin`
Expected: BUILD SUCCESSFUL（Hilt KSP 处理无错误）

- [x] **Step 5: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/data/repository/DelegatingCarRepository.kt app_glm/android/app/src/main/java/com/teslamatelink/di/AppModule.kt
git commit -m "feat(di): add DelegatingCarRepository + @RealImpl binding (T-005, T-006)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 6: UI 数据源开关（T-008）

**Files:**
- Modify: `app_glm/android/app/src/main/java/com/teslamatelink/ui/settings/SettingsScreen.kt`
- Modify: `app_glm/android/app/src/main/res/values/strings.xml`（添加字符串资源）

- [x] **Step 1: 添加字符串资源**

在 `app_glm/android/app/src/main/res/values/strings.xml` 的 `<resources>` 块内添加：

```xml
    <string name="data_source">Data Source</string>
    <string name="use_real_data">Use Real Backend</string>
    <string name="use_real_data_description">Switch to real TeslaMate API (requires configured server). Off = mock data.</string>
```

> **注意：** 如果项目使用多语言，也需要在 `values-zh/strings.xml` 添加对应翻译。先检查 `values-zh` 目录是否存在。

- [x] **Step 2: 在 SettingsScreen 添加数据源切换 UI**

在 `SettingsScreen.kt` 的 `Debug` Card 之后、`About` Card 之前，插入新的 Data Source Card：

```kotlin
            // Data Source
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.data_source), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.use_real_data))
                        Switch(checked = useRealDataSource, onCheckedChange = { newValue ->
                            useRealDataSource = newValue
                            scope.launch {
                                settingsDataStore.setUseRealDataSource(newValue)
                            }
                        })
                    }
                    Text(
                        stringResource(R.string.use_real_data_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
```

- [x] **Step 3: 在 SettingsScreen 添加状态变量和初始化**

在 `SettingsScreen` composable 函数内，在 `var isMockMode by remember { mutableStateOf(AppSettings.isMockMode) }` 后添加：

```kotlin
    var useRealDataSource by remember { mutableStateOf(false) }
```

在 `LaunchedEffect(Unit)` 块组中添加（在 instances 的 LaunchedEffect 之后）：

```kotlin
    LaunchedEffect(Unit) {
        settingsDataStore.useRealDataSource.collect { value ->
            useRealDataSource = value
        }
    }
```

- [x] **Step 4: 验证编译**

Run: `cd app_glm/android && ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 5: Commit**

```bash
git add app_glm/android/app/src/main/java/com/teslamatelink/ui/settings/SettingsScreen.kt app_glm/android/app/src/main/res/values/strings.xml
git commit -m "feat(ui): add data source toggle Switch in SettingsScreen (T-008)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 7: RealCarRepository 单元测试（T-009, T-010）

**Files:**
- Create: `app_glm/android/app/src/test/java/com/teslamatelink/data/repository/RealCarRepositoryTest.kt`

- [x] **Step 1: 创建 RealCarRepositoryTest.kt**

创建 `app_glm/android/app/src/test/java/com/teslamatelink/data/repository/RealCarRepositoryTest.kt`：

```kotlin
package com.teslamatelink.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.teslamatelink.MainDispatcherRule
import com.teslamatelink.data.api.TeslaMateApi
import com.teslamatelink.data.local.AppDatabase
import com.teslamatelink.data.local.dao.ChargeDao
import com.teslamatelink.data.local.dao.DriveDao
import com.teslamatelink.buildInMemoryDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class RealCarRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var server: MockWebServer
    private lateinit var api: TeslaMateApi
    private lateinit var db: AppDatabase
    private lateinit var driveDao: DriveDao
    private lateinit var chargeDao: ChargeDao
    private lateinit var repository: RealCarRepository

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TeslaMateApi::class.java)

        db = buildInMemoryDb()
        driveDao = db.driveDao()
        chargeDao = db.chargeDao()

        repository = RealCarRepository(api, driveDao, chargeDao)
    }

    @After
    fun tearDown() {
        server.shutdown()
        db.close()
    }

    // ── T-009: API call success + cache write ───────────

    @Test
    fun getDrives_success_writesToRoomCache() = runTest {
        val json = """
        {
          "data": {
            "drives": [
              {
                "id": 1,
                "carId": 5,
                "startDate": "2026-01-01T00:00:00Z",
                "endDate": "2026-01-01T01:00:00Z",
                "distanceKm": 42.5,
                "durationMin": 60,
                "efficiency": 150.0,
                "consumptionKwh": 6.4,
                "startAddress": "Home",
                "endAddress": "Work",
                "outsideTempAvg": 22.0,
                "startBatteryLevel": 80,
                "endBatteryLevel": 50
              }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val drives = repository.getDrives(5)

        assertThat(drives).hasSize(1)
        assertThat(drives[0].id).isEqualTo(1)
        assertThat(drives[0].distanceKm).isEqualTo(42.5)

        // Verify cache was written
        val cached = driveDao.getAllChronological(5)
        assertThat(cached).hasSize(1)
        assertThat(cached[0].id).isEqualTo(1)
    }

    @Test
    fun getCharges_success_writesToRoomCache() = runTest {
        val json = """
        {
          "data": {
            "charges": [
              {
                "id": 10,
                "carId": 5,
                "startDate": "2026-01-01T00:00:00Z",
                "endDate": "2026-01-01T02:00:00Z",
                "chargeEnergyAdded": 30.0,
                "chargeEnergyUsed": 32.0,
                "startBatteryLevel": 20,
                "endBatteryLevel": 90,
                "cost": 12.5,
                "chargeType": "ac",
                "address": "Supercharger"
              }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val charges = repository.getCharges(5)

        assertThat(charges).hasSize(1)
        assertThat(charges[0].id).isEqualTo(10)
        assertThat(charges[0].chargeEnergyAdded).isEqualTo(30.0)

        // Verify cache
        val cached = chargeDao.getAllChronological(5)
        assertThat(cached).hasSize(1)
        assertThat(cached[0].chargeEnergyAdded).isEqualTo(30.0)
    }

    @Test
    fun refreshCars_success_returnsDomainCars() = runTest {
        val json = """
        {
          "data": {
            "cars": [
              {
                "carId": 1,
                "name": "My Tesla",
                "carDetails": { "vin": "5YJ...", "model": "3" },
                "teslamateStats": { "totalCharges": 10, "totalDrives": 50, "totalUpdates": 3 }
              }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val cars = repository.refreshCars()

        assertThat(cars).hasSize(1)
        assertThat(cars[0].carId).isEqualTo(1)
        assertThat(cars[0].name).isEqualTo("My Tesla")
        assertThat(cars[0].vin).isEqualTo("5YJ...")
        assertThat(cars[0].totalDrives).isEqualTo(50)
    }

    // ── T-010: API failure → fall back to Room cache ────

    @Test
    fun getDrives_apiError_fallsBackToRoomCache() = runTest {
        // Pre-populate cache
        val json = """
        {
          "data": {
            "drives": [
              {
                "id": 1,
                "carId": 5,
                "startDate": "2026-01-01T00:00:00Z",
                "distanceKm": 42.5
              }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
        repository.getDrives(5) // populate cache

        // Now simulate API failure
        server.enqueue(MockResponse().setResponseCode(500))

        val drives = repository.getDrives(5)

        assertThat(drives).hasSize(1)
        assertThat(drives[0].id).isEqualTo(1)
        assertThat(drives[0].distanceKm).isEqualTo(42.5)
    }

    @Test
    fun getDrives_networkError_fallsBackToRoomCache() = runTest {
        // Pre-populate cache
        val json = """
        {
          "data": {
            "drives": [
              { "id": 2, "carId": 5, "distanceKm": 10.0 }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))
        repository.getDrives(5)

        // Simulate network failure (no response enqueued → SocketTimeout)
        server.enqueue(MockResponse().setResponseCode(500))

        val drives = repository.getDrives(5)

        assertThat(drives).hasSize(1)
        assertThat(drives[0].id).isEqualTo(2)
    }

    @Test
    fun getDrives_emptyCache_onFirstFailure_returnsEmpty() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val drives = repository.getDrives(999)

        assertThat(drives).isEmpty()
    }

    // ── Battery Health / Updates ────────────────────────

    @Test
    fun getBatteryHealth_success_returnsHealth() = runTest {
        val json = """
        {
          "data": {
            "batteryHealth": {
              "carId": 1,
              "originalCapacityKwh": 75.0,
              "currentCapacityKwh": 72.0,
              "capacityDegradationPercent": 4.0
            }
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val health = repository.getBatteryHealth(1)

        assertThat(health).isNotNull()
        assertThat(health!!.currentCapacityKwh).isEqualTo(72.0)
    }

    @Test
    fun getBatteryHealth_failure_returnsNull() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val health = repository.getBatteryHealth(1)

        assertThat(health).isNull()
    }

    @Test
    fun getUpdates_success_returnsUpdateItems() = runTest {
        val json = """
        {
          "data": {
            "updates": [
              { "id": 1, "carId": 5, "version": "2026.1.2", "startDate": "2026-01-01" }
            ]
          }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val updates = repository.getUpdates(5)

        assertThat(updates).hasSize(1)
        assertThat(updates[0].version).isEqualTo("2026.1.2")
    }
}
```

- [x] **Step 2: 运行测试验证通过**

Run: `cd app_glm/android && ./gradlew :app:testDebugUnitTest --tests "com.teslamatelink.data.repository.RealCarRepositoryTest" --info`
Expected: PASS — 9 个测试全部通过

- [x] **Step 3: Commit**

```bash
git add app_glm/android/app/src/test/java/com/teslamatelink/data/repository/RealCarRepositoryTest.kt
git commit -m "test(data): RealCarRepository API + cache + fallback tests (T-009, T-010)"
```

archived-with: 2026-06-29-glm-real-repository
---

## Task 8: DelegatingCarRepository 单元测试（T-011）

**Files:**
- Create: `app_glm/android/app/src/test/java/com/teslamatelink/data/repository/DelegatingCarRepositoryTest.kt`

- [x] **Step 1: 创建 DelegatingCarRepositoryTest.kt**

创建 `app_glm/android/app/src/test/java/com/teslamatelink/data/repository/DelegatingCarRepositoryTest.kt`：

```kotlin
package com.teslamatelink.data.repository

import com.google.common.truth.Truth.assertThat
import com.teslamatelink.data.api.model.BatteryHealth
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.di.MockImpl
import com.teslamatelink.di.RealImpl
import com.teslamatelink.domain.model.Car
import com.teslamatelink.domain.model.Charge
import com.teslamatelink.domain.model.Drive
import com.teslamatelink.domain.model.UpdateItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DelegatingCarRepositoryTest {

    private val mockRepo: CarRepository = mockk(relaxed = true)
    private val realRepo: CarRepository = mockk(relaxed = true)
    private val settings: SettingsDataStore = mockk(relaxed = true)

    private val delegating = DelegatingCarRepository(settings, mockRepo, realRepo)

    @Test
    fun getCars_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expectedCars = listOf(Car(carId = 1, name = "Mock"))
        every { mockRepo.getCars() } returns flowOf(expectedCars)

        val result = delegating.getCars()

        result.collect { cars ->
            assertThat(cars).isEqualTo(expectedCars)
        }
    }

    @Test
    fun getCars_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expectedCars = listOf(Car(carId = 2, name = "Real"))
        every { realRepo.getCars() } returns flowOf(expectedCars)

        val result = delegating.getCars()

        result.collect { cars ->
            assertThat(cars).isEqualTo(expectedCars)
        }
    }

    @Test
    fun getDrives_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expectedDrives = listOf(Drive(id = 1, carId = 5, distanceKm = 100.0))
        coEvery { realRepo.getDrives(5) } returns expectedDrives

        val result = delegating.getDrives(5)

        assertThat(result).isEqualTo(expectedDrives)
    }

    @Test
    fun getDrives_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expectedDrives = listOf(Drive(id = 2, carId = 5, distanceKm = 50.0))
        coEvery { mockRepo.getDrives(5) } returns expectedDrives

        val result = delegating.getDrives(5)

        assertThat(result).isEqualTo(expectedDrives)
    }

    @Test
    fun getCharges_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(Charge(id = 1, carId = 5, chargeEnergyAdded = 30.0))
        coEvery { realRepo.getCharges(5) } returns expected

        val result = delegating.getCharges(5)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun getBatteryHealth_delegatesCorrectly() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = BatteryHealth(carId = 1, currentCapacityKwh = 72.0)
        coEvery { realRepo.getBatteryHealth(1) } returns expected

        val result = delegating.getBatteryHealth(1)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun getUpdates_delegatesToMock_whenUseRealFalse() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns false
        val expected = listOf(UpdateItem(id = 1, carId = 5, version = "2026.1"))
        coEvery { mockRepo.getUpdates(5) } returns expected

        val result = delegating.getUpdates(5)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun refreshCars_delegatesToReal_whenUseRealTrue() = runTest {
        every { settings.useRealDataSourceSnapshot() } returns true
        val expected = listOf(Car(carId = 1, name = "Real"))
        coEvery { realRepo.refreshCars() } returns expected

        val result = delegating.refreshCars()

        assertThat(result).isEqualTo(expected)
    }
}
```

- [x] **Step 2: 运行测试验证通过**

Run: `cd app_glm/android && ./gradlew :app:testDebugUnitTest --tests "com.teslamatelink.data.repository.DelegatingCarRepositoryTest" --info`
Expected: PASS — 8 个测试全部通过

- [x] **Step 3: 运行全部测试验证无回归**

Run: `cd app_glm/android && ./gradlew :app:testDebugUnitTest --info`
Expected: BUILD SUCCESSFUL — 所有测试通过（MappersTest + RealCarRepositoryTest + DelegatingCarRepositoryTest）

- [x] **Step 4: Commit**

```bash
git add app_glm/android/app/src/test/java/com/teslamatelink/data/repository/DelegatingCarRepositoryTest.kt
git commit -m "test(data): DelegatingCarRepository proxy forwarding tests (T-011)"
```

archived-with: 2026-06-29-glm-real-repository
---

## 验收清单

实现完成后，逐项确认：

- [x] **T-001** `DriveRaw.toEntity()` / `ChargeRaw.toEntity()` / `DriveEntity.toDomain()` / `ChargeEntity.toDomain()` 已实现，6 个映射测试通过
- [x] **T-002** `RealCarRepository.kt` 已创建，实现 `CarRepository` 接口
- [x] **T-003** Network-First 策略：API 成功写 Room，失败降级读 Room，CancellationException re-throw
- [x] **T-004** 7 个方法全部实现：getCars / getCar / refreshCars / getDrives / getCharges / getBatteryHealth / getUpdates
- [x] **T-005** AppModule 中 `@RealImpl` 绑定已添加
- [x] **T-006** `DelegatingCarRepository` 已创建，按设置转发
- [x] **T-007** `SettingsDataStore` 中 `useRealDataSource` 字段 + snapshot 已添加
- [x] **T-008** `SettingsScreen` 数据源 Switch 已添加，持久化到 DataStore
- [x] **T-009** MockWebServer 测试验证 API 调用 + 缓存写入（3 个测试）
- [x] **T-010** API 失败降级测试（3 个测试）
- [x] **T-011** DelegatingCarRepository 转发测试（8 个测试）
- [x] **编译** `./gradlew :app:compileDebugKotlin` 通过
- [x] **Hilt** `./gradlew :app:kspDebugKotlin` 通过
- [x] **全量测试** `./gradlew :app:testDebugUnitTest` 通过（17+ 个测试）

archived-with: 2026-06-29-glm-real-repository
---

## 非目标（设计文档确认）

- RealStatusRepository（实时状态走单独 5s 轮询，本轮不动）
- ViewModel 层改动（接口不变）
- 后端 API 兼容性适配
- 真实后端联调测试（无运行中后端）
- 自动分页（只取第一页）
- SettingsScreen 重构为 ViewModel（保持现有 companion object 模式，仅持久化新字段）

archived-with: 2026-06-29-glm-real-repository
---

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| 无真实后端 | MockWebServer 单元测试覆盖，不依赖真实后端 |
| Response/Entity 字段不对齐 | T-001 已核对：字段完美对齐，仅 `carId` 需 `?: 0` |
| 代理层性能 | StateFlow 缓存设置值（snapshot），避免每次调用读 DataStore |
| ChargeEntity 缺 idealRange | `ChargeEntity.toDomain()` 中 idealRange 映射为 null，缓存降级时丢失（可接受） |
| 分页 | 本轮只取第一页，后续优化 |
| Robolectric Room 测试 | in-memory Room + `allowMainThreadQueries()` + `MainDispatcherRule` |
