import { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import StatCard from '../components/StatCard';
import { creatorApi } from '../api/client';

export default function SystemMonitoringPage() {
  const [health, setHealth] = useState(null);
  const [metricsHistory, setMetricsHistory] = useState([]);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await creatorApi.getHealth();
        setHealth(res.data.data);
        setMetricsHistory((prev) => [...prev.slice(-29), {
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
          workers: res.data.data?.onlineWorkers || 0,
          users: res.data.data?.activeUsers || 0,
          memory: res.data.data?.metrics?.jvm_memory_used
            ? Math.round(res.data.data.metrics.jvm_memory_used / 1024 / 1024) : 0,
        }]);
      } catch { /* ignore */ }
    };
    fetch();
    const interval = setInterval(fetch, 10000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">System Monitoring</h1>
        <p className="page-subtitle">Real-time platform health and infrastructure metrics</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <StatCard label="Platform" value={health?.status || '—'} status={health?.status === 'HEALTHY' ? 'good' : 'warning'} />
        <StatCard label="Backend API" value={health?.backendStatus || '—'} status="good" />
        <StatCard label="Database" value={health?.databaseStatus || '—'} status="good" />
        <StatCard label="AI Microservice" value={health?.aiServiceStatus || '—'} status={health?.aiServiceStatus === 'UP' ? 'good' : 'bad'} />
      </div>

      <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
        <div className="panel">
          <div className="panel-header"><span className="panel-title">Active Workers (Live)</span></div>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={metricsHistory}>
              <XAxis dataKey="time" stroke="#6b6b6b" fontSize={10} />
              <YAxis stroke="#6b6b6b" fontSize={11} />
              <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
              <Area type="monotone" dataKey="workers" stroke="#00c853" fill="rgba(0,200,83,0.1)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
        <div className="panel">
          <div className="panel-header"><span className="panel-title">JVM Memory (MB)</span></div>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={metricsHistory}>
              <XAxis dataKey="time" stroke="#6b6b6b" fontSize={10} />
              <YAxis stroke="#6b6b6b" fontSize={11} />
              <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
              <Area type="monotone" dataKey="memory" stroke="#ff9800" fill="rgba(255,152,0,0.1)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="panel">
        <div className="panel-header"><span className="panel-title">System Terminal</span></div>
        <div className="terminal">
          {`[${new Date().toISOString()}] System health check\n`}
          {`[BACKEND] ${health?.backendStatus || 'CHECKING...'}\n`}
          {`[DATABASE] ${health?.databaseStatus || 'CHECKING...'}\n`}
          {`[AI-SERVICE] ${health?.aiServiceStatus || 'CHECKING...'}\n`}
          {`[USERS] ${health?.activeUsers || 0}/${health?.totalUsers || 0} active\n`}
          {`[WORKERS] ${health?.onlineWorkers || 0}/${health?.totalWorkers || 0} online\n`}
          {`[AUDIT] ${health?.auditLogsToday || 0} events today\n`}
          {`[MEMORY] ${health?.metrics?.jvm_memory_used ? Math.round(health.metrics.jvm_memory_used / 1024 / 1024) + ' MB used' : 'N/A'}`}
        </div>
      </div>
    </div>
  );
}
