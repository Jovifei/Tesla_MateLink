package com.teslamatelink.data.repository

import com.teslamatelink.data.api.model.CarRaw
import com.teslamatelink.data.api.model.CarStatusData

/**
 * Repository for TeslaMate API access.
 *
 * Provides one-shot suspend functions returning [ApiResult] wrappers.
 * Concrete implementation will be wired via Hilt.
 */
interface TeslamateRepository {
    suspend fun getCars(): ApiResult<List<CarRaw>>
    suspend fun getCarStatus(carId: Int): ApiResult<CarStatusData>
    suspend fun isCurrentChargeAvailable(carId: Int): Boolean
}
