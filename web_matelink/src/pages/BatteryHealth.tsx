import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { BatteryHealthData } from '../api/types';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function BatteryHealth() {
  const {currentCarId} = useStore();
  const [data,setData] = useState<BatteryHealthData|null>(null);
  const [loading,setLoading] = useState(true);

  useEffect(()=>{ api.getBatteryHealth(currentCarId).then(d=>{setData(d);setLoading(false);}); },[currentCarId]);
  if(loading||!data) return <div className="animate-pulse text-gray-400">Loading...</div>;

  const deg = data.capacity_degradation_percent;
  const status = deg<5?'Excellent':deg<10?'Good':deg<15?'Fair':'Poor';
  const statusColor = deg<5?'text-green-500':deg<10?'text-blue-500':deg<15?'text-orange-500':'text-red-500';

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Battery Health</h2>

      {/* Health ring */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-8 border border-gray-100 dark:border-gray-700 text-center">
        <div className="inline-flex items-center justify-center w-32 h-32 rounded-full border-[6px] border-blue-500 mb-4">
          <div><div className="text-3xl font-bold text-gray-900 dark:text-white">{100-deg}%</div><div className="text-xs text-gray-400">Health</div></div>
        </div>
        <div className={`text-lg font-semibold ${statusColor}`}>{status}</div>
        <div className="text-sm text-gray-400 mt-1">{data.mileage_km.toLocaleString()} km driven</div>
      </div>

      {/* Comparison cards */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700">
          <div className="text-xs text-gray-400">Original Capacity</div><div className="text-xl font-bold text-gray-900 dark:text-white">{data.original_capacity_kwh} kWh</div>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700">
          <div className="text-xs text-gray-400">Current Capacity</div><div className="text-xl font-bold text-gray-900 dark:text-white">{data.current_capacity_kwh} kWh <span className="text-sm text-red-400">-{data.capacity_degradation_percent}%</span></div>
        </div>
      </div>

      {/* Trend chart */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Degradation Trend</h3>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart data={data.history.map(h=>({...h,date:h.date.slice(0,7)}))}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/>
            <XAxis dataKey="date" tick={{fontSize:11}}/>
            <YAxis domain={['dataMin-1','dataMax+1']} tick={{fontSize:11}}/>
            <Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/>
            <Line type="monotone" dataKey="capacity_kwh" stroke="#3B82F6" strokeWidth={2} dot={{r:4}}/>
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
