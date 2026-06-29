import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { UpdateItem } from '../api/types';

export function Updates() {
  const {currentCarId} = useStore();
  const [updates,setUpdates] = useState<UpdateItem[]>([]);

  useEffect(()=>{ api.getUpdates(currentCarId).then(setUpdates); },[currentCarId]);

  const longest = updates.reduce((max,u)=> {
    const d=(new Date(u.end_date).getTime()-new Date(u.start_date).getTime())/86400000;
    return d>(max?.days||0)?{...u,days:Math.round(d)}:max;
  }, null as (UpdateItem&{days:number})|null);

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Firmware Updates</h2>
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center">
          <div className="text-2xl font-bold text-gray-900 dark:text-white">{updates.length}</div>
          <div className="text-xs text-gray-400">Total Updates</div>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center">
          <div className="text-2xl font-bold text-blue-500">{updates[0]?.version||'—'}</div>
          <div className="text-xs text-gray-400">Latest Version</div>
        </div>
        {longest&&<div className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 text-center">
          <div className="text-2xl font-bold text-amber-500 flex items-center justify-center gap-1">{longest.days}<span className="text-xs font-normal text-gray-400">days</span></div>
          <div className="text-xs text-gray-400">Longest Run · {longest.version} 🏆</div>
        </div>}
      </div>
      <div className="space-y-2">
        {updates.map(u=>{
          const days=Math.round((new Date(u.end_date).getTime()-new Date(u.start_date).getTime())/86400000);
          return (
            <div key={u.id} className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 flex items-center justify-between hover:shadow-md transition-shadow">
              <div className="flex items-center gap-3">
                <span className="text-xl">💻</span>
                <div>
                  <div className="font-medium text-gray-900 dark:text-white">{u.version}</div>
                  <div className="text-xs text-gray-400">{new Date(u.start_date).toLocaleDateString()} — {new Date(u.end_date).toLocaleDateString()} · {days} days</div>
                </div>
              </div>
              {u===longest&&<span className="text-[10px] bg-amber-100 dark:bg-amber-900/30 text-amber-600 px-2 py-0.5 rounded-full font-medium">Longest Run</span>}
            </div>
          );
        })}
      </div>
    </div>
  );
}
