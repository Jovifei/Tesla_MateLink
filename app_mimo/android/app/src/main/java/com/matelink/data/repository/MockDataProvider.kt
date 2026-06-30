package com.matelink.data.repository

import com.matelink.data.api.models.BatteryHealth
import com.matelink.data.api.models.CarData
import com.matelink.data.api.models.CarDetails
import com.matelink.data.api.models.CarExterior
import com.matelink.data.api.models.CarGeodata
import com.matelink.data.api.models.CarSettings
import com.matelink.data.api.models.CarStatus
import com.matelink.data.api.models.CarStatusDetails
import com.matelink.data.api.models.CarVersions
import com.matelink.data.api.models.ChargeBatteryDetails
import com.matelink.data.api.models.ChargeData
import com.matelink.data.api.models.ChargeDetail
import com.matelink.data.api.models.ChargePoint
import com.matelink.data.api.models.ChargeRange
import com.matelink.data.api.models.ChargerDetails
import com.matelink.data.api.models.ChargingDetails
import com.matelink.data.api.models.ClimateDetails
import com.matelink.data.api.models.DriveBatteryDetails
import com.matelink.data.api.models.DriveData
import com.matelink.data.api.models.DriveDetail
import com.matelink.data.api.models.DriveOdometerDetails
import com.matelink.data.api.models.DrivePosition
import com.matelink.data.api.models.DriveRange
import com.matelink.data.api.models.DrivingDetails
import com.matelink.data.api.models.GlobalSettingsData
import com.matelink.data.api.models.TeslamateStats
import com.matelink.data.api.models.Units
import com.matelink.data.api.models.UpdateData
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Provides hardcoded sample data for mock mode.
 * All data is realistic and self-consistent for demo/testing purposes.
 */
object MockDataProvider {

    private fun isoNow(): String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    private fun isoPast(minutesAgo: Int): String =
        DateTimeFormatter.ISO_INSTANT.format(Instant.now().minusSeconds(minutesAgo * 60L))

    fun getCars(): List<CarData> = listOf(
        CarData(
            carId = 1,
            name = "Model 3 Performance",
            carDetails = CarDetails(
                model = "3",
                trim_badging = "P74D",
                vin = "5YJ3E1EA1LF000001",
                efficiency = 153.0
            ),
            carExterior = CarExterior(
                exterior_color = "Red",
                wheel_type = "WY18P",
                spoiler_type = "Carbon"
            ),
            carSettings = CarSettings(freeSupercharging = false),
            teslamateStats = TeslamateStats(total_charges = 247, total_drives = 892)
        )
    )

    fun getCarStatus(): CarStatus = CarStatus(
        display_name = "Model 3 Performance",
        state = "online",
        state_since = isoPast(5),
        odometer = 42350.7,
        carStatus = CarStatusDetails(
            healthy = true,
            locked = true,
            sentryMode = true,
            windowsOpen = false,
            doorsOpen = false,
            trunkOpen = false,
            frunkOpen = false,
            isUserPresent = false,
            centerDisplayState = "0"
        ),
        carGeodata = CarGeodata(
            geofence = "Home",
            latitude = 52.5200,
            longitude = 13.4050
        ),
        carVersions = CarVersions(
            version = "2024.38.7",
            updateAvailable = false
        ),
        drivingDetails = DrivingDetails(
            shiftState = null,
            speed = null,
            power = null,
            heading = null,
            elevation = 85
        ),
        climateDetails = ClimateDetails(
            isClimateOn = false,
            insideTemp = 21.5,
            outsideTemp = 14.2
        ),
        batteryDetails = com.matelink.data.api.models.BatteryDetails(
            batteryLevel = 72,
            usableBatteryLevel = 70,
            estBatteryRange = 310.0,
            ratedBatteryRange = 295.0,
            idealBatteryRange = 340.0
        ),
        chargingDetails = ChargingDetails(
            pluggedIn = true,
            chargingState = "stopped",
            chargeEnergyAdded = 12.5,
            chargeLimitSoc = 80,
            chargerPower = null,
            chargerActualCurrent = null,
            timeToFullCharge = null
        ),
        tpmsDetails = null
    )

