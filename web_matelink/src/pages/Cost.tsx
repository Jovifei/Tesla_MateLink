import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Charge } from '../api/types';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function Cost() {
  const {currentCarId} = useStore();
  const [charges,setCharges] = useState<Charge[]>([]);

  useEffect(()=>{ api.getCharges(currentCarId).then(setCharges); },[]);

  const monthly = new Map<string,{acCost:number;dcCost:number;acKwh:number;dcKwh:number;acCount:number;dcCount:number}>();
  charges.filter(c=>c.end_date).forEach(c=>{
    const m=c.start_date.slice(0,7); const e=monthly.get(m)||{acCost:0,dcCost:0,acKwh:0,dcKwh:0,acCount:0,dcCount:0};
    if(c.charge_type==='DC'){e.dcCost+=c.cost||0;e.dcKwh+=c.charge_energy_added;e.dcCount++;}
    else{e.acCost+=c.cost||0;e.acKwh+=c.charge_energy_added;e.acCount++;}
    monthly.set(m,e);
  });
  const chartData = [...monthly.entries()].sort(([a],[b])=>a.localeCompare(b)).map(([m,v])=>({month:m.slice(5),acCost:Math.round(v.acCost*100)/100,dcCost:Math.round(v.dcCost*100)/100,total:Math.round((v.acCost+v.dcCost)*100)/100}));
  const totalCost = chartData.reduce((s,d)=>s+d.total,0);
  const acTotal = chartData.reduce((s,d)=>s+d.acCost,0);
  const dcTotal = chartData.reduce((s,d)=>s+d.dcCost,0);

  // Per-location ranking
  const locMap = new Map<string,{cost:number;kwh:number;count:number}>();
  charges.filter(c=>c.end_date).forEach(c=>{
    const e=locMap.get(c.address)||{cost:0,kwh:0,count:0};
    e.cost+=c.cost||0; e.kwh+=c.charge_energy_added; e.count++;
    locMap.set(c.address,e);
  });
  const ranking = [...locMap.entries()].map(([addr,v])=>({addr,cost:v.cost,kwh:v.kwh,pricePerKwh:Math.round(v.cost/Math.max(v.kwh,0.1)*100)/100,count:v.count})).sort((a,b)=>a.pricePerKwh-b.pricePerKwh);

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Charging Cost</h2>

      <div className="grid grid-cols-3 gap-4">
        {[{l:'Total Cost',v:`¥${totalCost.toFixed(2)}`},{l:'Home AC',v:`¥${acTotal.toFixed(2)}`},{l:'Supercharger DC',v:`¥${dcTotal.toFixed(2)}`,c:'text-orange-500'}].map(({l,v,c})=>(
          <div key={l} className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center">
            <div className="text-xs text-gray-400">{l}</div><div className={`text-xl font-bold mt-1 ${c||'text-gray-900 dark:text-white'}`}>{v}</div>
          </div>
        ))}
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Monthly Cost Breakdown</h3>
        <ResponsiveContainer width="100%" height={250}>
          <BarChart data={chartData}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="month" tick={{fontSize:11}}/><YAxis tick={{fontSize:11}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Bar dataKey="acCost" stackId="a" fill="#3B82F6" radius={[0,0,0,0]} name="Home AC"/><Bar dataKey="dcCost" stackId="a" fill="#F59E0B" radius={[4,4,0,0]} name="Supercharger DC"/></BarChart>
        </ResponsiveContainer>
      </div>

      {/* Cost ranking */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700">
        <div className="px-5 py-2.5 border-b border-gray-100 dark:border-gray-700 text-xs font-semibold text-gray-400 uppercase">🏆 Location Ranking (¥/kWh)</div>
        <div className="divide-y divide-gray-100 dark:divide-gray-700">
          {ranking.map((r,i)=>(<div key={r.addr} className="flex items-center gap-3 px-5 py-2.5 text-sm"><span className="font-bold text-gray-300 dark:text-gray-600 w-6">{i+1}</span><span className="flex-1 truncate text-gray-700 dark:text-gray-300">{r.addr}</span><span className="font-medium text-green-600">¥{r.pricePerKwh}/kWh</span><span className="text-xs text-gray-400">{r.count}× · {r.kwh}kWh</span></div>))}
        </div>
      </div>
    </div>
  );
}
