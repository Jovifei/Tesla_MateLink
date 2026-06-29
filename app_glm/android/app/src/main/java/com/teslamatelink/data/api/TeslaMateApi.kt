package com.teslamatelink.data.api

import com.teslamatelink.data.api.model.BatteryHealthResponse
import com.teslamatelink.data.api.model.BatteryHistoryResponse
import com.teslamatelink.data.api.model.CarResponse
import com.teslamatelink.data.api.model.CarStatusResponse
import com.teslamatelink.data.api.model.CarsResponse
import com.teslamatelink.data.api.model.ChargeDetailResponse
import com.teslamatelink.data.api.model.ChargesResponse
import com.teslamatelink.data.api.model.CommandResponse
import com.teslamatelink.data.api.model.CurrentChargeResponse
import com.teslamatelink.data.api.model.DriveDetailResponse
import com.teslamatelink.data.api.model.DrivesResponse
import com.teslamatelink.data.api.model.HealthzResponse
import com.teslamatelink.data.api.model.PingResponse
import com.teslamatelink.data.api.model.ReadyzResponse
import com.teslamatelink.data.api.model.UpdateDetailResponse
import com.teslamatelink.data.api.model.UpdatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the TeslaMate API (v1).
 *
 * All 16 endpoints covering:
 *   ping, cars, status, drives, charges,
 *   battery-health, updates, commands, health checks.
 */
interface TeslaMateApi {

    // ── Health / Ping ────────────────────────────────────
    //  1

    @GET("api/ping")
    suspend fun ping(): Response<PingResponse>

    //  2
    @GET("api/healthz")
    suspend fun healthz(): Response<HealthzResponse>

    //  3
    @GET("api/readyz")
    suspend fun readyz(): Response<ReadyzResponse>

    // ── Cars ─────────────────────────────────────────────

    //  4  List all registered cars
    @GET("api/v1/cars")
    suspend fun getCars(): Response<CarsResponse>

    //  5  Single car details
    @GET("api/v1/cars/{carId}")
    suspend fun getCar(
        @Path("carId") carId: Int
    ): Response<CarResponse>

    //  6  Real-time car status (state, battery, location, climate, …)
    @GET("api/v1/cars/{carId}/status")
    suspend fun getCarStatus(
        @Path("carId") carId: Int
    ): Response<CarStatusResponse>

    // ── Drives ───────────────────────────────────────────

    //  7  Paginated drive history
    @GET("api/v1/cars/{carId}/drives")
    suspend fun getDrives(
        @Path("carId") carId: Int,
        @Query("page") page: Int? = null,
        @Query("show") show: Int? = null
    ): Response<DrivesResponse>

    //  8  Single drive detail (includes positions)
    @GET("api/v1/cars/{carId}/drives/{driveId}")
    suspend fun getDriveDetail(
        @Path("carId") carId: Int,
        @Path("driveId") driveId: Int
    ): Response<DriveDetailResponse>

    // ── Charges ──────────────────────────────────────────

    //  9  Paginated charge history
    @GET("api/v1/cars/{carId}/charges")
    suspend fun getCharges(
        @Path("carId") carId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int? = null,
        @Query("show") show: Int? = null
    ): Response<ChargesResponse>

    // 10  Single charge detail (includes charge points)
    @GET("api/v1/cars/{carId}/charges/{chargeId}")
    suspend fun getChargeDetail(
        @Path("carId") carId: Int,
        @Path("chargeId") chargeId: Int
    ): Response<ChargeDetailResponse>

    // 11  Current active charge (returns 204 / error body when none)
    @GET("api/v1/cars/{carId}/charges/current")
    suspend fun getCurrentCharge(
        @Path("carId") carId: Int
    ): Response<CurrentChargeResponse>

    // ── Battery Health ───────────────────────────────────

    // 12  Battery health summary (degradation, capacity)
    @GET("api/v1/cars/{carId}/battery-health")
    suspend fun getBatteryHealth(
        @Path("carId") carId: Int
    ): Response<BatteryHealthResponse>

    // 13  Battery health history (time series of capacity / mileage)
    @GET("api/v1/cars/{carId}/battery-health/history")
    suspend fun getBatteryHistory(
        @Path("carId") carId: Int
    ): Response<BatteryHistoryResponse>

    // ── Software Updates ─────────────────────────────────

    // 14  Paginated update history
    @GET("api/v1/cars/{carId}/updates")
    suspend fun getUpdates(
        @Path("carId") carId: Int,
        @Query("page") page: Int? = null,
        @Query("show") show: Int? = null
    ): Response<UpdatesResponse>

    // 15  Single update detail
    @GET("api/v1/cars/{carId}/updates/{updateId}")
    suspend fun getUpdateDetail(
        @Path("carId") carId: Int,
        @Path("updateId") updateId: Int
    ): Response<UpdateDetailResponse>

    // ── Commands ─────────────────────────────────────────

    // 16  Wake up a sleeping car
    @POST("api/v1/cars/{carId}/command/wake")
    suspend fun wakeCar(
        @Path("carId") carId: Int
    ): Response<CommandResponse>
}
