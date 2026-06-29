import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export function Vampire() {
  const {currentCarId} = useStore();
  const [drains,setDrains] = useState<{date:string;kWh:number;km:number;temp:number}[]>([]);
  const [totalLoss,setTotalLoss] = useState(0);

  useEffect(()=>{ api.getDrives(currentCarId).then(drives=>{
    // Estimate vampire drain: battery % lost between drives that are >1h apart
    const sorted = [...drives].sort((a,b)=>new Date(a.start_date).getTime()-new Date(b.start_date).getTime());
    const drains:any[]=[]; let total=0;
    for(let i=1;i<sorted.length;i++){
      const prev=sorted[i-1]; const cur=sorted[i];
      const gap=(new Date(cur.start_date).getTime()-new Date(prev.end_date).getTime())/3600000;
      if(gap>1&&gap<48){
        const battLoss=prev.end_battery_level-cur.start_battery_level;
        if(battLoss>0){const kWh=Math.round(battLoss/100*75*10)/10; total+=kWh;
          drains.push({date:prev.end_date.slice(0,10),kWh,km:Math.round(battLoss/100*520),temp:cur.outside_temp_avg||25});}
      }
    }
    setDrains(drains); setTotalLoss(Math.round(total*10)/10);
  }); },[]);

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Vampire Drain</h2>
      <p className="text-sm text-gray-400">Estimated battery loss during parking periods</p>

      <div className="grid grid-cols-3 gap-4">
        {[{l:'Total Drain',v:`${totalLoss} kWh`},{l:'Range Loss',v:`${Math.round(totalLoss*6.9)} km`,c:'text-red-500'},{l:'Events',v:String(drains.length)}].map(({l,v,c})=>(
          <div key={l} className={`bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center`}>
            <div className="text-xs text-gray-400">{l}</div><div className={`text-xl font-bold mt-1 ${c||'text-gray-900 dark:text-white'}`}>{v}</div>
          </div>
        ))}
      </div>

      {drains.length>0&&<div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <h3 className="text-sm font-medium text-gray-400 mb-3">Daily Drain Trend</h3>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart data={drains.slice(0,30)}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="date" tick={{fontSize:10}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Line type="monotone" dataKey="kWh" stroke="#EF4444" strokeWidth={2} dot={{r:3}} name="kWh lost"/></LineChart>
        </ResponsiveContainer>
      </div>}
    </div>
  );
}
