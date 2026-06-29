export function About() {
  return (
    <div className="space-y-4 max-w-lg">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">About Tesla_MateLink</h2>
      <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 border border-gray-100 dark:border-gray-700 space-y-3 text-sm text-gray-600 dark:text-gray-400">
        <p><strong className="text-gray-900 dark:text-white">Tesla_MateLink</strong> — Your Tesla Data Companion.</p>
        <p>Connects to your self-hosted <a href="https://github.com/teslamate-org/teslamate" className="text-blue-500 hover:underline" target="_blank">TeslaMate</a> instance to view vehicle data on mobile and web.</p>
        <div className="border-t border-gray-100 dark:border-gray-700 pt-3 mt-3">
          <p><strong>Not affiliated with Tesla, Inc.</strong></p>
          <p className="text-xs">Tesla is a registered trademark of Tesla, Inc. Requires self-hosted TeslaMate + TeslaMateApi.</p>
        </div>
        <div className="text-xs">
          <p>Version: 0.1.0-alpha · Built with React + Vite + Tailwind</p>
          <p>License: MIT · <a href="https://github.com" className="text-blue-500 hover:underline">GitHub</a></p>
        </div>
      </div>
    </div>
  );
}
