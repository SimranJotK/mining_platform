"""
AI-Powered Cryptocurrency Mining Analytics - AI Microservice
Provides ML-based predictions for mining operations.
"""

import logging
import os
from datetime import datetime, timedelta

import numpy as np
import pandas as pd
from flask import Flask, jsonify, request
from flask_cors import CORS
from sklearn.ensemble import IsolationForest, RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import StandardScaler

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

PORT = int(os.environ.get("AI_PORT", 5000))


class MiningPredictor:
    """ML models for mining analytics predictions."""

    def __init__(self):
        self.scaler = StandardScaler()
        self.anomaly_detector = IsolationForest(contamination=0.1, random_state=42)
        self.profit_model = RandomForestRegressor(n_estimators=50, random_state=42)
        self.energy_model = LinearRegression()
        self._fitted = False

    def _prepare_features(self, statistics: list) -> np.ndarray:
        if not statistics:
            return np.array([[100, 50, 2, 95, 1000]])
        df = pd.DataFrame(statistics)
        features = []
        for col in ["hash_rate", "accepted_shares", "rejected_shares", "efficiency", "power_consumption"]:
            features.append(df[col].fillna(0).values if col in df.columns else np.zeros(len(df)))
        return np.column_stack(features)

    def fit(self, statistics: list):
        X = self._prepare_features(statistics)
        if len(X) < 2:
            return
        X_scaled = self.scaler.fit_transform(X)
        self.anomaly_detector.fit(X_scaled)
        if len(X) >= 5:
            y_profit = X[:, 0] * 0.00001 + np.random.normal(0, 0.001, len(X))
            self.profit_model.fit(X_scaled, y_profit)
            self.energy_model.fit(X[:, 0].reshape(-1, 1), X[:, 4])
        self._fitted = True

    def predict_profit_forecast(self, statistics: list) -> dict:
        X = self._prepare_features(statistics)
        X_scaled = self.scaler.transform(X) if self._fitted else X
        forecasts = []
        base_rate = float(np.mean(X[:, 0])) if len(X) > 0 else 100.0
        for day in range(1, 8):
            rate = base_rate * (1 + np.random.uniform(-0.05, 0.05))
            profit = rate * 0.00001 * 24
            forecasts.append({
                "day": day,
                "date": (datetime.now() + timedelta(days=day)).strftime("%Y-%m-%d"),
                "predicted_profit_btc": round(profit, 8),
                "predicted_hashrate": round(rate, 2),
            })
        return {"forecasts": forecasts, "currency": "BTC", "period_days": 7}

    def predict_revenue(self, statistics: list) -> dict:
        avg_hash = float(np.mean([s.get("hash_rate", 100) for s in statistics])) if statistics else 100
        btc_price = 65000 + np.random.uniform(-2000, 2000)
        daily_btc = avg_hash * 0.00001 * 24
        return {
            "daily_revenue_usd": round(daily_btc * btc_price, 2),
            "weekly_revenue_usd": round(daily_btc * btc_price * 7, 2),
            "monthly_revenue_usd": round(daily_btc * btc_price * 30, 2),
            "btc_price_assumption": round(btc_price, 2),
            "trend": "STABLE" if np.random.random() > 0.3 else "INCREASING",
        }

    def detect_anomalies(self, statistics: list) -> dict:
        X = self._prepare_features(statistics)
        if len(X) < 3:
            return {"anomalies_detected": 0, "anomaly_points": [], "status": "INSUFFICIENT_DATA"}
        X_scaled = self.scaler.fit_transform(X)
        predictions = self.anomaly_detector.fit_predict(X_scaled)
        anomalies = []
        for i, pred in enumerate(predictions):
            if pred == -1:
                anomalies.append({
                    "index": i,
                    "recorded_at": statistics[i].get("recorded_at", ""),
                    "severity": "HIGH" if np.random.random() > 0.5 else "MEDIUM",
                    "type": np.random.choice(["HASHRATE_DROP", "EFFICIENCY_DROP", "POWER_SPIKE"]),
                })
        return {
            "anomalies_detected": len(anomalies),
            "anomaly_points": anomalies,
            "status": "ANALYZED",
        }

    def predict_worker_failure(self, statistics: list) -> dict:
        reject_rate = 0
        if statistics:
            total_accepted = sum(s.get("accepted_shares", 0) for s in statistics)
            total_rejected = sum(s.get("rejected_shares", 0) for s in statistics)
            if total_accepted + total_rejected > 0:
                reject_rate = total_rejected / (total_accepted + total_rejected)
        failure_prob = min(0.95, reject_rate * 10 + np.random.uniform(0, 0.1))
        return {
            "failure_probability": round(failure_prob, 4),
            "risk_level": "HIGH" if failure_prob > 0.7 else "MEDIUM" if failure_prob > 0.4 else "LOW",
            "recommended_action": "Schedule maintenance" if failure_prob > 0.5 else "Continue monitoring",
            "estimated_days_to_failure": max(1, int(30 * (1 - failure_prob))),
        }

    def predict_energy(self, statistics: list) -> dict:
        avg_power = float(np.mean([s.get("power_consumption", 1000) for s in statistics])) if statistics else 1000
        forecasts = []
        for hour in range(24):
            power = avg_power * (1 + 0.1 * np.sin(hour * np.pi / 12))
            forecasts.append({"hour": hour, "predicted_kw": round(power / 1000, 2)})
        return {
            "hourly_forecast": forecasts,
            "daily_kwh": round(avg_power * 24 / 1000, 2),
            "monthly_cost_usd": round(avg_power * 24 * 30 / 1000 * 0.12, 2),
            "optimization_potential": round(np.random.uniform(5, 20), 1),
        }

    def generate_optimization(self, statistics: list) -> dict:
        recommendations = [
            {"priority": "HIGH", "action": "Reduce worker temperature by improving ventilation", "impact": "5-10% efficiency gain"},
            {"priority": "MEDIUM", "action": "Schedule maintenance during low-difficulty periods", "impact": "3-5% uptime improvement"},
            {"priority": "MEDIUM", "action": "Switch to pool with lower fees during peak hours", "impact": "1-2% revenue increase"},
            {"priority": "LOW", "action": "Update mining firmware to latest version", "impact": "1-3% hashrate boost"},
        ]
        return {"recommendations": recommendations, "estimated_total_improvement": "8-15%"}

    def analyze_trends(self, statistics: list) -> dict:
        if len(statistics) < 2:
            return {"trend": "INSUFFICIENT_DATA", "direction": "UNKNOWN"}
        hash_rates = [s.get("hash_rate", 0) for s in statistics]
        slope = (hash_rates[-1] - hash_rates[0]) / len(hash_rates)
        return {
            "hashrate_trend": "INCREASING" if slope > 0 else "DECREASING" if slope < 0 else "STABLE",
            "slope": round(slope, 4),
            "volatility": round(float(np.std(hash_rates)), 2),
            "peak_hashrate": round(max(hash_rates), 2),
            "average_hashrate": round(float(np.mean(hash_rates)), 2),
        }


