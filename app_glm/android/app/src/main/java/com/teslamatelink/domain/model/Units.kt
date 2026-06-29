package com.teslamatelink.domain.model

/**
 * Unit preference model used by UnitFormatter.
 *
 * TODO: Consolidate with any per-car unit preference from the API.
 * Currently used as a standalone data class.
 */
data class Units(
    val isImperial: Boolean = false,
    val unitOfTemperature: String = "C", // "C" or "F"
    val unitOfPressure: String = "bar"   // "bar" or "psi"
)