    fun getCharges(): List<ChargeData> = listOf(
        ChargeData(
            chargeId = 1001,
            start_date = isoPast(1440),
            end_date = isoPast(1380),
            address = "Supercharger Berlin",
            charge_energy_added = 45.2,
            charge_energy_used = 48.1,
            cost = 0.0,
            duration_min = 60,
            duration_str = "1h 0m",
            battery_details = ChargeBatteryDetails(start_battery_level = 20, end_battery_level = 80),
            range_ideal = ChargeRange(start_range = 120.0, end_range = 410.0),
            range_rated = ChargeRange(start_range = 95.0, end_range = 320.0),
            outside_temp_avg = 18.5,
            odometer = 42000.0,
            latitude = 52.4500,
            longitude = 13.4000
        ),
        ChargeData(
            chargeId = 1002,
            start_date = isoPast(2880),
            end_date = isoPast(2820),
            address = "Home Wallbox",
            charge_energy_added = 22.8,
            charge_energy_used = 24.0,
            cost = 8.50,
            duration_min = 180,
            duration_str = "3h 0m",
            battery_details = ChargeBatteryDetails(start_battery_level = 45, end_battery_level = 80),
            range_ideal = ChargeRange(start_range = 270.0, end_range = 410.0),
            range_rated = ChargeRange(start_range = 210.0, end_range = 320.0),
            outside_temp_avg = 12.0,
            odometer = 41500.0,
            latitude = 52.5200,
            longitude = 13.4050
        ),
        ChargeData(
            chargeId = 1003,
            start_date = isoPast(4320),
            end_date = isoPast(4260),
            address = "Supercharger Potsdam",
            charge_energy_added = 38.5,
            charge_energy_used = 41.0,
            cost = 0.0,
            duration_min = 45,
            duration_str = "45m",
            battery_details = ChargeBatteryDetails(start_battery_level = 15, end_battery_level = 75),
            range_ideal = ChargeRange(start_range = 90.0, end_range = 380.0),
            range_rated = ChargeRange(start_range = 70.0, end_range = 300.0),
            outside_temp_avg = 22.0,
            odometer = 41000.0,
            latitude = 52.4000,
            longitude = 13.0600
        )
    )

    fun getChargeDetail(chargeId: Int): ChargeDetail = ChargeDetail(
        charge_id = chargeId,
        start_date = isoPast(1440),
        end_date = isoPast(1380),
        address = "Supercharger Berlin",
        charge_energy_added = 45.2,
        charge_energy_used = 48.1,
        cost = 0.0,
        duration_min = 60,
        duration_str = "1h 0m",
        battery_details = ChargeBatteryDetails(start_battery_level = 20, end_battery_level = 80),
        range_ideal = ChargeRange(start_range = 120.0, end_range = 410.0),
        range_rated = ChargeRange(start_range = 95.0, end_range = 320.0),
        outside_temp_avg = 18.5,
        odometer = 42000.0,
        latitude = 52.4500,
        longitude = 13.4000,
        charge_points = listOf(
            ChargePoint(date = isoPast(1440), battery_level = 20, charge_energy_added = 0.0,
                charger_details = ChargerDetails(charger_power = 150, charger_voltage = 400, charger_actual_current = 300, charger_phases = 0),
                outside_temp = 18.0),
            ChargePoint(date = isoPast(1410), battery_level = 45, charge_energy_added = 22.0,
                charger_details = ChargerDetails(charger_power = 150, charger_voltage = 400, charger_actual_current = 300, charger_phases = 0),
                outside_temp = 18.5),
            ChargePoint(date = isoPast(1380), battery_level = 80, charge_energy_added = 45.2,
                charger_details = ChargerDetails(charger_power = 80, charger_voltage = 390, charger_actual_current = 205, charger_phases = 0),
                outside_temp = 19.0)
        ),
        is_charging = false
    )

