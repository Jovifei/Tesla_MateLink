import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Sidebar } from './components/Sidebar';
import { Dashboard } from './pages/Dashboard';
import { Drives } from './pages/Drives';
import { DriveDetail } from './pages/DriveDetail';
import { Charges } from './pages/Charges';
import { ChargeDetail } from './pages/ChargeDetail';
import { BatteryHealth } from './pages/BatteryHealth';
import { Updates } from './pages/Updates';
import { Timeline } from './pages/Timeline';
import { Statistics } from './pages/Statistics';
import { Heatmap } from './pages/Heatmap';
import { Destinations } from './pages/Destinations';
import { Efficiency } from './pages/Efficiency';
import { Vampire } from './pages/Vampire';
import { RangePage } from './pages/Range';
import { Cost } from './pages/Cost';
import { Settings } from './pages/Settings';
import { About } from './pages/About';

const P = ({title}:{title:string})=><div className="flex flex-col items-center justify-center h-64 text-gray-400 gap-2"><span className="text-3xl">🛠</span><span className="text-lg">{title}</span><span className="text-xs">Coming soon</span></div>;

export default function App() {
  return (
    <BrowserRouter>
      <div className="flex h-screen bg-[#F5F5F7] dark:bg-[#1A1A2E] text-gray-900 dark:text-white">
        <Sidebar/>
        <main className="flex-1 overflow-y-auto p-6">
          <Routes>
            <Route path="/" element={<Dashboard/>}/>
            <Route path="/battery" element={<BatteryHealth/>}/>
            <Route path="/updates" element={<P title="Firmware Updates"/>}/>
            <Route path="/timeline" element={<P title="Vehicle Timeline"/>}/>
            <Route path="/drives" element={<Drives/>}/>
            <Route path="/drives/:id" element={<DriveDetail/>}/>
            <Route path="/charges" element={<Charges/>}/>
            <Route path="/charges/:id" element={<ChargeDetail/>}/>
            <Route path="/updates" element={<Updates/>}/>
            <Route path="/timeline" element={<Timeline/>}/>
            <Route path="/statistics" element={<Statistics/>}/>
            <Route path="/heatmap" element={<Heatmap/>}/>
            <Route path="/destinations" element={<Destinations/>}/>
            <Route path="/efficiency" element={<Efficiency/>}/>
            <Route path="/vampire" element={<Vampire/>}/>
            <Route path="/range" element={<RangePage/>}/>
            <Route path="/cost" element={<Cost/>}/>
            <Route path="/settings" element={<Settings/>}/>
            <Route path="/about" element={<About/>}/>
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
