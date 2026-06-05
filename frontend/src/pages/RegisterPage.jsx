import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPage.css';

export default function RegisterPage() {
  const [form, setForm] = useState({
    email: '', username: '', password: '', firstName: '', lastName: '', encryptionPassphrase: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-brand">
          <span className="auth-icon">⛏</span>
          <h1>Create Account</h1>
          <p>Join the mining analytics platform</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label>Email</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" value={form.username} onChange={handleChange} required />
          </div>

          <div className="grid-2" style={{ gap: '0.75rem' }}>
            <div className="form-group">
              <label>First Name</label>
              <input type="text" name="firstName" value={form.firstName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input type="text" name="lastName" value={form.lastName} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={form.password} onChange={handleChange} required minLength={8} />
          </div>

          <div className="form-group">
            <label>Encryption Passphrase</label>
            <input type="password" name="encryptionPassphrase" value={form.encryptionPassphrase} onChange={handleChange} placeholder="For E2E encrypted data vault" />
            <small style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Used to encrypt your private data. Never shared with admins.</small>
          </div>

          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>

          <p className="auth-link">Already have an account? <Link to="/login">Sign In</Link></p>
        </form>
      </div>
    </div>
  );
}
