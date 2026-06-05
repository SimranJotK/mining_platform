import { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { creatorApi } from '../api/client';

export default function CreatorPage() {
  const [health, setHealth] = useState(null);
  const [configs, setConfigs] = useState([]);
  const [deployStatus, setDeployStatus] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([creatorApi.getHealth(), creatorApi.getConfigurations()])
      .then(([hRes, cRes]) => {
        setHealth(hRes.data.data);
        setConfigs(cRes.data.data || []);
      }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleDeploy = async (service) => {
    try {
      const res = await creatorApi.deploy(service);
      setDeployStatus(res.data.data?.message || 'Deployment initiated');
    } catch {
      setDeployStatus('Deployment failed');
    }
  };

  if (loading) return <div className="loading">Loading creator panel...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Creator Dashboard</h1>
        <p className="page-subtitle">Full infrastructure control and service management</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <StatCard label="Platform Status" value={health?.status || '—'} status="good" />
        <StatCard label="Backend" value={health?.backendStatus || '—'} status="good" />
        <StatCard label="Database" value={health?.databaseStatus || '—'} status="good" />
        <StatCard label="AI Service" value={health?.aiServiceStatus || '—'} status={health?.aiServiceStatus === 'UP' ? 'good' : 'warning'} />
      </div>

      <div className="grid-2">
        <div className="panel">
          <div className="panel-header"><span className="panel-title">Service Deployment</span></div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {['backend', 'ai-service', 'frontend', 'database'].map((svc) => (
              <div key={svc} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid var(--border-color)' }}>
                <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.875rem' }}>{svc}</span>
                <button className="btn btn-primary" style={{ fontSize: '0.75rem' }} onClick={() => handleDeploy(svc)}>Deploy</button>
              </div>
            ))}
          </div>
          {deployStatus && <div className="alert alert-success" style={{ marginTop: '1rem' }}>{deployStatus}</div>}
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">System Configurations</span></div>
          <table className="data-table">
            <thead><tr><th>Key</th><th>Value</th><th>Description</th></tr></thead>
            <tbody>
              {configs.map((c) => (
                <tr key={c.id}>
                  <td style={{ fontSize: '0.75rem' }}>{c.configKey}</td>
                  <td>{c.configValue}</td>
                  <td style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>{c.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="panel" style={{ marginTop: '1.5rem' }}>
        <div className="panel-header"><span className="panel-title">Infrastructure Metrics</span></div>
        <div className="terminal">
          {`[PLATFORM] Status: ${health?.status}\n`}
          {`[USERS] Total: ${health?.totalUsers} | Active: ${health?.activeUsers}\n`}
          {`[WORKERS] Total: ${health?.totalWorkers} | Online: ${health?.onlineWorkers}\n`}
          {`[AUDIT] Logs today: ${health?.auditLogsToday}\n`}
          {`[JVM] Memory: ${health?.metrics?.jvm_memory_used ? Math.round(health.metrics.jvm_memory_used / 1024 / 1024) + ' MB' : 'N/A'}`}
        </div>
      </div>
    </div>
  );
}
