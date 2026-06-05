import { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import StatCard from '../components/StatCard';
import { dashboardApi, miningApi } from '../api/client';

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [sumRes, statRes] = await Promise.all([
          dashboardApi.getSummary(),
          miningApi.getStatistics(6),
        ]);
        setSummary(sumRes.data.data);
        setStats(statRes.data.data || []);
      } catch { /* use defaults */ }
      setLoading(false);
    };
    fetch();
    const interval = setInterval(fetch, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;

  const chartData = stats.map((s) => ({
    time: new Date(s.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    hashRate: parseFloat(s.hashRate),
    earnings: parseFloat(s.estimatedEarnings) * 100000,
  }));

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Mining Dashboard</h1>
        <p className="page-subtitle">Real-time mining operations overview</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <StatCard label="Total Hash Rate" value={summary?.totalHashRate?.toFixed(2) || '0'} unit="MH/s" status="good" />
        <StatCard label="Online Workers" value={`${summary?.onlineWorkers || 0}/${summary?.totalWorkers || 0}`} status={summary?.onlineWorkers > 0 ? 'good' : 'warning'} />
        <StatCard label="Accepted Shares" value={summary?.totalAcceptedShares?.toLocaleString() || '0'} status="good" />
        <StatCard label="Daily Earnings" value={summary?.estimatedDailyEarnings?.toFixed(8) || '0'} unit="BTC" />
      </div>

      <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
        <div className="panel">
          <div className="panel-header">
            <span className="panel-title">Hash Rate Trend</span>
            <span className="badge badge-green">LIVE</span>
          </div>
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={chartData}>
              <XAxis dataKey="time" stroke="#6b6b6b" fontSize={11} />
              <YAxis stroke="#6b6b6b" fontSize={11} />
              <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
              <Area type="monotone" dataKey="hashRate" stroke="#2196f3" fill="rgba(33,150,243,0.1)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="panel">
          <div className="panel-header">
            <span className="panel-title">System Terminal</span>
          </div>
          <div className="terminal">
            {`[${new Date().toISOString()}] Dashboard sync complete\n`}
            {`[WORKERS] ${summary?.onlineWorkers}/${summary?.totalWorkers} online\n`}
            {`[HASHRATE] ${summary?.totalHashRate?.toFixed(2) || 0} MH/s\n`}
            {`[EFFICIENCY] ${summary?.averageEfficiency?.toFixed(1) || 0}%\n`}
            {`[POWER] ${summary?.totalPowerConsumption?.toFixed(0) || 0}W\n`}
            {`[REJECTED] ${summary?.totalRejectedShares || 0} shares\n`}
            {`[NOTIFICATIONS] ${summary?.unreadNotifications || 0} unread`}
          </div>
        </div>
      </div>

      <div className="panel">
        <div className="panel-header">
          <span className="panel-title">Active Workers</span>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Worker</th>
              <th>Status</th>
              <th>Hash Rate</th>
              <th>Temperature</th>
              <th>Pool</th>
              <th>Last Seen</th>
            </tr>
          </thead>
          <tbody>
            {(summary?.recentWorkers || []).map((w) => (
              <tr key={w.id}>
                <td>{w.workerName}</td>
                <td>
                  <span className={`badge ${w.status === 'ONLINE' ? 'badge-green' : w.status === 'ERROR' ? 'badge-red' : 'badge-orange'}`}>
                    {w.status}
                  </span>
                </td>
                <td>{w.hashRate} {w.hashRateUnit}</td>
                <td>{w.temperature ? `${w.temperature}°C` : '—'}</td>
                <td>{w.poolName || '—'}</td>
                <td>{w.lastSeenAt ? new Date(w.lastSeenAt).toLocaleString() : '—'}</td>
              </tr>
            ))}
            {(!summary?.recentWorkers || summary.recentWorkers.length === 0) && (
              <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No workers configured</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
