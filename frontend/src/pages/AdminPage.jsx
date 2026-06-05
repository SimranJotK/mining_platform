import { useState, useEffect } from 'react';
import StatCard from '../components/StatCard';
import { adminApi } from '../api/client';

export default function AdminPage() {
  const [users, setUsers] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([adminApi.getUsers(), adminApi.getAnalytics()])
      .then(([uRes, aRes]) => {
        setUsers(uRes.data.data || []);
        setAnalytics(aRes.data.data);
      }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleSuspend = async (id) => {
    await adminApi.suspendUser(id);
    const res = await adminApi.getUsers();
    setUsers(res.data.data || []);
  };

  const handleActivate = async (id) => {
    await adminApi.activateUser(id);
    const res = await adminApi.getUsers();
    setUsers(res.data.data || []);
  };

  if (loading) return <div className="loading">Loading admin panel...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="page-subtitle">User management and platform analytics</p>
      </div>

      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <StatCard label="Total Users" value={analytics?.totalUsers || 0} />
        <StatCard label="Active Users" value={analytics?.activeUsers || 0} status="good" />
        <StatCard label="Online Workers" value={analytics?.onlineWorkers || 0} status="good" />
        <StatCard label="AI Service" value={analytics?.aiServiceStatus || '—'} status={analytics?.aiServiceStatus === 'UP' ? 'good' : 'warning'} />
      </div>

      <div className="panel">
        <div className="panel-header"><span className="panel-title">User Management</span></div>
        <table className="data-table">
          <thead>
            <tr><th>ID</th><th>Username</th><th>Email</th><th>Roles</th><th>Status</th><th>MFA</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>{u.roles?.join(', ')}</td>
                <td><span className={`badge ${u.accountStatus === 'ACTIVE' ? 'badge-green' : 'badge-red'}`}>{u.accountStatus}</span></td>
                <td>{u.mfaEnabled ? '✓' : '—'}</td>
                <td>
                  {u.accountStatus === 'ACTIVE' ? (
                    <button className="btn btn-danger" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                      onClick={() => handleSuspend(u.id)}>Suspend</button>
                  ) : (
                    <button className="btn btn-primary" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                      onClick={() => handleActivate(u.id)}>Activate</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
