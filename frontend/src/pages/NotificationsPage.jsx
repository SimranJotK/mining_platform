import { useState, useEffect } from 'react';
import { notificationApi } from '../api/client';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch = async () => {
    try {
      const res = await notificationApi.getAll();
      setNotifications(res.data.data || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetch(); }, []);

  const handleMarkRead = async (id) => {
    await notificationApi.markAsRead(id);
    fetch();
  };

  const handleMarkAll = async () => {
    await notificationApi.markAllAsRead();
    fetch();
  };

  const typeBadge = (type) => {
    const map = { INFO: 'badge-blue', WARNING: 'badge-orange', ALERT: 'badge-red', SUCCESS: 'badge-green', SYSTEM: 'badge-blue' };
    return map[type] || 'badge-blue';
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">System alerts and mining event notifications</p>
        </div>
        <button className="btn btn-secondary" onClick={handleMarkAll}>Mark All Read</button>
      </div>

      {loading ? <div className="loading">Loading notifications...</div> : (
        <div className="panel">
          {notifications.map((n) => (
            <div key={n.id} style={{
              padding: '1rem', borderBottom: '1px solid var(--border-color)',
              opacity: n.isRead ? 0.6 : 1, display: 'flex', justifyContent: 'space-between', alignItems: 'start',
            }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <span className={`badge ${typeBadge(n.type)}`}>{n.type}</span>
                  <span style={{ fontWeight: 500 }}>{n.title}</span>
                  {!n.isRead && <span className="status-dot status-online" />}
                </div>
                <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>{n.message}</p>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{new Date(n.createdAt).toLocaleString()}</span>
              </div>
              {!n.isRead && (
                <button className="btn btn-secondary" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
                  onClick={() => handleMarkRead(n.id)}>Read</button>
              )}
            </div>
          ))}
          {notifications.length === 0 && (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>No notifications</div>
          )}
        </div>
      )}
    </div>
  );
}
