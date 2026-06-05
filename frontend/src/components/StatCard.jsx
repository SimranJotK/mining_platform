export default function StatCard({ label, value, unit, status, trend }) {
  const statusClass = status === 'good' ? 'status-online'
    : status === 'warning' ? 'status-warning'
    : status === 'bad' ? 'status-offline' : '';

  return (
    <div className="stat-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        {statusClass && <span className={`status-dot ${statusClass}`} />}
        <div className="stat-value">
          {value}
          {unit && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginLeft: '0.25rem' }}>{unit}</span>}
        </div>
      </div>
      <div className="stat-label">{label}</div>
      {trend && (
        <div style={{ fontSize: '0.6875rem', color: trend > 0 ? 'var(--accent-green)' : 'var(--accent-red)', marginTop: '0.25rem' }}>
          {trend > 0 ? '▲' : '▼'} {Math.abs(trend)}%
        </div>
      )}
    </div>
  );
}
