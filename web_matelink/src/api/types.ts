export interface CarRaw {
  car_id: number; name: string;
  car_details: { eid: number; vid: number; vin: string; model: string; trim_badging: string; efficiency: number };
  car_exterior: { exterior_color: string; spoiler_type: string; wheel_type: string };
  car_settings: { suspend_min: number; suspend_after_idle_min: number; req_not_unlocked: boolean; free_supercharging: boolean; use_streaming_api: boolean };
  teslamate_details: { inserted_at: string; updated_at: string };
  teslamate_stats: { total_charges: number; total_drives: number; total_updates: number };
}
export interface Car { id: number; name: string; model: string; trim: string; color: string; wheel: string; efficiency: number; totalCharges: number; totalDrives: number; totalUpdates: number }
export type CarState = 'online' | 'offline' | 'asleep' | 'charging' | 'driving';
export interface CarStatus {
  car_id: number; state: CarState; since: string; healthy: boolean;
  odometer: number; battery_level: number; usable_battery_level: number;
  charge_energy_added: number; charge_limit_soc: number; ideal_battery_range_km: number; est_battery_range_km: number;
  charger_power: number; charger_actual_current: number; charger_voltage: number;
  charge_port_door_open: boolean; time_to_full_charge: number;
  inside_temp: number; outside_temp: number; is_climate_on: boolean;
  latitude: number; longitude: number; heading: number; speed: number; shift_state: string;
  locked: boolean; sentry_mode: boolean; car_version: string;
  tire_pressure?: { front_left: number; front_right: number; rear_left: number; rear_right: number };
}
export interface Drive { id: number; car_id: number; start_date: string; end_date: string; distance_km: number; duration_min: number; efficiency: number; consumption_kWh: number; start_address: string; end_address: string; outside_temp_avg: number; start_battery_level: number; end_battery_level: number }
export interface Charge { id: number; car_id: number; start_date: string; end_date: string|null; charge_energy_added: number; charge_energy_used: number; start_battery_level: number; end_battery_level: number|null; start_ideal_range_km: number; end_ideal_range_km: number|null; cost: number; charge_type: string; address: string }
export interface BatteryHealthData { car_id: number; original_capacity_kwh: number; current_capacity_kwh: number; capacity_degradation_percent: number; original_range_km: number; current_range_km: number; range_loss_percent: number; mileage_km: number; history: { date: string; capacity_kwh: number; mileage_km: number }[] }
export interface UpdateItem { id: number; car_id: number; start_date: string; end_date: string; version: string }

export function flattenCar(r: CarRaw): Car {
  return { id: r.car_id, name: r.name, model: r.car_details.model, trim: r.car_details.trim_badging, color: r.car_exterior.exterior_color, wheel: r.car_exterior.wheel_type, efficiency: r.car_details.efficiency, totalCharges: r.teslamate_stats.total_charges, totalDrives: r.teslamate_stats.total_drives, totalUpdates: r.teslamate_stats.total_updates };
}
