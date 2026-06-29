import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import type { Drive } from '../api/types';

export function Drives() {
  const {currentCarId} = useStore();
  const [drives,setDrives] = useState<Drive[]>([]);
  const [loading,setLoading] = useState(true);

  useEffect(()=>{ api.getDrives(currentCarId).then(d=>{setDrives(d);setLoading(false);}); },[currentCarId]);

  if(loading) return <div className="animate-pulse text-gray-400">Loading...</div>;

  const today = new Date().toISOString().slice(0,10);
  const groups = new Map<string,Drive[]>();
  drives.forEach(d=>{ const date=d.start_date.slice(0,10); const label=date===today?'Today':date===new Date(Date.now()-86400000).toISOString().slice(0,10)?'Yesterday':date; const g=groups.get(label)||[]; g.push(d); groups.set(label,g); });

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Drive History</h2>
      {[...groups.entries()].map(([label,items])=>(
        <div key={label}>
          <h3 className="text-sm font-medium text-gray-400 mb-2">{label} · {items.length} drives</h3>
          <div className="space-y-2">
            {items.map(d=>(
              <div key={d.id} className="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-100 dark:border-gray-700 hover:shadow-md transition-shadow cursor-pointer">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <span className="text-2xl">🚗</span>
                    <div>
                      <div className="font-medium text-gray-900 dark:text-white">{d.start_address} → {d.end_address}</div>
                      <div className="text-xs text-gray-400">{new Date(d.start_date).toLocaleTimeString()} - {new Date(d.end_date).toLocaleTimeString()}</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-gray-900 dark:text-white">{d.distance_km} km</div>
                    <div className="text-xs text-gray-400">{d.duration_min} min · {d.efficiency} Wh/km</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
