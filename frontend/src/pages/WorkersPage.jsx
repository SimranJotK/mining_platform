import { useState, useEffect } from 'react';
import { miningApi } from '../api/client';

export default function WorkersPage() {
  const [workers, setWorkers] = useState([]);
  const [pools, setPools] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [newWorker, setNewWorker] = useState({ workerName: '', poolId: '' });
  const [loading, setLoading] = useState(true);

  const fetchWorkers = async () => {
    try {
      const [wRes, pRes] = await Promise.all([miningApi.getWorkers(), miningApi.getPools()]);
      setWorkers(wRes.data.data || []);
      setPools(pRes.data.data || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => {
    fetchWorkers();
    const interval = setInterval(fetchWorkers, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await miningApi.createWorker({
        workerName: newWorker.workerName,
        poolId: newWorker.poolId ? parseInt(newWorker.poolId) : null,
      });
      setShowAdd(false);
      setNewWorker({ workerName: '', poolId: '' });
      fetchWorkers();
    } catch { /* ignore */ }
  };

  const formatUptime = (seconds) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h}h ${m}m`;
  };

  if (loading) return <div className="loading">Loading workers...</div>;

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Mining Workers</h1>
          <p className="page-subtitle">Monitor and manage mining worker nodes</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowAdd(!showAdd)}>+ Add Worker</button>
      </div>

      {showAdd && (
        <div className="panel" style={{ marginBottom: '1.5rem' }}>
          <form onSubmit={handleAdd} className="grid-3" style={{ alignItems: 'end' }}>
            <div className="form-group">
              <label>Worker Name</label>
              <input value={newWorker.workerName} onChange={(e) => setNewWorker({ ...newWorker, workerName: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Mining Pool</label>
              <select value={newWorker.poolId} onChange={(e) => setNewWorker({ ...newWorker, poolId: e.target.value })}>
                <option value="">Select pool</option>
                {pools.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <button type="submit" className="btn btn-primary">Create Worker</button>
          </form>
        </div>
      )}

      <div className="grid-3">
        {workers.map((w) => (
          <div key={w.id} className="panel" style={{ borderLeft: `3px solid ${w.status === 'ONLINE' ? 'var(--accent-green)' : w.status === 'ERROR' ? 'var(--accent-red)' : 'var(--accent-orange)'}` }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.9375rem' }}>{w.workerName}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{w.workerId}</div>
              </div>
              <span className={`badge ${w.status === 'ONLINE' ? 'badge-green' : w.status === 'ERROR' ? 'badge-red' : 'badge-orange'}`}>{w.status}</span>
            </div>
            <div className="grid-2" style={{ gap: '0.75rem' }}>
              <div>
                <div className="stat-label">Hash Rate</div>
                <div className="stat-value" style={{ fontSize: '1.125rem' }}>{w.hashRate} <span style={{ fontSize: '0.75rem' }}>{w.hashRateUnit}</span></div>
              </div>
              <div>
                <div className="stat-label">Temperature</div>
                <div className="stat-value" style={{ fontSize: '1.125rem' }}>{w.temperature ? `${w.temperature}°C` : '—'}</div>
              </div>
              <div>
                <div className="stat-label">Power</div>
                <div style={{ fontFamily: 'var(--font-mono)' }}>{w.powerConsumption ? `${w.powerConsumption}W` : '—'}</div>
              </div>
              <div>
                <div className="stat-label">Uptime</div>
                <div style={{ fontFamily: 'var(--font-mono)' }}>{formatUptime(w.uptimeSeconds || 0)}</div>
              </div>
            </div>
            <div style={{ marginTop: '0.75rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {w.deviceType} · {w.poolName || 'No pool'}
            </div>
          </div>
        ))}
      </div>

      {workers.length === 0 && (
        <div className="panel" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          No mining workers configured. Add a worker to start monitoring.
        </div>
      )}
    </div>
  );
}
