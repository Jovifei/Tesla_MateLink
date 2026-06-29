import m from '../mock_data.json';
import type { CarRaw, CarStatus, Drive, Charge, BatteryHealthData, UpdateItem } from './types';

const MOCK_CARS: CarRaw[] = m.cars as CarRaw[];
const MOCK_STATUS: Record<string,CarStatus> = m.status as any;
const MOCK_DRIVES: Drive[] = m.drives as Drive[];
const MOCK_CHARGES: Charge[] = m.charges as Charge[];
const MOCK_BH: Record<string,BatteryHealthData> = m.batteryHealth as any;
const MOCK_UPDATES: Record<string,UpdateItem[]> = m.updates as any;

function delay<T>(v:T, ms=300):Promise<T> { return new Promise(r=>setTimeout(()=>r(v),ms)); }

export const api = {
  getCars: async (): Promise<CarRaw[]> => delay(MOCK_CARS),
  getCarStatus: async (carId:number): Promise<CarStatus> => delay({...MOCK_STATUS[String(carId)],battery_level:Math.min(100,Math.max(10,(MOCK_STATUS[String(carId)].battery_level+(Math.random()-0.5)*2|0)))} as CarStatus,200),
  getDrives: async (_carId:number): Promise<Drive[]> => delay(MOCK_DRIVES.filter(d=>d.car_id===_carId)),
  getCharges: async (_carId:number): Promise<Charge[]> => delay(MOCK_CHARGES.filter(c=>c.car_id===_carId)),
  getBatteryHealth: async (carId:number): Promise<BatteryHealthData> => delay(MOCK_BH[String(carId)]),
  getUpdates: async (carId:number): Promise<UpdateItem[]> => delay(MOCK_UPDATES[String(carId)]||[]),
};
