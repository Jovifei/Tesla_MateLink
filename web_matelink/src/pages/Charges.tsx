import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Charge } from '../api/types';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function Charges() {
  const {currentCarId} = useStore();
  const [charges,setCharges] = useState<Charge[]>([]);
  const [loading,setLoading] = useState(true);
  const [filter,setFilter] = useState<'all'|'AC'|'DC'>('all');

  useEffect(()=>{ api.getCharges(currentCarId).then(c=>{setCharges(c);setLoading(false);}); },[currentCarId]);
  if(loading) return <div className="animate-pulse text-gray-400">Loading...</div>;

  const filtered = filter==='all'?charges:charges.filter(c=>c.charge_type===filter);
  const chartData = charges.filter(c=>c.end_date).map(c=>({name:new Date(c.start_date).toLocaleDateString('en',{month:'short',day:'numeric'}),kWh:c.charge_energy_added,cost:c.cost}));

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Charge History</h2>
        <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 rounded-lg p-1">
          {(['all','AC','DC'] as const).map(f=><button key={f} onClick={()=>setFilter(f)} className={`px-3 py-1 text-xs rounded-md font-medium transition-colors ${filter===f?'bg-white dark:bg-gray-700 text-blue-600 dark:text-blue-400 shadow-sm':'text-gray-500'}`}>{f==='all'?'All':f}</button>)}
        </div>
      </div>

      {/* Monthly chart */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Energy per Session</h3>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={chartData}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="name" tick={{fontSize:11}}/><YAxis tick={{fontSize:11}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Bar dataKey="kWh" fill="#3B82F6" radius={[4,4,0,0]}/></BarChart>
        </ResponsiveContainer>
      </div>

      {/* List */}
      <div className="space-y-2">
        {filtered.map(c=>(
          <div key={c.id} className={`bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 hover:shadow-md transition-shadow ${c.charge_type==='DC'?'border-l-4 border-l-orange-500':''}`}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="text-2xl">{c.charge_type==='DC'?'⚡':'🔌'}</span>
                <div>
                  <div className="font-medium text-gray-900 dark:text-white flex items-center gap-2">
                    {c.address} {c.charge_type==='DC'&&<span className="text-[10px] bg-orange-100 dark:bg-orange-900/30 text-orange-600 px-1.5 py-0.5 rounded">DC</span>}
                  </div>
                  <div className="text-xs text-gray-400">{new Date(c.start_date).toLocaleString()} {!c.end_date&&'· In progress...'}</div>
                </div>
              </div>
              <div className="text-right">
                <div className="font-bold text-gray-900 dark:text-white">{c.charge_energy_added} kWh</div>
                <div className="text-xs text-gray-400">{c.start_battery_level}% → {c.end_battery_level??'--'}% {c.cost>0&&`· ¥${c.cost.toFixed(2)}`}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
