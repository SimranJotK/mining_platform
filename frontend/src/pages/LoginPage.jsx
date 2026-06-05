import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPage.css';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [mfaCode, setMfaCode] = useState('');
  const [mfaRequired, setMfaRequired] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const result = await login(email, password, mfaCode || undefined);
      if (result.mfaRequired) {
        setMfaRequired(true);
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-brand">
          <span className="auth-icon">⛏</span>
          <h1>CryptoMining Analytics</h1>
          <p>Secure mining operations monitoring</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Sign In</h2>
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required placeholder="user@platform.local" />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>

          {mfaRequired && (
            <div className="form-group">
              <label>MFA Code</label>
              <input type="text" value={mfaCode} onChange={(e) => setMfaCode(e.target.value)} placeholder="6-digit code" maxLength={6} required />
            </div>
          )}

          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

          <p className="auth-link">Don't have an account? <Link to="/register">Register</Link></p>
        </form>
      </div>
    </div>
  );
}
