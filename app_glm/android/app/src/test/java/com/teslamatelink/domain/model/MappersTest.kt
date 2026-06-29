package com.teslamatelink.domain.model

import com.google.common.truth.Truth.assertThat
import com.teslamatelink.data.api.model.ChargeRaw
import com.teslamatelink.data.api.model.DriveRaw
import com.teslamatelink.data.local.entity.ChargeEntity
import com.teslamatelink.data.local.entity.DriveEntity
import org.junit.Test

class MappersTest {

    // ── DriveRaw → DriveEntity ──────────────────────────────────────────

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

    // ── ChargeRaw → ChargeEntity ────────────────────────────────────────

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
        assertThat(entity.startDate).isEqualTo("2026-01-01T00:00:00Z")
        assertThat(entity.endDate).isEqualTo("2026-01-01T02:00:00Z")
        assertThat(entity.chargeEnergyAdded).isEqualTo(30.0)
        assertThat(entity.chargeEnergyUsed).isEqualTo(32.0)
        assertThat(entity.startBatteryLevel).isEqualTo(20)
        assertThat(entity.endBatteryLevel).isEqualTo(90)
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

    // ── DriveEntity → Drive (Domain) ────────────────────────────────────

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
        assertThat(domain.startDate).isEqualTo("2026-01-01T00:00:00Z")
        assertThat(domain.distanceKm).isEqualTo(42.5)
        assertThat(domain.durationMin).isEqualTo(60)
        assertThat(domain.consumptionKwh).isEqualTo(6.4)
        assertThat(domain.startBatteryLevel).isEqualTo(80)
    }

    // ── ChargeEntity → Charge (Domain) ──────────────────────────────────

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
        assertThat(domain.carId).isEqualTo(5)
        assertThat(domain.chargeEnergyAdded).isEqualTo(30.0)
        assertThat(domain.cost).isEqualTo(12.5)
        assertThat(domain.chargeType).isEqualTo("ac")
        // ChargeEntity does not store idealRange; null on cache fallback
        assertThat(domain.startIdealRangeKm).isNull()
        assertThat(domain.endIdealRangeKm).isNull()
    }
}
