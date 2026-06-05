import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { aiApi } from '../api/client';

const TYPE_LABELS = {
  PROFIT_FORECAST: 'Profit Forecast',
  REVENUE_FORECAST: 'Revenue Forecast',
  ANOMALY_DETECTION: 'Anomaly Detection',
  WORKER_FAILURE: 'Worker Failure Prediction',
  ENERGY_FORECAST: 'Energy Consumption',
  OPTIMIZATION: 'Optimization',
  TREND_ANALYSIS: 'Trend Analysis',
};

export default function AiAnalyticsPage() {
  const [predictions, setPredictions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [selected, setSelected] = useState(null);

  const fetchPredictions = async () => {
    try {
      const res = await aiApi.getPredictions();
      setPredictions(res.data.data || []);
      if (res.data.data?.length && !selected) setSelected(res.data.data[0].predictionType);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchPredictions(); }, []);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      const res = await aiApi.generatePredictions();
      setPredictions(res.data.data || []);
      if (res.data.data?.length) setSelected(res.data.data[0].predictionType);
    } catch { /* ignore */ }
    setGenerating(false);
  };

  const selectedPred = predictions.find((p) => p.predictionType === selected);
  const profitData = selectedPred?.predictionType === 'PROFIT_FORECAST'
    ? (selectedPred.predictionData?.forecasts || []).map((f) => ({
        day: `Day ${f.day}`, profit: f.predicted_profit_btc * 1000000,
      }))
    : [];

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">AI Analytics</h1>
          <p className="page-subtitle">Machine learning predictions and optimization insights</p>
        </div>
        <button className="btn btn-primary" onClick={handleGenerate} disabled={generating}>
          {generating ? 'Generating...' : 'Generate Predictions'}
        </button>
      </div>

      {loading ? <div className="loading">Loading AI analytics...</div> : (
        <div className="grid-2">
          <div>
            <div className="panel" style={{ marginBottom: '1rem' }}>
              <div className="panel-header"><span className="panel-title">Prediction Models</span></div>
              {predictions.map((p) => (
                <div key={p.id} onClick={() => setSelected(p.predictionType)}
                  style={{
                    padding: '0.75rem 1rem', cursor: 'pointer', borderBottom: '1px solid var(--border-color)',
                    background: selected === p.predictionType ? 'rgba(33,150,243,0.08)' : 'transparent',
                  }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ fontWeight: 500, fontSize: '0.875rem' }}>{TYPE_LABELS[p.predictionType] || p.predictionType}</span>
                    <span className="badge badge-blue">{((p.confidenceScore || 0) * 100).toFixed(0)}%</span>
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                    {new Date(p.createdAt).toLocaleString()}
                  </div>
                </div>
              ))}
              {predictions.length === 0 && (
                <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No predictions yet. Click "Generate Predictions" to run AI models.
                </div>
              )}
            </div>
          </div>

          <div className="panel">
            <div className="panel-header">
              <span className="panel-title">{TYPE_LABELS[selected] || 'Select a prediction'}</span>
            </div>
            {selectedPred ? (
              <>
                {profitData.length > 0 && (
                  <ResponsiveContainer width="100%" height={200}>
                    <LineChart data={profitData}>
                      <XAxis dataKey="day" stroke="#6b6b6b" fontSize={11} />
                      <YAxis stroke="#6b6b6b" fontSize={11} />
                      <Tooltip contentStyle={{ background: '#2d2d2d', border: '1px solid #3a3a3a' }} />
                      <Line type="monotone" dataKey="profit" stroke="#00c853" dot />
                    </LineChart>
                  </ResponsiveContainer>
                )}
                <div className="terminal" style={{ marginTop: '1rem', color: 'var(--text-primary)' }}>
                  <pre style={{ whiteSpace: 'pre-wrap', color: 'var(--text-secondary)' }}>
                    {JSON.stringify(selectedPred.predictionData, null, 2)}
                  </pre>
                </div>
              </>
            ) : (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>Select a prediction to view details</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
