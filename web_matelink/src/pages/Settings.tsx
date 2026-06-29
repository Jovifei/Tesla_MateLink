import { useStore } from '../store';

export function Settings() {
  const {theme,setTheme,mockMode,setMockMode,serverUrl,apiToken} = useStore();
  return (
    <div className="space-y-5 max-w-lg">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Settings</h2>
      <Section title="Connection">
        <label className="block text-xs text-gray-400 mb-1">Server URL</label>
        <input type="text" defaultValue={serverUrl} placeholder="https://teslamate.example.com" className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-sm"/>
        <label className="block text-xs text-gray-400 mt-3 mb-1">API Token</label>
        <input type="password" defaultValue={apiToken} placeholder="Bearer token" className="w-full px-3 py-2 border border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-sm"/>
        <button className="mt-3 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white text-sm rounded-lg transition-colors">Test Connection</button>
      </Section>
      <Section title="Preferences">
        <div className="flex items-center justify-between py-2"><span className="text-sm">Theme</span>
          <select value={theme} onChange={e=>setTheme(e.target.value as any)} className="px-2 py-1 border border-gray-200 dark:border-gray-700 rounded bg-white dark:bg-gray-800 text-sm">
            <option value="system">System</option><option value="light">Light</option><option value="dark">Dark</option>
          </select>
        </div>
      </Section>
      <Section title="Development">
        <div className="flex items-center justify-between py-2">
          <div><div className="text-sm">Mock Mode</div><div className="text-xs text-gray-400">Use built-in mock data</div></div>
          <button onClick={()=>setMockMode(!mockMode)} className={`w-11 h-6 rounded-full transition-colors ${mockMode?'bg-blue-500':'bg-gray-300 dark:bg-gray-600'} relative`}>
            <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white transition-transform ${mockMode?'translate-x-5.5':'translate-x-0.5'}`}/>
          </button>
        </div>
      </Section>
    </div>
  );
}
function Section({title,children}:{title:string;children:any}) {
  return <div className="bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700"><h3 className="text-sm font-semibold text-gray-500 mb-3">{title}</h3>{children}</div>;
}
