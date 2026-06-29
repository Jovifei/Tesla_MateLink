import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Route, Zap, Battery, Settings, BarChart3, Flame, MapPin, Gauge, Info, Droplets, Clock, Cpu, TrendingUp, DollarSign } from 'lucide-react';

const groups = [
  { label:'Vehicle', items:[
    { to:'/', icon:LayoutDashboard, label:'Dashboard' },
    { to:'/battery', icon:Battery, label:'Battery' },
    { to:'/updates', icon:Cpu, label:'Updates' },
    { to:'/timeline', icon:Clock, label:'Timeline' },
  ]},
  { label:'History', items:[
    { to:'/drives', icon:Route, label:'Drives' },
    { to:'/charges', icon:Zap, label:'Charges' },
  ]},
  { label:'Analytics', items:[
    { to:'/statistics', icon:BarChart3, label:'Statistics' },
    { to:'/heatmap', icon:Flame, label:'Heatmap' },
    { to:'/destinations', icon:MapPin, label:'Top Dest' },
    { to:'/efficiency', icon:Gauge, label:'Efficiency' },
    { to:'/vampire', icon:Droplets, label:'Vampire' },
    { to:'/range', icon:TrendingUp, label:'Range' },
    { to:'/cost', icon:DollarSign, label:'Cost' },
  ]},
  { label:'System', items:[
    { to:'/settings', icon:Settings, label:'Settings' },
    { to:'/about', icon:Info, label:'About' },
  ]},
];

export function Sidebar() {
  return (
    <aside className="w-56 bg-white dark:bg-[#16213E] border-r border-gray-200 dark:border-gray-700 flex flex-col min-h-screen overflow-y-auto">
      <div className="p-5 font-bold text-xl text-blue-600 dark:text-blue-400 border-b border-gray-200 dark:border-gray-700 sticky top-0 bg-white dark:bg-[#16213E] z-10">Tesla_MateLink</div>
      <nav className="flex-1 py-1">
        {groups.map(g=>(
          <div key={g.label} className="mb-1">
            <div className="px-5 py-2 text-[10px] font-semibold text-gray-400 uppercase tracking-wider">{g.label}</div>
            {g.items.map(({to,icon:Icon,label})=>(
              <NavLink key={to} to={to} end={to==='/'} className={({isActive})=>`flex items-center gap-3 px-5 py-2 text-sm transition-colors ${isActive?'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 font-medium border-r-2 border-blue-600':'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800'}`}>
                <Icon size={16}/> {label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
    </aside>
  );
}