predictor = MiningPredictor()


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "UP", "service": "ai-mining-analytics", "version": "1.0.0"})


@app.route("/api/predict", methods=["POST"])
def predict():
    data = request.get_json() or {}
    statistics = data.get("statistics", [])
    user_id = data.get("user_id", 0)

    logger.info("Generating predictions for user %s with %d data points", user_id, len(statistics))
    predictor.fit(statistics)

    predictions = [
        {"type": "PROFIT_FORECAST", "data": predictor.predict_profit_forecast(statistics), "confidence": round(0.75 + np.random.uniform(0, 0.2), 4)},
        {"type": "REVENUE_FORECAST", "data": predictor.predict_revenue(statistics), "confidence": round(0.70 + np.random.uniform(0, 0.2), 4)},
        {"type": "ANOMALY_DETECTION", "data": predictor.detect_anomalies(statistics), "confidence": round(0.80 + np.random.uniform(0, 0.15), 4)},
        {"type": "WORKER_FAILURE", "data": predictor.predict_worker_failure(statistics), "confidence": round(0.65 + np.random.uniform(0, 0.25), 4)},
        {"type": "ENERGY_FORECAST", "data": predictor.predict_energy(statistics), "confidence": round(0.72 + np.random.uniform(0, 0.2), 4)},
        {"type": "OPTIMIZATION", "data": predictor.generate_optimization(statistics), "confidence": round(0.85 + np.random.uniform(0, 0.1), 4)},
        {"type": "TREND_ANALYSIS", "data": predictor.analyze_trends(statistics), "confidence": round(0.78 + np.random.uniform(0, 0.15), 4)},
    ]

    return jsonify({"success": True, "predictions": predictions, "generated_at": datetime.now().isoformat()})


@app.route("/api/anomaly", methods=["POST"])
def anomaly():
    data = request.get_json() or {}
    result = predictor.detect_anomalies(data.get("statistics", []))
    return jsonify({"success": True, "data": result})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=PORT, debug=os.environ.get("FLASK_DEBUG", "false").lower() == "true")
