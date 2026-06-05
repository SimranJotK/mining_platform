import { useState } from 'react';

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    refreshInterval: '30',
    theme: 'dark',
    notifications: true,
    emailAlerts: false,
    simulationMode: true,
    defaultPool: 'simulation',
    chartPeriod: '24',
  });
  const [saved, setSaved] = useState(false);

  const handleChange = (key, value) => {
    setSettings({ ...settings, [key]: value });
    setSaved(false);
  };

  const handleSave = () => {
    localStorage.setItem('userSettings', JSON.stringify(settings));
    setSaved(true);
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Settings</h1>
        <p className="page-subtitle">Configure your monitoring preferences</p>
      </div>

      {saved && <div className="alert alert-success">Settings saved successfully</div>}

      <div className="grid-2">
        <div className="panel">
          <div className="panel-header"><span className="panel-title">Display Settings</span></div>
          <div className="form-group">
            <label>Dashboard Refresh Interval (seconds)</label>
            <select value={settings.refreshInterval} onChange={(e) => handleChange('refreshInterval', e.target.value)}>
              <option value="15">15 seconds</option>
              <option value="30">30 seconds</option>
              <option value="60">60 seconds</option>
              <option value="120">2 minutes</option>
            </select>
          </div>
          <div className="form-group">
            <label>Default Chart Period</label>
            <select value={settings.chartPeriod} onChange={(e) => handleChange('chartPeriod', e.target.value)}>
              <option value="6">6 hours</option>
              <option value="24">24 hours</option>
              <option value="72">3 days</option>
              <option value="168">7 days</option>
            </select>
          </div>
          <div className="form-group">
            <label>Theme</label>
            <select value={settings.theme} onChange={(e) => handleChange('theme', e.target.value)}>
              <option value="dark">Dark (Technical)</option>
            </select>
          </div>
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">Mining Settings</span></div>
          <div className="form-group">
            <label>
              <input type="checkbox" checked={settings.simulationMode}
                onChange={(e) => handleChange('simulationMode', e.target.checked)} style={{ marginRight: '0.5rem' }} />
              Simulation Mode
            </label>
          </div>
          <div className="form-group">
            <label>Default Mining Pool</label>
            <select value={settings.defaultPool} onChange={(e) => handleChange('defaultPool', e.target.value)}>
              <option value="simulation">Simulation Pool</option>
              <option value="slush">Slush Pool</option>
              <option value="f2pool">F2Pool</option>
              <option value="antpool">Antpool</option>
            </select>
          </div>
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">Notification Preferences</span></div>
          <div className="form-group">
            <label>
              <input type="checkbox" checked={settings.notifications}
                onChange={(e) => handleChange('notifications', e.target.checked)} style={{ marginRight: '0.5rem' }} />
              In-app Notifications
            </label>
          </div>
          <div className="form-group">
            <label>
              <input type="checkbox" checked={settings.emailAlerts}
                onChange={(e) => handleChange('emailAlerts', e.target.checked)} style={{ marginRight: '0.5rem' }} />
              Email Alerts (Worker Offline)
            </label>
          </div>
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">API Integration</span></div>
          <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            Future integrations: CGMiner, BFGMiner, ASIC devices, Stratum Protocol
          </p>
          <div className="terminal" style={{ color: 'var(--text-secondary)' }}>
            {`[INTEGRATION] Mode: ${settings.simulationMode ? 'SIMULATION' : 'API'}\n`}
            {`[SUPPORTED] Pool APIs, Worker Monitoring\n`}
            {`[PLANNED] CGMiner, BFGMiner, Stratum, ASIC\n`}
            {`[STATUS] Ready for pool API connection`}
          </div>
        </div>
      </div>

      <button className="btn btn-primary" style={{ marginTop: '1.5rem' }} onClick={handleSave}>Save Settings</button>
    </div>
  );
}
