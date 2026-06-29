import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function RangePage() {
  const {currentCarId} = useStore();
  const [data,setData] = useState<{date:string;est:number;actual:number;diff:number;temp:number}[]>([]);

  useEffect(()=>{ api.getDrives(currentCarId).then(drives=>{
    setData(drives.filter(d=>d.distance_km>5).slice(0,30).map(d=>({
      date:d.start_date.slice(0,10),
      est:d.start_battery_level,
      actual:Math.round((d.end_battery_level||d.start_battery_level-(d.distance_km*d.efficiency/520))),
      diff:Math.round((d.start_battery_level-(d.end_battery_level||d.start_battery_level-(d.distance_km*d.efficiency/520)))*100)/100,
      temp:d.outside_temp_avg||25,
    })));
  }); },[]);

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Projected Range</h2>
      <p className="text-sm text-gray-400">Estimated vs actual battery consumption per trip</p>

      <div className="grid grid-cols-4 gap-3">
        {[{l:'Avg Diff',v:`${Math.round(data.reduce((s,d)=>s+Math.abs(d.diff),0)/Math.max(data.length,1)*100)/100} %`},{l:'Trips',v:String(data.length)},{l:'Avg Temp',v:`${Math.round(data.reduce((s,d)=>s+d.temp,0)/Math.max(data.length,1))}°C`},{l:'Accuracy',v:`${Math.round((1-data.reduce((s,d)=>s+Math.abs(d.diff),0)/Math.max(data.reduce((s,d)=>s+Math.abs(d.est-d.actual),0),1))*100)}%`}].map(({l,v})=>(
          <div key={l} className="bg-white dark:bg-gray-800 rounded-xl p-3 border border-gray-100 dark:border-gray-700 text-center">
            <div className="text-[10px] text-gray-400">{l}</div><div className="font-bold text-sm mt-1 text-gray-900 dark:text-white">{v}</div>
          </div>
        ))}
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Est vs Actual per Trip</h3>
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={[...data].reverse()}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="date" tick={{fontSize:10}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Line type="monotone" dataKey="est" stroke="#3B82F6" strokeWidth={2} dot={{r:3}} name="Est %"/><Line type="monotone" dataKey="actual" stroke="#10B981" strokeWidth={2} dot={{r:3}} name="Actual %"/></LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
