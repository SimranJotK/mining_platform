import { useState, useEffect } from 'react';
import { userApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ firstName: '', lastName: '' });
  const [message, setMessage] = useState('');

  useEffect(() => {
    userApi.getProfile().then((res) => {
      setProfile(res.data.data);
      setForm({ firstName: res.data.data.firstName || '', lastName: res.data.data.lastName || '' });
    }).catch(() => {});
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      const res = await userApi.updateProfile(form);
      setProfile(res.data.data);
      setMessage('Profile updated successfully');
    } catch {
      setMessage('Failed to update profile');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Profile Management</h1>
        <p className="page-subtitle">Manage your account information</p>
      </div>

      {message && <div className="alert alert-success">{message}</div>}

      <div className="grid-2">
        <div className="panel">
          <div className="panel-header"><span className="panel-title">Account Details</span></div>
          <table className="data-table">
            <tbody>
              <tr><td style={{ color: 'var(--text-muted)' }}>Email</td><td>{profile?.email || user?.email}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Username</td><td>{profile?.username || user?.username}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Role</td><td>{profile?.roles?.join(', ') || user?.roles?.join(', ')}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Status</td><td><span className="badge badge-green">{profile?.accountStatus || 'ACTIVE'}</span></td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>MFA</td><td>{profile?.mfaEnabled ? 'Enabled' : 'Disabled'}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Last Login</td><td>{profile?.lastLoginAt ? new Date(profile.lastLoginAt).toLocaleString() : '—'}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Member Since</td><td>{profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—'}</td></tr>
            </tbody>
          </table>
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">Edit Profile</span></div>
          <form onSubmit={handleSave}>
            <div className="form-group">
              <label>First Name</label>
              <input value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
            </div>
            <button type="submit" className="btn btn-primary">Save Changes</button>
          </form>
        </div>
      </div>
    </div>
  );
}
