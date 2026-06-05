import { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import StatCard from '../components/StatCard';
import { miningApi } from '../api/client';

export default function StatisticsPage() {
  const [stats, setStats] = useState([]);
  const [hours, setHours] = useState(24);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const res = await miningApi.getStatistics(hours);
        setStats(res.data.data || []);
      } catch { /* ignore */ }
      setLoading(false);
    };
    fetch();
  }, [hours]);

  const chartData = stats.map((s) => ({
    time: new Date(s.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    hashRate: parseFloat(s.hashRate),
    accepted: s.acceptedShares,
    rejected: s.rejectedShares,
    efficiency: parseFloat(s.efficiency),
    earnings: parseFloat(s.estimatedEarnings) * 1000000,
  }));

  const totalAccepted = stats.reduce((s, x) => s + x.acceptedShares, 0);
  const totalRejected = stats.reduce((s, x) => s + x.rejectedShares, 0);
  const avgEfficiency = stats.length ? (stats.reduce((s, x) => s + parseFloat(x.efficiency), 0) / stats.length).toFixed(1) : 0;
  const totalEarnings = stats.reduce((s, x) => s + parseFloat(x.estimatedEarnings), 0);

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Mining Statistics</h1>
          <p className="page-subtitle">Historical performance and share analytics</p>
        </div>
        <select value={hours} onChange={(e) => setHours(parseInt(e.target.value))}>
          <option value={6}>Last 6 hours</option>
          <option value={24}>Last 24 hours</option>
          <option value={72}>Last 3 days</option>
          <option value={168}>Last 7 days</option>
        </select>
      </div>

      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <StatCard label="Accepted Shares" value={totalAccepted.toLocaleString()} status="good" />
        <StatCard label="Rejected Shares" value={totalRejected.toLocaleString()} status={totalRejected > 100 ? 'warning' : 'good'} />
        <StatCard label="Avg Efficiency" value={`${avgEfficiency}%`} status="good" />
        <StatCard label="Total Earnings" value={totalEarnings.toFixed(8)} unit="BTC" />
      </div>

      {loading ? <div className="loading">Loading statistics...</div> : (
        <>
          <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
            <div className="panel">
              <div className="panel-header"><span className="panel-title">Hash Rate History</span></div>
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={chartData}>
                  <XAxis dataKey="time" stroke="#6b6b6b" fontSize={11} />
                  <YAxis stroke="#6b6b6b" fontSize={11} />
                  <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
                  <Line type="monotone" dataKey="hashRate" stroke="#2196f3" dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div className="panel">
              <div className="panel-header"><span className="panel-title">Share Distribution</span></div>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={chartData.slice(-20)}>
                  <XAxis dataKey="time" stroke="#6b6b6b" fontSize={10} />
                  <YAxis stroke="#6b6b6b" fontSize={11} />
                  <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
                  <Legend />
                  <Bar dataKey="accepted" fill="#00c853" />
                  <Bar dataKey="rejected" fill="#f44336" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="panel">
            <div className="panel-header"><span className="panel-title">Detailed Statistics</span></div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Time</th><th>Worker</th><th>Hash Rate</th><th>Accepted</th>
                  <th>Rejected</th><th>Efficiency</th><th>Earnings (BTC)</th>
                </tr>
              </thead>
              <tbody>
                {stats.slice().reverse().slice(0, 50).map((s) => (
                  <tr key={s.id}>
                    <td>{new Date(s.recordedAt).toLocaleString()}</td>
                    <td>{s.workerName}</td>
                    <td>{s.hashRate}</td>
                    <td>{s.acceptedShares}</td>
                    <td>{s.rejectedShares}</td>
                    <td>{s.efficiency}%</td>
                    <td>{parseFloat(s.estimatedEarnings).toFixed(8)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
