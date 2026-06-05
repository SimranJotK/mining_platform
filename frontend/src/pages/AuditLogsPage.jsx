import { useState, useEffect } from 'react';
import { adminApi } from '../api/client';

export default function AuditLogsPage() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    adminApi.getAuditLogs(page)
      .then((res) => setLogs(res.data.data?.content || res.data.data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Audit Logs</h1>
        <p className="page-subtitle">Complete audit trail of platform activities</p>
      </div>

      <div className="panel">
        {loading ? <div className="loading">Loading audit logs...</div> : (
          <>
            <table className="data-table">
              <thead>
                <tr><th>Time</th><th>User</th><th>Action</th><th>Resource</th><th>IP</th><th>Status</th></tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td>{new Date(log.createdAt).toLocaleString()}</td>
                    <td>{log.username}</td>
                    <td><span className="badge badge-blue">{log.action}</span></td>
                    <td>{log.resourceType}{log.resourceId ? `:${log.resourceId}` : ''}</td>
                    <td style={{ fontSize: '0.75rem' }}>{log.ipAddress || '—'}</td>
                    <td><span className={`badge ${log.status === 'SUCCESS' ? 'badge-green' : log.status === 'FAILURE' ? 'badge-red' : 'badge-orange'}`}>{log.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '1rem' }}>
              <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
              <span style={{ padding: '0.5rem', color: 'var(--text-muted)' }}>Page {page + 1}</span>
              <button className="btn btn-secondary" onClick={() => setPage(page + 1)}>Next</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
