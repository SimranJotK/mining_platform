#!/usr/bin/env bash
# Start all platform services on local desktop (no Docker required)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PID_DIR="$ROOT/.local-pids"
LOG_DIR="$ROOT/.local-logs"
mkdir -p "$PID_DIR" "$LOG_DIR" "$ROOT/backend/data"

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 21 2>/dev/null || true)}"

stop_service() {
  local name=$1
  local pidfile="$PID_DIR/$name.pid"
  if [[ -f "$pidfile" ]]; then
    local pid
    pid=$(cat "$pidfile")
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      sleep 1
    fi
    rm -f "$pidfile"
  fi
}

if [[ "${1:-}" == "stop" ]]; then
  stop_service ai
  stop_service backend
  stop_service frontend
  echo "All services stopped."
  exit 0
fi

echo "=== Crypto Mining Analytics Platform — Local Start ==="

# AI Service (port 5001 — macOS uses 5000 for AirPlay)
export AI_PORT=5001
stop_service ai
echo "Starting AI service (port $AI_PORT)..."
cd "$ROOT/ai"
if [[ ! -d .venv ]]; then
  python3 -m venv .venv
  .venv/bin/pip install -q -r requirements.txt
fi
setsid .venv/bin/python app.py > "$LOG_DIR/ai.log" 2>&1 &
echo $! > "$PID_DIR/ai.pid"

# Backend
stop_service backend
echo "Starting Backend (port 8081, profile: local)..."
cd "$ROOT/backend"
export SERVER_PORT=8081
if [[ -n "$JAVA_HOME" ]]; then export JAVA_HOME; fi
setsid ./mvnw spring-boot:run -Dspring-boot.run.profiles=local -q \
  > "$LOG_DIR/backend.log" 2>&1 &
echo $! > "$PID_DIR/backend.pid"

# Frontend
stop_service frontend
echo "Starting Frontend (port 3000)..."
cd "$ROOT/frontend"
if [[ ! -d node_modules ]]; then
  npm install --silent
fi
setsid npm run dev -- --host 0.0.0.0 --port 3000 > "$LOG_DIR/frontend.log" 2>&1 &
echo $! > "$PID_DIR/frontend.pid"

echo ""
echo "Waiting for services to become ready..."
for i in $(seq 1 60); do
  AI_OK=0; BE_OK=0; FE_OK=0
  curl -sf "http://localhost:${AI_PORT:-5001}/health" >/dev/null 2>&1 && AI_OK=1
  curl -sf http://localhost:8081/api/v1/actuator/health >/dev/null 2>&1 && BE_OK=1
  curl -sf http://localhost:3000 >/dev/null 2>&1 && FE_OK=1
  if [[ $AI_OK -eq 1 && $BE_OK -eq 1 && $FE_OK -eq 1 ]]; then
    echo ""
    echo "All services are UP!"
    echo "  Frontend:  http://localhost:3000"
    echo "  Backend:   http://localhost:8081/api/v1"
    echo "  AI:        http://localhost:${AI_PORT:-5001}"
    echo ""
    echo "Login: user@platform.local / User@123"
    echo "Logs:  $LOG_DIR/"
    echo "Stop:  $0 stop"
    exit 0
  fi
  sleep 2
done

echo "Timeout waiting for services. Check logs in $LOG_DIR/"
tail -20 "$LOG_DIR/backend.log" 2>/dev/null || true
exit 1
