import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: '▣' },
  { path: '/workers', label: 'Workers', icon: '⚙' },
  { path: '/statistics', label: 'Statistics', icon: '◈' },
  { path: '/ai-analytics', label: 'AI Analytics', icon: '◉' },
  { path: '/security', label: 'Security', icon: '⛨' },
  { path: '/notifications', label: 'Notifications', icon: '◔' },
  { path: '/profile', label: 'Profile', icon: '◎' },
  { path: '/settings', label: 'Settings', icon: '⚙' },
];

const adminItems = [
  { path: '/admin', label: 'Admin Panel', icon: '⬡' },
  { path: '/audit-logs', label: 'Audit Logs', icon: '☰' },
];

const creatorItems = [
  { path: '/creator', label: 'Creator Panel', icon: '◆' },
  { path: '/system-monitoring', label: 'System Monitor', icon: '◐' },
];

export default function Layout({ children }) {
  const { user, logout, isAdmin, isCreator } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">⛏</span>
          <div>
            <div className="brand-name">CryptoMining</div>
            <div className="brand-sub">Analytics Platform</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section">Monitoring</div>
          {navItems.map((item) => (
            <NavLink key={item.path} to={item.path} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}

          {isAdmin && (
            <>
              <div className="nav-section">Administration</div>
              {adminItems.map((item) => (
                <NavLink key={item.path} to={item.path} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
            </>
          )}

          {isCreator && (
            <>
              <div className="nav-section">Infrastructure</div>
              {creatorItems.map((item) => (
                <NavLink key={item.path} to={item.path} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                  <span className="nav-icon">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
            </>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <span className="status-dot status-online" />
            <div>
              <div className="user-name">{user?.username}</div>
              <div className="user-role">{user?.roles?.[0]?.replace('ROLE_', '')}</div>
            </div>
          </div>
          <button className="btn-logout" onClick={handleLogout}>Logout</button>
        </div>
      </aside>

      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
