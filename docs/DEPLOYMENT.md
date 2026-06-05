# Deployment Guide

## Docker Compose (Recommended)

```bash
cd cloud
cp .env.example .env
# Edit .env with production secrets
docker-compose up -d
```

### Services

| Service | Port | URL |
|---------|------|-----|
| Frontend | 3000 | http://localhost:3000 |
| Backend API | 8080 | http://localhost:8080/api/v1 |
| AI Service | 5000 | http://localhost:5000 |
| MySQL | 3306 | localhost:3306 |

### Verify Deployment

```bash
# Backend health
curl http://localhost:8080/api/v1/actuator/health

# AI service health
curl http://localhost:5000/health

# Login test
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@platform.local","password":"User@123"}'
```

## Kubernetes Deployment

```bash
# Create namespace
kubectl apply -f cloud/kubernetes/namespace.yaml

# Deploy MySQL
kubectl apply -f cloud/kubernetes/mysql.yaml

# Wait for MySQL
kubectl wait --for=condition=ready pod -l app=mysql -n crypto-mining --timeout=120s

# Initialize database (run once)
kubectl exec -it deployment/mysql -n crypto-mining -- \
  mysql -u root -ppassword crypto_mining_platform < backend/src/main/resources/db/schema.sql

# Build and push images
docker build -t crypto-mining-backend:latest ./backend
docker build -t crypto-mining-frontend:latest ./frontend
docker build -t crypto-mining-ai:latest ./ai

# Deploy services
kubectl apply -f cloud/kubernetes/ai-service.yaml
kubectl apply -f cloud/kubernetes/backend.yaml
kubectl apply -f cloud/kubernetes/frontend.yaml
kubectl apply -f cloud/kubernetes/ingress.yaml
```

## Manual Development Setup

### 1. Database
```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```

### 3. AI Service
```bash
cd ai
pip install -r requirements.txt
python app.py
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_HOST | localhost | MySQL host |
| DB_PORT | 3306 | MySQL port |
| DB_NAME | crypto_mining_platform | Database name |
| DB_USER | root | Database user |
| DB_PASSWORD | password | Database password |
| JWT_SECRET | (required) | JWT signing key (32+ chars) |
| ENCRYPTION_MASTER_KEY | (required) | AES master key |
| AI_SERVICE_URL | http://localhost:5000 | AI microservice URL |
| MINING_SIMULATION | true | Enable simulation mode |
| CORS_ORIGINS | http://localhost:3000 | Allowed CORS origins |
| RATE_LIMIT | 100 | Requests per minute |

## Production Checklist

- [ ] Change all default passwords
- [ ] Set strong JWT_SECRET (32+ characters)
- [ ] Set ENCRYPTION_MASTER_KEY
- [ ] Enable HTTPS/TLS (Ingress or reverse proxy)
- [ ] Configure MySQL with strong root password
- [ ] Set `ddl-auto: validate` (never `create` in production)
- [ ] Configure log aggregation
- [ ] Set up database backups
- [ ] Configure monitoring alerts
- [ ] Review CORS origins

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci-cd.yml`):

1. **Backend**: Maven build + test
2. **Frontend**: npm build
3. **AI**: Python lint check
4. **Docker**: Build all images (on main branch)
5. **Security**: Trivy vulnerability scan
