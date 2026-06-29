import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';


interface Dest { name:string; count:number; totalKm:number; avgEff:number; }

export function Destinations() {
  const {currentCarId} = useStore();
  const [dests,setDests] = useState<Dest[]>([]);
  const [sort,setSort] = useState<'count'|'km'|'eff'>('count');

  useEffect(()=>{ api.getDrives(currentCarId).then(drives=>{
    const map=new Map<string,{count:number;totalKm:number;effSum:number}>();
    drives.forEach(d=>{
      ['start_address','end_address'].forEach(k=>{
        const addr=(d as any)[k] as string; if(!addr||addr.length<2) return;
        const e=map.get(addr)||{count:0,totalKm:0,effSum:0};
        e.count++; e.totalKm+=d.distance_km; e.effSum+=d.efficiency;
        map.set(addr,e);
      });
    });
    setDests([...map.entries()].map(([name,v])=>({name,count:v.count,totalKm:Math.round(v.totalKm),avgEff:Math.round(v.effSum/v.count)})).sort((a,b)=>b.count-a.count));
  }); },[]);

  const sorted = [...dests].sort((a,b)=>sort==='count'?b.count-a.count:sort==='km'?b.totalKm-a.totalKm:b.avgEff-a.avgEff);

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Top Destinations</h2>
        <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 rounded-lg p-1">
          {([{k:'count',l:'Visits'},{k:'km',l:'Distance'},{k:'eff',l:'Efficiency'}] as const).map(s=>
            <button key={s.k} onClick={()=>setSort(s.k)} className={`px-3 py-1 text-xs rounded-md font-medium transition-colors ${sort===s.k?'bg-white dark:bg-gray-700 text-blue-600 dark:text-blue-400 shadow-sm':'text-gray-500'}`}>{s.l}</button>
          )}
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 divide-y divide-gray-100 dark:divide-gray-700">
        {sorted.slice(0,20).map((d,i)=>(
          <div key={d.name} className="flex items-center gap-4 px-5 py-3 hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors">
            <span className="text-lg font-bold text-gray-300 dark:text-gray-600 w-8 text-right">{i+1}</span>
            <span className="text-xl">📍</span>
            <div className="flex-1 min-w-0">
              <div className="font-medium text-sm text-gray-900 dark:text-white truncate">{d.name}</div>
              <div className="text-xs text-gray-400">{d.count} visits</div>
            </div>
            <div className="text-right text-sm"><span className="font-medium text-gray-900 dark:text-white">{d.totalKm}</span> <span className="text-gray-400 text-xs">km</span></div>
            <div className="text-right text-sm w-20"><span className="font-medium" style={{color:d.avgEff<150?'#22c55e':d.avgEff<200?'#f59e0b':'#ef4444'}}>{d.avgEff}</span> <span className="text-gray-400 text-xs">Wh/km</span></div>
          </div>
        ))}
      </div>
    </div>
  );
}
