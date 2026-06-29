import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';


const HOURS = Array.from({length:24},(_,i)=>i);
const DAYS = 15;

export function Heatmap() {
  const {currentCarId} = useStore();
  const [grid,setGrid] = useState<number[][]>([]);
  const [tooltip,setTooltip] = useState<string|null>(null);
  const [maxVal,setMaxVal] = useState(1);

  useEffect(()=>{ api.getDrives(currentCarId).then(drives=>{
    const g = Array.from({length:24},()=>Array(DAYS).fill(0));
    drives.forEach(d=>{ const h=new Date(d.start_date).getHours(); const dayOffset=Math.floor((Date.now()-new Date(d.start_date).getTime())/86400000); if(dayOffset>=0&&dayOffset<DAYS) g[h][DAYS-1-dayOffset]+=d.distance_km; });
    setGrid(g); setMaxVal(Math.max(1,...g.flat()));
  }); },[currentCarId]);

  const getColor = (v:number) => {
    if(v===0) return 'bg-gray-100 dark:bg-gray-800';
    const p = v/maxVal;
    if(p<0.25) return 'bg-blue-100 dark:bg-blue-900/40';
    if(p<0.5) return 'bg-blue-300 dark:bg-blue-700/60';
    if(p<0.75) return 'bg-blue-500 dark:bg-blue-500/80';
    return 'bg-blue-700 dark:bg-blue-400';
  };

  const days = Array.from({length:DAYS},(_,i)=>{
    const d=new Date(); d.setDate(d.getDate()-(DAYS-1-i));
    return d.toLocaleDateString('en',{month:'short',day:'numeric'});
  });

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Drive Heatmap</h2>
      <p className="text-sm text-gray-400">GitHub-style activity heatmap — 15 days × 24 hours</p>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 border border-gray-100 dark:border-gray-700 overflow-x-auto">
        <div className="flex gap-0.5" onMouseLeave={()=>setTooltip(null)}>
          {/* Hour labels */}
          <div className="flex flex-col gap-0.5 mr-2 text-[9px] text-gray-400 pt-1">
            {HOURS.filter(h=>h%3===0).map(h=><div key={h} className="h-3.5 flex items-center">{String(h).padStart(2,'0')}</div>)}
          </div>
          {/* Grid */}
          {days.map((day,di)=><div key={di} className="flex flex-col gap-0.5">
            <div className="text-[9px] text-gray-400 text-center mb-1">{di%3===0?day.slice(-2):''}</div>
            {HOURS.map(h=><div key={h} className={`w-3.5 h-3.5 rounded-sm ${getColor(grid[h]?.[di]||0)} cursor-pointer transition-transform hover:scale-125 hover:ring-1 hover:ring-blue-400`} onMouseEnter={()=>setTooltip(`${h}:00 · ${(grid[h]?.[di]||0).toFixed(1)} km · ${day}`)}/>)}
          </div>)}
        </div>
        {/* Legend */}
        <div className="flex items-center gap-1 mt-4 text-[10px] text-gray-400">
          <span>Less</span>
          {[0,0.25,0.5,0.75,1].map(p=><div key={p} className={`w-3 h-3 rounded-sm ${getColor(p*maxVal)}`}/>)}
          <span>More</span>
        </div>
      </div>
      {tooltip&&<div className="text-center text-sm text-blue-500">{tooltip}</div>}
    </div>
  );
}
