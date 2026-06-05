import { Link } from 'react-router-dom';
import './LandingPage.css';

export default function LandingPage() {
  return (
    <div className="landing">
      <header className="landing-header">
        <div className="landing-logo">
          <span>⛏</span> CryptoMining Analytics
        </div>
        <nav>
          <Link to="/login" className="btn btn-secondary">Login</Link>
          <Link to="/register" className="btn btn-primary">Get Started</Link>
        </nav>
      </header>

      <section className="landing-hero">
        <div className="hero-content">
          <h1>AI-Powered Cryptocurrency<br />Mining Analytics Platform</h1>
          <p>Monitor hash rates, track worker performance, predict profitability, and optimize your mining operations with enterprise-grade security.</p>
          <div className="hero-actions">
            <Link to="/register" className="btn btn-primary">Start Monitoring</Link>
            <Link to="/login" className="btn btn-secondary">Sign In</Link>
          </div>
        </div>
        <div className="hero-terminal">
          <div className="terminal-header">
            <span className="terminal-dot red" />
            <span className="terminal-dot yellow" />
            <span className="terminal-dot green" />
            <span>mining-monitor — live</span>
          </div>
          <pre>{`[SYSTEM] Mining Analytics Platform v1.0
[STATUS] All systems operational
[WORKERS] 3 online / 3 total
[HASHRATE] 342.50 MH/s ▲ 2.3%
[POOL] Simulation Pool — connected
[AI] Profit forecast: +4.2% (7-day)
[SECURITY] E2E encryption active
[UPTIME] 99.97% — last 30 days`}</pre>
        </div>
      </section>

      <section className="landing-features">
        <div className="feature-card">
          <div className="feature-icon">◈</div>
          <h3>Real-Time Monitoring</h3>
          <p>Track hash rates, shares, temperatures, and power consumption across all mining workers in real time.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">◉</div>
          <h3>AI Predictions</h3>
          <p>Profit forecasting, anomaly detection, worker failure prediction, and optimization recommendations.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">⛨</div>
          <h3>Zero-Trust Security</h3>
          <p>End-to-end encrypted user data, JWT authentication, MFA, and role-based access control.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">⚙</div>
          <h3>Pool Integration</h3>
          <p>Connect to major mining pools or use simulation mode. Ready for CGMiner, BFGMiner, and Stratum.</p>
        </div>
      </section>

      <footer className="landing-footer">
        <p>CryptoMining Analytics Platform — Enterprise Mining Intelligence</p>
      </footer>
    </div>
  );
}
