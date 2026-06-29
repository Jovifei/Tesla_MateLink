import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Drive } from '../api/types';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Brush } from 'recharts';

type Tab = 'speed'|'power'|'altitude'|'temp'|'tires';

export function DriveDetail() {
  const { id } = useParams<{id:string}>();
  const nav = useNavigate();
  const {currentCarId} = useStore();
  const [drive,setDrive] = useState<Drive|null>(null);
  const [tab,setTab] = useState<Tab>('speed');
  const [loading,setLoading] = useState(true);

  useEffect(()=>{ api.getDrives(currentCarId).then(all=>{ const d=all.find(dd=>String(dd.id)===id); setDrive(d||null); setLoading(false); }); },[id,currentCarId]);
  if(loading) return <div className="animate-pulse text-gray-400 p-8">Loading Drive...</div>;
  if(!drive) return <div className="text-red-400 p-8">Drive not found. <button onClick={()=>nav('/drives')} className="underline">Back to list</button></div>;

  // Generate mock position data for curves
  const n = 30;
  const duration = drive.duration_min;
  const positions = Array.from({length:n},(_,i)=>{
    const t=Math.round(duration*i/n); const v=drive.distance_km/duration*60;
    return { time:t, speed:Math.max(0,Math.round(v+Math.sin(i*0.5)*v*0.4)), power:Math.round(20+Math.sin(i*0.3)*60+Math.random()*20), altitude:Math.round(50+Math.sin(i*0.2)*30+Math.cos(i*0.15)*20), inside_temp:Math.round(22+Math.sin(i*0.1)*3), outside_temp:drive.outside_temp_avg||28, fl:2.4+Math.random()*0.1, fr:2.5+Math.random()*0.1, rl:2.4+Math.random()*0.1, rr:2.5+Math.random()*0.1 };
  });

  const tabs:Tab[] = ['speed','power','altitude','temp','tires'];
  const tabLabels:Record<Tab,string> = { speed:'Speed (km/h)', power:'Power (kW)', altitude:'Altitude (m)', temp:'Temp (°C)', tires:'Tire Pressure (bar)' };
  const tabColors:Record<Tab,string> = { speed:'#3B82F6', power:'#F59E0B', altitude:'#10B981', temp:'#EF4444', tires:'#8B5CF6' };

  return (
    <div className="space-y-5">
      <button onClick={()=>nav('/drives')} className="text-sm text-blue-500 hover:underline flex items-center gap-1">← Back to Drives</button>

      {/* Header */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <div className="text-lg font-bold text-gray-900 dark:text-white">{drive.start_address} → {drive.end_address}</div>
        <div className="text-sm text-gray-400 mt-1">{new Date(drive.start_date).toLocaleString()} — {new Date(drive.end_date).toLocaleString()} · {drive.duration_min} min</div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-5 gap-3">
        {[ {l:'Distance',v:`${drive.distance_km} km`}, {l:'Avg Speed',v:`${Math.round(drive.distance_km/drive.duration_min*60)} km/h`}, {l:'Max Speed',v:`${Math.round(drive.distance_km/drive.duration_min*60*1.5)} km/h`}, {l:'Energy',v:`${drive.consumption_kWh.toFixed(1)} kWh`}, {l:'Efficiency',v:`${drive.efficiency} Wh/km`} ].map(({l,v})=>(
          <div key={l} className="bg-white dark:bg-gray-800 rounded-xl p-3 border border-gray-100 dark:border-gray-700 text-center">
            <div className="text-[10px] text-gray-400">{l}</div><div className="font-bold text-sm mt-1 text-gray-900 dark:text-white">{v}</div>
          </div>
        ))}
      </div>

      {/* Battery change */}
      <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 flex items-center gap-4">
        <span className="text-sm text-gray-400">Battery</span>
        <span className="font-bold text-gray-900 dark:text-white">{drive.start_battery_level}%</span>
        <div className="flex-1 bg-gray-200 dark:bg-gray-700 h-2 rounded-full"><div className="bg-blue-500 h-2 rounded-full" style={{width:`${100-(drive.start_battery_level-(drive.end_battery_level||0))-drive.end_battery_level}%`}}/></div>
        <span className="font-bold text-gray-900 dark:text-white">{drive.end_battery_level}%</span>
        <span className="text-xs text-red-400">-{drive.start_battery_level-(drive.end_battery_level||0)}%</span>
      </div>

      {/* Curve tabs */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <div className="flex gap-1 mb-3">
          {tabs.map(t=><button key={t} onClick={()=>setTab(t)} className={`px-3 py-1.5 text-xs rounded-md font-medium transition-colors ${tab===t?`text-white bg-${tab===tabLabels[tab]?'':'['+tabColors[t]+']'}`:'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'}`} style={tab===t?{backgroundColor:tabColors[t]}:{}}>{tabLabels[t]}</button>)}
        </div>
        <ResponsiveContainer width="100%" height={280}>
          {tab==='tires'?(
            <LineChart data={positions}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="time" tick={{fontSize:10}} label={{value:'min',position:'insideBottom',offset:-5,style:{fontSize:10}}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Brush/><Line type="monotone" dataKey="fl" stroke="#3B82F6" strokeWidth={1.5} dot={false} name="Front Left"/><Line type="monotone" dataKey="fr" stroke="#EF4444" strokeWidth={1.5} dot={false} name="Front Right"/><Line type="monotone" dataKey="rl" stroke="#10B981" strokeWidth={1.5} dot={false} name="Rear Left"/><Line type="monotone" dataKey="rr" stroke="#F59E0B" strokeWidth={1.5} dot={false} name="Rear Right"/></LineChart>
          ):tab==='temp'?(
            <LineChart data={positions}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="time" tick={{fontSize:10}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Brush/><Line type="monotone" dataKey="inside_temp" stroke="#EF4444" strokeWidth={1.5} dot={false} name="Inside"/><Line type="monotone" dataKey="outside_temp" stroke="#F59E0B" strokeWidth={1.5} dot={false} name="Outside"/></LineChart>
          ):(
            <LineChart data={positions}><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis dataKey="time" tick={{fontSize:10}}/><YAxis tick={{fontSize:10}}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}}/><Brush/><Line type="monotone" dataKey={tab} stroke={tabColors[tab]} strokeWidth={2} dot={false}/></LineChart>
          )}
        </ResponsiveContainer>
      </div>
    </div>
  );
}
