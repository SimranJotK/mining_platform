import { useState } from 'react';
import { userApi } from '../api/client';

export default function SecurityPage() {
  const [passphrase, setPassphrase] = useState('');
  const [dataType, setDataType] = useState('WALLET_ADDRESS');
  const [data, setData] = useState('');
  const [retrieved, setRetrieved] = useState('');
  const [mfaSetup, setMfaSetup] = useState(null);
  const [mfaCode, setMfaCode] = useState('');
  const [message, setMessage] = useState('');

  const handleStore = async (e) => {
    e.preventDefault();
    try {
      await userApi.storeEncryptedData({ dataType, data, passphrase });
      setMessage('Data encrypted and stored securely');
      setData('');
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to store data');
    }
  };

  const handleRetrieve = async (e) => {
    e.preventDefault();
    try {
      const res = await userApi.retrieveEncryptedData({ dataType, passphrase });
      setRetrieved(res.data.data);
      setMessage('Data decrypted successfully');
    } catch {
      setMessage('Decryption failed - invalid passphrase');
      setRetrieved('');
    }
  };

  const handleEnableMfa = async () => {
    try {
      const res = await userApi.enableMfa();
      setMfaSetup(res.data.data);
      setMessage('Scan QR code with your authenticator app');
    } catch {
      setMessage('Failed to enable MFA');
    }
  };

  const handleConfirmMfa = async () => {
    try {
      await userApi.confirmMfa(mfaCode);
      setMessage('MFA enabled successfully');
      setMfaSetup(null);
    } catch {
      setMessage('Invalid MFA code');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Security Center</h1>
        <p className="page-subtitle">End-to-end encrypted vault and multi-factor authentication</p>
      </div>

      {message && <div className="alert alert-success">{message}</div>}

      <div className="grid-2">
        <div className="panel">
          <div className="panel-header"><span className="panel-title">Encrypted Data Vault</span></div>
          <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            Store sensitive data encrypted with your passphrase. Administrators and creators cannot access this data.
          </p>
          <form onSubmit={handleStore}>
            <div className="form-group">
              <label>Data Type</label>
              <select value={dataType} onChange={(e) => setDataType(e.target.value)}>
                <option value="WALLET_ADDRESS">Wallet Address</option>
                <option value="POOL_CREDENTIALS">Pool Credentials</option>
                <option value="API_KEYS">API Keys</option>
                <option value="PRIVATE_NOTES">Private Notes</option>
              </select>
            </div>
            <div className="form-group">
              <label>Data to Encrypt</label>
              <textarea value={data} onChange={(e) => setData(e.target.value)} rows={3} required />
            </div>
            <div className="form-group">
              <label>Encryption Passphrase</label>
              <input type="password" value={passphrase} onChange={(e) => setPassphrase(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">Encrypt & Store</button>
          </form>
        </div>

        <div className="panel">
          <div className="panel-header"><span className="panel-title">Retrieve Encrypted Data</span></div>
          <form onSubmit={handleRetrieve}>
            <div className="form-group">
              <label>Data Type</label>
              <select value={dataType} onChange={(e) => setDataType(e.target.value)}>
                <option value="WALLET_ADDRESS">Wallet Address</option>
                <option value="POOL_CREDENTIALS">Pool Credentials</option>
                <option value="API_KEYS">API Keys</option>
                <option value="PRIVATE_NOTES">Private Notes</option>
              </select>
            </div>
            <div className="form-group">
              <label>Encryption Passphrase</label>
              <input type="password" value={passphrase} onChange={(e) => setPassphrase(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-secondary">Decrypt & Retrieve</button>
          </form>
          {retrieved && (
            <div className="terminal" style={{ marginTop: '1rem', color: 'var(--accent-green)' }}>
              {retrieved}
            </div>
          )}
        </div>
      </div>

      <div className="panel" style={{ marginTop: '1.5rem' }}>
        <div className="panel-header"><span className="panel-title">Multi-Factor Authentication</span></div>
        {!mfaSetup ? (
          <div>
            <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              Add an extra layer of security with TOTP-based two-factor authentication.
            </p>
            <button className="btn btn-primary" onClick={handleEnableMfa}>Enable MFA</button>
          </div>
        ) : (
          <div>
            {mfaSetup.qrCodeBase64 && (
              <img src={`data:image/png;base64,${mfaSetup.qrCodeBase64}`} alt="MFA QR Code" style={{ marginBottom: '1rem' }} />
            )}
            <p style={{ fontFamily: 'var(--font-mono)', fontSize: '0.875rem', marginBottom: '1rem' }}>
              Secret: {mfaSetup.secret}
            </p>
            <div className="form-group">
              <label>Verification Code</label>
              <input type="text" value={mfaCode} onChange={(e) => setMfaCode(e.target.value)} maxLength={6} />
            </div>
            <button className="btn btn-primary" onClick={handleConfirmMfa}>Confirm MFA</button>
          </div>
        )}
      </div>

      <div className="panel" style={{ marginTop: '1.5rem' }}>
        <div className="panel-header"><span className="panel-title">Security Status</span></div>
        <div className="grid-3">
          <div><span className="status-dot status-online" /> AES-256 Encryption Active</div>
          <div><span className="status-dot status-online" /> BCrypt Password Hashing</div>
          <div><span className="status-dot status-online" /> JWT Token Authentication</div>
          <div><span className="status-dot status-online" /> Rate Limiting Enabled</div>
          <div><span className="status-dot status-online" /> Audit Logging Active</div>
          <div><span className="status-dot status-online" /> Zero-Trust Architecture</div>
        </div>
      </div>
    </div>
  );
}
