import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Drive } from '../api/types';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function Statistics() {
  const {currentCarId} = useStore();
  const [drives,setDrives] = useState<Drive[]>([]);
  const [view,setView] = useState<'year'|'month'|'day'>('year');
  const [selectedMonth,setSelectedMonth] = useState<string|null>(null);

  useEffect(()=>{ api.getDrives(currentCarId).then(setDrives); },[currentCarId]);

  const months = Array.from({length:12},(_,i)=>{
    const m = String(i+1).padStart(2,'0');
    const monthDrives = drives.filter(d=>d.start_date.slice(0,7).endsWith(m));
    return {
      name: new Date(2026,i,1).toLocaleDateString('en',{month:'short'}),
      km: Math.round(monthDrives.reduce((s,d)=>s+d.distance_km,0)),
      kWh: Math.round(monthDrives.reduce((s,d)=>s+d.consumption_kWh,0)*10)/10,
      drives: monthDrives.length,
      eff: monthDrives.length?Math.round(monthDrives.reduce((s,d)=>s+d.efficiency,0)/monthDrives.length):0,
    };
  });
  const totalKm = months.reduce((s,m)=>s+m.km,0);
  const totalKwh = months.reduce((s,m)=>s+m.kWh,0);
  const totalDrives = months.reduce((s,m)=>s+m.drives,0);

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Statistics</h2>
        <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 rounded-lg p-1">
          {(['year','month','day'] as const).map(v=><button key={v} onClick={()=>setView(v)} className={`px-3 py-1 text-xs rounded-md font-medium transition-colors ${view===v?'bg-white dark:bg-gray-700 text-blue-600 dark:text-blue-400 shadow-sm':'text-gray-500'}`}>{v==='year'?'Year':v==='month'?'Month':'Day'}</button>)}
        </div>
      </div>
      <div className="grid grid-cols-3 gap-4">
        {[{l:'Total Distance',v:`${totalKm} km`},{l:'Total Energy',v:`${totalKwh} kWh`},{l:'Total Drives',v:String(totalDrives)}].map(({l,v})=>(
          <div key={l} className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center">
            <div className="text-xs text-gray-400">{l}</div><div className="text-xl font-bold text-gray-900 dark:text-white mt-1">{v}</div>
          </div>
        ))}
      </div>
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Monthly Distance</h3>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={months}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="name" tick={{fontSize:11}}/><YAxis tick={{fontSize:11}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Bar dataKey="km" fill="#3B82F6" radius={[4,4,0,0]}/></BarChart>
        </ResponsiveContainer>
      </div>
      <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
        {months.map(m=><button key={m.name} onClick={()=>{setSelectedMonth(m.name);setView('month');}} className="bg-white dark:bg-gray-800 rounded-xl p-3 border border-gray-100 dark:border-gray-700 hover:shadow-md hover:border-blue-300 dark:hover:border-blue-700 transition-all text-left">
          <div className="text-xs text-gray-400">{m.name}</div>
          <div className="font-bold text-sm text-gray-900 dark:text-white">{m.km} km</div>
          <div className="text-[10px] text-gray-400">{m.drives} drives · {m.eff} Wh/km</div>
        </button>)}
      </div>
      {selectedMonth&&<div className="text-center text-xs text-blue-500">Month→Day drilling coming soon · Selected: {selectedMonth}</div>}
    </div>
  );
}
