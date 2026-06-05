# AI-Powered Cryptocurrency Mining Analytics and Monitoring Platform

A production-ready full-stack platform for monitoring cryptocurrency mining operations, collecting statistics via mining pool APIs, and providing AI-powered analytics and predictions.

> **Note:** This platform does not perform real mining. It monitors and analyzes mining statistics.

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   React     │────▶│  API Gateway │────▶│  Spring Boot│
│  Frontend   │     │  (Backend)   │     │   Backend   │
└─────────────┘     └──────────────┘     └──────┬──────┘
                                                  │
                    ┌──────────────┐     ┌─────────▼──────┐
                    │  AI Service  │◀───▶│     MySQL     │
                    │   (Python)   │     │   Database    │
                    └──────────────┘     └───────────────┘
```

## Project Structure

```
Project_2/
├── backend/          # Java Spring Boot API (JWT, Security, REST)
├── frontend/         # React.js SPA (15 dashboards)
├── ai/               # Python AI microservice (scikit-learn)
├── cloud/            # Docker, Kubernetes, GitHub Actions
└── docs/             # Architecture, API, database documentation
```

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Python 3.11+
- Docker & Docker Compose
- MySQL 8.0+

### Local Desktop (No Docker)

```bash
# Start all services (AI :5001, Backend :8081, Frontend :3000)
./scripts/start-local.sh

# Stop all services
./scripts/start-local.sh stop
```

> **Note:** Ports 8081 and 5001 are used locally because macOS often reserves 8080 (Tomcat) and 5000 (AirPlay). Production Docker uses 8080/5000 with MySQL.

### Using Docker Compose (Production MySQL)

```bash
cd cloud
docker-compose up -d
```

Services:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- AI Service: http://localhost:5000
- MySQL: localhost:3306

### Manual Development

**Database:**
```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

**Backend:**
```bash
cd backend
# Requires Java 17 (set JAVA_HOME if needed)
./mvnw spring-boot:run
```

**AI Service:**
```bash
cd ai
pip install -r requirements.txt
python app.py
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Default Accounts

| Role    | Email                    | Password     |
|---------|--------------------------|--------------|
| Creator | creator@platform.local   | Creator@123  |
| Admin   | admin@platform.local     | Admin@123    |
| User    | user@platform.local      | User@123     |

## Security

- Zero-Trust Architecture
- JWT + Refresh Token authentication
- BCrypt password hashing
- AES-256 end-to-end encrypted user data
- RSA key exchange
- Role-Based Access Control (USER, ADMIN, CREATOR)
- MFA (TOTP)
- Audit logging
- Rate limiting

**Critical:** Administrators and creators cannot access decrypted customer private data.

## Roles & Permissions

| Permission              | USER | ADMIN | CREATOR |
|-------------------------|------|-------|---------|
| View own mining stats   | ✓    | ✓     | ✓       |
| Connect mining accounts | ✓    | -     | -       |
| View AI predictions     | ✓    | ✓     | ✓       |
| Manage users            | -    | ✓     | ✓       |
| System configuration    | -    | -     | ✓       |
| Deploy services         | -    | -     | ✓       |
| Access encrypted data   | Own  | ✗     | ✗       |

## Documentation

- [Architecture Overview](docs/ARCHITECTURE.md)
- [API Reference](docs/API.md)
- [Database Schema](docs/DATABASE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Security Model](docs/SECURITY.md)

## License

Proprietary - All rights reserved.
