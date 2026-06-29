/**
 * Time-of-Use tariff configuration.
 * Defaults: peak ¥1.0, flat ¥0.7, valley ¥0.3 per kWh.
 * Hour mapping (default): valley [0,7), peak [9,12) + [17,22), flat otherwise.
 */
export interface TariffConfig {
  peak_price: number;
  flat_price: number;
  valley_price: number;
  currency_code: string;
}

export const DEFAULT_TARIFF_CONFIG: TariffConfig = {
  peak_price: 1.0,
  flat_price: 0.7,
  valley_price: 0.3,
  currency_code: "CNY"
};

/**
 * Tariff period time range (hour-of-day, 0-23 inclusive).
 * Used to map a given hour to its pricing tier.
 */
export interface TariffPeriod {
  start_hour: number;
  end_hour: number;
  tier: "peak" | "flat" | "valley";
}

export const DEFAULT_TARIFF_PERIODS: TariffPeriod[] = [
  { start_hour: 0, end_hour: 7, tier: "valley" },
  { start_hour: 7, end_hour: 9, tier: "flat" },
  { start_hour: 9, end_hour: 12, tier: "peak" },
  { start_hour: 12, end_hour: 17, tier: "flat" },
  { start_hour: 17, end_hour: 22, tier: "peak" },
  { start_hour: 22, end_hour: 24, tier: "flat" },
];
