import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';


interface TimelineEvent { type:'drive'|'charge'|'park'|'sleep'; start:Date; end:Date; label:string; detail:string; id?:number; }

export function Timeline() {
  const {currentCarId} = useStore();
  const [events,setEvents] = useState<TimelineEvent[]>([]);
  const [range,setRange] = useState<'day'|'week'|'month'>('week');
  const [selected,setSelected] = useState<TimelineEvent|null>(null);

  useEffect(()=>{ Promise.all([api.getDrives(currentCarId),api.getCharges(currentCarId)]).then(([drives,charges])=>{
    const ev:TimelineEvent[] = [];
    drives.forEach(d=>ev.push({type:'drive',start:new Date(d.start_date),end:new Date(d.end_date),label:`${d.start_address} → ${d.end_address}`,detail:`${d.distance_km}km · ${d.efficiency}Wh/km`,id:d.id}));
    charges.filter(c=>c.end_date).forEach(c=>ev.push({type:'charge',start:new Date(c.start_date),end:new Date(c.end_date!),label:c.address,detail:`${c.charge_energy_added}kWh · ${c.charge_type==='DC'?'⚡':'🔌'}`,id:c.id}));
    ev.sort((a,b)=>b.start.getTime()-a.start.getTime());
    setEvents(ev);
  }); },[currentCarId]);

  const colors = { drive:'bg-blue-500', charge:'bg-orange-500', park:'bg-gray-300 dark:bg-gray-600', sleep:'bg-gray-200 dark:bg-gray-700' };
  const labels = { drive:'Drive', charge:'Charge', park:'Park', sleep:'Sleep' };

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Vehicle Timeline</h2>
        <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 rounded-lg p-1">
          {(['day','week','month'] as const).map(r=><button key={r} onClick={()=>setRange(r)} className={`px-3 py-1 text-xs rounded-md font-medium transition-colors ${range===r?'bg-white dark:bg-gray-700 text-blue-600 dark:text-blue-400 shadow-sm':'text-gray-500'}`}>{r==='day'?'Day':r==='week'?'Week':'Month'}</button>)}
        </div>
      </div>

      {/* Legend */}
      <div className="flex gap-4 text-xs">
        {Object.entries(colors).map(([k,c])=><div key={k} className="flex items-center gap-1.5"><span className={`w-3 h-3 rounded-full ${c}`}/>{labels[k as keyof typeof labels]}</div>)}
      </div>

      {/* Timeline */}
      <div className="relative pl-8 border-l-2 border-gray-200 dark:border-gray-700 space-y-1">
        {events.slice(0,range==='day'?10:range==='week'?30:60).map((e,i)=>{
          const dur=Math.round((e.end.getTime()-e.start.getTime())/60000);
          return (
            <div key={i} className={`relative pb-3 ${selected===e?'opacity-100':'opacity-90 hover:opacity-100'}`} onClick={()=>setSelected(selected===e?null:e)}>
              <span className={`absolute -left-[30px] top-1 w-4 h-4 rounded-full ${colors[e.type]} border-2 border-white dark:border-[#1A1A2E] ${selected===e?'ring-2 ring-blue-400':''}`}/>
              <div className="bg-white dark:bg-gray-800 rounded-lg p-3 border border-gray-100 dark:border-gray-700 cursor-pointer hover:shadow-md transition-shadow">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{e.type==='drive'?'🚗':e.type==='charge'?'⚡':'🅿️'}</span>
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">{e.label}</div>
                      <div className="text-xs text-gray-400">{e.start.toLocaleTimeString()} — {e.end.toLocaleTimeString()} · {dur} min</div>
                    </div>
                  </div>
                  <span className="text-xs text-gray-400">{e.detail}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
      {selected&&<div className="text-center text-xs text-blue-500">Click to view details (coming in real API)</div>}
    </div>
  );
}