    fun getDrives(): List<DriveData> = listOf(
        DriveData(
            drive_id = 2001,
            start_date = isoPast(120),
            end_date = isoPast(90),
            start_address = "Home",
            end_address = "Office",
            odometer_details = DriveOdometerDetails(
                odometer_start = 42300.0,
                odometer_end = 42350.7,
                distance = 50.7
            ),
            duration_min = 30,
            duration_str = "30m",
            speed_max = 135,
            speed_avg = 45.2,
            power_max = 210,
            power_min = -15,
            battery_details = DriveBatteryDetails(start_battery_level = 75, end_battery_level = 68),
            range_ideal = DriveRange(start_range = 380.0, end_range = 330.0, range_diff = -50.0),
            range_rated = DriveRange(start_range = 300.0, end_range = 270.0, range_diff = -30.0),
            outside_temp_avg = 14.5,
            inside_temp_avg = 21.0,
            energy_consumed_net = 8.2,
            consumption_net = 162.0
        ),
        DriveData(
            drive_id = 2002,
            start_date = isoPast(1440),
            end_date = isoPast(1410),
            start_address = "Office",
            end_address = "Home",
            odometer_details = DriveOdometerDetails(
                odometer_start = 42250.0,
                odometer_end = 42300.0,
                distance = 50.0
            ),
            duration_min = 35,
            duration_str = "35m",
            speed_max = 120,
            speed_avg = 42.8,
            power_max = 195,
            power_min = -20,
            battery_details = DriveBatteryDetails(start_battery_level = 70, end_battery_level = 63),
            range_ideal = DriveRange(start_range = 350.0, end_range = 310.0, range_diff = -40.0),
            range_rated = DriveRange(start_range = 275.0, end_range = 250.0, range_diff = -25.0),
            outside_temp_avg = 16.0,
            inside_temp_avg = 22.0,
            energy_consumed_net = 7.8,
            consumption_net = 156.0
        )
    )

    fun getDriveDetail(driveId: Int): DriveDetail = DriveDetail(
        drive_id = driveId,
        start_date = isoPast(120),
        end_date = isoPast(90),
        start_address = "Home",
        end_address = "Office",
        odometer_details = DriveOdometerDetails(
            odometer_start = 42300.0,
            odometer_end = 42350.7,
            distance = 50.7
        ),
        duration_min = 30,
        duration_str = "30m",
        speed_max = 135,
        speed_avg = 45.2,
        power_max = 210,
        power_min = -15,
        battery_details = DriveBatteryDetails(start_battery_level = 75, end_battery_level = 68),
        range_ideal = DriveRange(start_range = 380.0, end_range = 330.0, range_diff = -50.0),
        range_rated = DriveRange(start_range = 300.0, end_range = 270.0, range_diff = -30.0),
        outside_temp_avg = 14.5,
        inside_temp_avg = 21.0,
        energy_consumed_net = 8.2,
        consumption_net = 162.0,
        drive_details = listOf(
            DrivePosition(date = isoPast(120), latitude = 52.5200, longitude = 13.4050,
                speed = 0, power = 0, battery_level = 75, elevation = 85),
            DrivePosition(date = isoPast(110), latitude = 52.5100, longitude = 13.3900,
                speed = 80, power = 120, battery_level = 73, elevation = 78),
            DrivePosition(date = isoPast(100), latitude = 52.5000, longitude = 13.3800,
                speed = 120, power = 180, battery_level = 71, elevation = 72),
            DrivePosition(date = isoPast(90), latitude = 52.4900, longitude = 13.3700,
                speed = 0, power = -15, battery_level = 68, elevation = 70)
        )
    )

    fun getBatteryHealth(): BatteryHealth = BatteryHealth(
        max_range = 450.0,
        current_range = 430.0,
        max_capacity = 75.0,
        current_capacity = 72.5,
        rated_efficiency = 153.0,
        battery_health_percentage = 96.7
    )

    fun getUpdates(): List<UpdateData> = listOf(
        UpdateData(id = 1, version = "2024.38.7", start_date = isoPast(10080), end_date = isoPast(10020)),
        UpdateData(id = 2, version = "2024.32.3", start_date = isoPast(20160), end_date = isoPast(20100)),
        UpdateData(id = 3, version = "2024.26.1", start_date = isoPast(30240), end_date = isoPast(30180))
    )

    fun getGlobalSettings(): GlobalSettingsData = GlobalSettingsData(
        settings = com.matelink.data.api.models.GlobalSettings(
            teslamate_urls = com.matelink.data.api.models.TeslamateUrls(
                base_url = "http://localhost:4000",
                grafana_url = "http://localhost:3000"
            ),
            teslamate_units = com.matelink.data.api.models.TeslamateUnits(
                unit_of_length = "km",
                unit_of_temperature = "°C"
            )
        )
    )

    fun getUnits(): Units = Units(
        unit_of_length = "km",
        unit_of_pressure = "bar",
        unit_of_temperature = "°C"
    )
}
