package com.teslamatelink.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.teslamatelink.BuildConfig
import com.teslamatelink.data.api.ApiInterceptor
import com.teslamatelink.data.api.TeslaMateApi
import com.teslamatelink.data.local.AppDatabase
import com.teslamatelink.data.local.SettingsDataStore
import com.teslamatelink.data.local.dao.ChargeDao
import com.teslamatelink.data.local.dao.DriveDao
import com.teslamatelink.data.repository.CarRepository
import com.teslamatelink.data.repository.DelegatingCarRepository
import com.teslamatelink.data.repository.MockCarRepository
import com.teslamatelink.data.repository.MockStatusRepository
import com.teslamatelink.data.repository.RealCarRepository
import com.teslamatelink.data.repository.StatusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotation for mock implementations.
 * Use @MockImpl to inject mock data sources.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockImpl

/**
 * Qualifier annotation for real (API-backed) implementations.
 * Use @RealImpl to inject real data sources.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RealImpl

/**
 * OkHttp interceptor that rewrites each request's base URL to the value
 * stored in SettingsDataStore.  This lets Retrofit work with a hardcoded
 * placeholder while the user can change the server at runtime.
 */
@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val configuredUrl = runBlocking {
            settingsDataStore.settings.firstOrNull()?.serverUrl
        }
        if (configuredUrl.isNullOrBlank()) return chain.proceed(originalRequest)

        val newHost = configuredUrl.toHttpUrlOrNull() ?: return chain.proceed(originalRequest)
        val newUrl = originalRequest.url.newBuilder()
            .scheme(newHost.scheme)
            .host(newHost.host)
            .port(newHost.port)
            .build()
        return chain.proceed(originalRequest.newBuilder().url(newUrl).build())
    }
}

/**
 * Hilt module providing application-wide singletons.
 *
 * Defaults to the mock data source. Swap bindings to use @RealImpl
 * when a real API backend is available.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // -- Gson --

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    // -- OkHttp --

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiInterceptor: ApiInterceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(apiInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            this.level = level
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // -- Retrofit --

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:4000/") // Default -- override at runtime
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideTeslaMateApi(retrofit: Retrofit): TeslaMateApi =
        retrofit.create(TeslaMateApi::class.java)

    // -- Room --

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).addMigrations(*AppDatabase.ALL_MIGRATIONS).build()

    @Provides
    fun provideDriveDao(db: AppDatabase): DriveDao = db.driveDao()

    @Provides
    fun provideChargeDao(db: AppDatabase): ChargeDao = db.chargeDao()

    // -- Repositories (Mock by default) --

    /**
     * Binds the mock CarRepository implementation.
     */
    @Provides
    @Singleton
    @MockImpl
    fun provideMockCarRepository(
        mockImpl: MockCarRepository
    ): CarRepository = mockImpl

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

    /**
     * Default CarRepository binding — delegates to Mock or Real based on
     * SettingsDataStore.useRealDataSource flag (runtime switchable).
     */
    @Provides
    @Singleton
    fun provideCarRepository(
        delegating: DelegatingCarRepository
    ): CarRepository = delegating

    /**
     * Binds the mock StatusRepository as the default.
     */
    @Provides
    @Singleton
    @MockImpl
    fun provideMockStatusRepository(
        mockImpl: MockStatusRepository
    ): StatusRepository = mockImpl

    /**
     * Default StatusRepository binding (uses mock).
     */
    @Provides
    @Singleton
    fun provideStatusRepository(
        @MockImpl mockImpl: StatusRepository
    ): StatusRepository = mockImpl
}
