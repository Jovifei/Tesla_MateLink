import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useStore } from '../store';
import { ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ZAxis, Cell } from 'recharts';

interface Point { speed:number; eff:number; temp:number; date:string; }

export function Efficiency() {
  const {currentCarId} = useStore();
  const [points,setPoints] = useState<Point[]>([]);
  const [selected,setSelected] = useState<Point|null>(null);

  useEffect(()=>{ api.getDrives(currentCarId).then(drives=>{
    setPoints(drives.filter(d=>d.distance_km>1).map(d=>({speed:Math.round(d.distance_km/d.duration_min*60),eff:d.efficiency,temp:d.outside_temp_avg||25,date:d.start_date.slice(0,10)})));
  }); },[]);

  const tempColor = (t:number) => t<0?'#3B82F6':t<15?'#10B981':t<25?'#F59E0B':'#EF4444';
  const zones = [{l:'0-30',min:0,max:30},{l:'30-60',min:30,max:60},{l:'60-90',min:60,max:90},{l:'90-120',min:90,max:120},{l:'120+',min:120,max:200}];
  const zoneStats = zones.map(z=>{const pts=points.filter(p=>p.speed>=z.min&&p.speed<z.max);return {...z,count:pts.length,avgEff:pts.length?Math.round(pts.reduce((s,p)=>s+p.eff,0)/pts.length):0};});

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Efficiency Curve</h2>
      <p className="text-sm text-gray-400">Speed vs Efficiency — colored by outside temperature</p>

      <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700">
        <ResponsiveContainer width="100%" height={350}>
          <ScatterChart><CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb"/><XAxis type="number" dataKey="speed" name="Speed" unit=" km/h" tick={{fontSize:11}}/><YAxis type="number" dataKey="eff" name="Efficiency" unit=" Wh/km" tick={{fontSize:11}}/><ZAxis range={[40,40]}/><Tooltip contentStyle={{borderRadius:8,border:'none',boxShadow:'0 4px 12px rgba(0,0,0,.1)'}} cursor={{strokeDasharray:'3 3'}}/>
            <Scatter data={points} onClick={(d:any)=>{setSelected(d);}}>
              {points.map((p,i)=><Cell key={i} fill={tempColor(p.temp)} fillOpacity={0.6}/>)}
            </Scatter>
          </ScatterChart>
        </ResponsiveContainer>
      </div>

      <div className="flex items-center justify-center gap-4 text-xs">
        {[{label:'<0°C',color:'#3B82F6'},{label:'0-15°C',color:'#10B981'},{label:'15-25°C',color:'#F59E0B'},{label:'>25°C',color:'#EF4444'}].map(l=><span key={l.label} className="flex items-center gap-1"><span className="w-3 h-3 rounded-full" style={{backgroundColor:l.color}}/>{l.label}</span>)}
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 divide-y divide-gray-100 dark:divide-gray-700">
        <div className="px-5 py-2.5 grid grid-cols-4 text-[10px] text-gray-400 font-medium uppercase tracking-wider"><div>Speed Zone</div><div className="text-center">Drives</div><div className="text-center">Avg Eff</div><div className="text-center">Best</div></div>
        {zoneStats.map(z=>{
          const best=Math.min(...points.filter(p=>p.speed>=z.min&&p.speed<z.max).map(p=>p.eff));
          return <div key={z.l} className="px-5 py-2.5 grid grid-cols-4 text-sm items-center"><div className="font-medium text-gray-900 dark:text-white">{z.l} km/h</div><div className="text-center text-gray-500">{z.count}</div><div className="text-center font-medium">{z.avgEff} Wh/km</div><div className="text-center text-gray-400">{isFinite(best)?best:0} Wh/km</div></div>;
        })}
      </div>
      {selected&&<div className="text-center text-xs text-blue-500">Selected: {selected.speed} km/h · {selected.eff} Wh/km · {selected.date}</div>}
    </div>
  );
}
