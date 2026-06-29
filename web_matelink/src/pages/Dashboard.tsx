import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { CarStatus } from '../api/types';

const stateColors:Record<string,string> = { online:'bg-green-500', driving:'bg-blue-500', charging:'bg-orange-500', asleep:'bg-gray-400', offline:'bg-gray-600' };
const stateLabels:Record<string,string> = { online:'Online', driving:'Driving', charging:'Charging', asleep:'Asleep', offline:'Offline' };


export function Dashboard() {
  const {currentCarId, cars, setCarId} = useStore();
  const [status,setStatus] = useState<CarStatus|null>(null);
  const [loading,setLoading] = useState(true);
  const [showSwitcher,setShowSwitcher] = useState(false);
  const car = cars.find(c=>c.id===currentCarId)!;

  useEffect(()=>{ let active=true; const f=async()=>{ const s=await api.getCarStatus(currentCarId); if(active) { setStatus(s); setLoading(false); } }; f(); const i=setInterval(f,5000); return ()=>{active=false;clearInterval(i)}; },[currentCarId]);

  if(loading || !status) return <div className="flex items-center justify-center h-64 text-gray-400 animate-pulse">Loading Dashboard...</div>;

  return (
    <div className="space-y-5">
      {/* Top bar */}
      <div className="flex items-center justify-between">
        <button onClick={()=>setShowSwitcher(!showSwitcher)} className="flex items-center gap-2 group">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{car.name}</h1>
          <span className="text-gray-400 text-sm group-hover:text-blue-500">▼</span>
        </button>
        <div className="flex items-center gap-3">
          <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium text-white ${stateColors[status.state]}`}>
            <span className="w-2 h-2 rounded-full bg-white animate-pulse"/> {stateLabels[status.state]}
          </span>
          <span className="text-xs text-gray-400">{new Date(status.since).toLocaleTimeString()}</span>
        </div>
      </div>

      {/* Car switcher modal */}
      {showSwitcher && (
        <div className="absolute z-50 mt-2 w-72 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 p-2">
          {cars.map(c=>(
            <button key={c.id} onClick={()=>{setCarId(c.id);setShowSwitcher(false);}} className={`w-full text-left px-4 py-3 rounded-lg text-sm transition-colors ${c.id===currentCarId?'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400':'text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'}`}>
              <div className="font-medium">{c.name}</div>
              <div className="text-xs text-gray-400">{c.model} · {c.totalDrives} drives · {c.totalCharges} charges</div>
            </button>
          ))}
        </div>
      )}

      {/* Main cards */}
      <div className="grid grid-cols-3 gap-4">
        <Card title="Battery" value={`${status.battery_level}%`} sub={`${status.est_battery_range_km} km range`} color="blue">
          <div className="mt-2 w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5"><div className="bg-blue-500 h-2.5 rounded-full transition-all duration-1000" style={{width:`${status.battery_level}%`}}/></div>
        </Card>
        <Card title="Odometer" value={`${status.odometer.toLocaleString()} km`} sub="Total mileage" color="slate"/>
        <Card title="Location" value={status.latitude.toFixed(2)+', '+status.longitude.toFixed(2)} sub={`Heading: ${status.heading}°`} color="emerald"/>
      </div>

      {/* Charging card */}
      {status.state==='charging' && (
        <div className="bg-orange-50 dark:bg-orange-900/20 border border-orange-200 dark:border-orange-800 rounded-2xl p-5">
          <div className="flex items-center gap-3 mb-3"><ZapIcon/><span className="text-lg font-bold text-orange-700 dark:text-orange-400">Charging in Progress</span></div>
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div><div className="text-gray-500">Power</div><div className="font-bold text-lg">{status.charger_power} kW</div></div>
            <div><div className="text-gray-500">Added</div><div className="font-bold text-lg">{status.charge_energy_added} kWh</div></div>
            <div><div className="text-gray-500">Remaining</div><div className="font-bold text-lg">{Math.round(status.time_to_full_charge*60)} min</div></div>
          </div>
        </div>
      )}

      {/* 4 info cards */}
      <div className="grid grid-cols-4 gap-4">
        <MiniCard label="Inside Temp" value={`${status.inside_temp}°C`} icon="🌡️"/>
        <MiniCard label="Outside Temp" value={`${status.outside_temp}°C`} icon="☀️"/>
        <MiniCard label="Climate" value={status.is_climate_on?'ON':'OFF'} icon="💨" active={status.is_climate_on}/>
        <MiniCard label="Sentry" value={status.sentry_mode?'ARMED':'OFF'} icon="🛡️" active={status.sentry_mode}/>
      </div>

      {/* Tires */}
      {status.tire_pressure && (
        <div className="grid grid-cols-4 gap-4">
          <MiniCard label="Front Left" value={`${status.tire_pressure.front_left} bar`} icon="🛞"/>
          <MiniCard label="Front Right" value={`${status.tire_pressure.front_right} bar`} icon="🛞"/>
          <MiniCard label="Rear Left" value={`${status.tire_pressure.rear_left} bar`} icon="🛞"/>
          <MiniCard label="Rear Right" value={`${status.tire_pressure.rear_right} bar`} icon="🛞"/>
        </div>
      )}
    </div>
  );
}

function Card({title,value,sub,color,children}:{title:string;value:string;sub:string;color:string;children?:any}) {
  const clrs:Record<string,string>={blue:'border-blue-200 dark:border-blue-800',slate:'border-gray-200 dark:border-gray-700',emerald:'border-emerald-200 dark:border-emerald-800'};
  return <div className={`bg-white dark:bg-gray-800 rounded-2xl p-5 border ${clrs[color]||''} shadow-sm hover:shadow-md transition-shadow`}>
    <div className="text-xs text-gray-400 mb-1">{title}</div>
    <div className="text-2xl font-bold text-gray-900 dark:text-white">{value}</div>
    <div className="text-xs text-gray-400 mt-1">{sub}</div>
    {children}
  </div>;
}
function MiniCard({label,value,icon,active}:{label:string;value:string;icon:string;active?:boolean}) {
  return <div className={`bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 ${active?'ring-1 ring-green-400':''}`}>
    <span className="text-lg">{icon}</span>
    <div className="text-xs text-gray-400 mt-2">{label}</div>
    <div className="font-semibold text-sm text-gray-900 dark:text-white">{value}</div>
  </div>;
}
function ZapIcon() { return <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#F97316" strokeWidth="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>; }
