import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Charge } from '../api/types';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Brush } from 'recharts';

type Tab = 'power'|'voltage'|'temp';

export function ChargeDetail() {
  const { id } = useParams<{id:string}>();
  const nav = useNavigate();
  const {currentCarId} = useStore();
  const [charge,setCharge] = useState<Charge|null>(null);
  const [tab,setTab] = useState<Tab>('power');
  const [loading,setLoading] = useState(true);

  useEffect(()=>{ api.getCharges(currentCarId).then(all=>{ const c=all.find(cc=>String(cc.id)===id); setCharge(c||null); setLoading(false); }); },[id,currentCarId]);
  if(loading) return <div className="animate-pulse text-gray-400 p-8">Loading Charge...</div>;
  if(!charge) return <div className="text-red-400 p-8">Charge not found. <button onClick={()=>nav('/charges')} className="underline">Back to list</button></div>;

  const d = charge.end_date ? (new Date(charge.end_date).getTime() - new Date(charge.start_date).getTime())/60000 : null;
  const n = 30;
  const samples = Array.from({length:n},(_,i)=>({
    time:Math.round((d||45)*i/n),
    power:Math.round(charge.charge_type==='DC'?50+Math.sin(i*0.4)*80+Math.random()*15:8+Math.sin(i*0.5)*3+Math.random()*2),
    voltage:Math.round(charge.charge_type==='DC'?380+Math.sin(i*0.2)*20+Math.random()*10:230+Math.sin(i*0.3)*5+Math.random()*3),
    temp:Math.round(32+Math.sin(i*0.3)*5+Math.random()*3),
  }));

  const eff = charge.charge_energy_added/(charge.charge_energy_used||charge.charge_energy_added)*100;
  const tabs:Tab[]=['power','voltage','temp'];
  const tabLabels:Record<Tab,string>={power:'Power (kW)',voltage:'Voltage (V)',temp:'Temp (°C)'};
  const tabColors:Record<Tab,string>={power:'#F59E0B',voltage:'#3B82F6',temp:'#EF4444'};

  return (
    <div className="space-y-5">
      <button onClick={()=>nav('/charges')} className="text-sm text-blue-500 hover:underline flex items-center gap-1">← Back to Charges</button>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <div className="flex items-center gap-2 mb-2">
          <span className="text-2xl">{charge.charge_type==='DC'?'⚡':'🔌'}</span>
          <div className="text-lg font-bold text-gray-900 dark:text-white">{charge.address}</div>
          {charge.charge_type==='DC'&&<span className="text-[10px] bg-orange-100 dark:bg-orange-900/30 text-orange-600 px-1.5 py-0.5 rounded font-medium">DC FAST</span>}
        </div>
        <div className="text-sm text-gray-400">{new Date(charge.start_date).toLocaleString()} — {charge.end_date?new Date(charge.end_date).toLocaleString():'In progress...'} · {d?`${Math.round(d)} min`:''}</div>
      </div>

      <div className="grid grid-cols-4 gap-3">
        {[{l:'Energy Added',v:`${charge.charge_energy_added} kWh`},{l:'Cost',v:charge.cost>0?`¥${charge.cost.toFixed(2)}`:'Free'},{l:'Efficiency',v:`${eff.toFixed(1)}%`},{l:'Battery',v:`${charge.start_battery_level}% → ${charge.end_battery_level??'?'}%`}].map(({l,v})=>(
          <div key={l} className="bg-white dark:bg-gray-800 rounded-xl p-3 border border-gray-100 dark:border-gray-700 text-center">
            <div className="text-[10px] text-gray-400">{l}</div><div className="font-bold text-sm mt-1 text-gray-900 dark:text-white">{v}</div>
          </div>
        ))}
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <div className="flex gap-1 mb-3">
          {tabs.map(t=><button key={t} onClick={()=>setTab(t)} className="px-3 py-1.5 text-xs rounded-md font-medium transition-colors" style={tab===t?{backgroundColor:tabColors[t],color:'#fff'}:{color:'#6b7280'}}>{tabLabels[t]}</button>)}
        </div>
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={samples}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="time" tick={{fontSize:10}} label={{value:'min',position:'insideBottom',offset:-5,style:{fontSize:10}}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Brush/><Line type="monotone" dataKey={tab} stroke={tabColors[tab]} strokeWidth={2} dot={false}/></LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
