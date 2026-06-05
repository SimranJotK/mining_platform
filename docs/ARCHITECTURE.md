# Architecture Overview

## System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  React SPA (15 Pages) — Dark Technical Dashboard UI              │
└──────────────────────────┬───────────────────────────────────────┘
                           │ HTTPS / REST
┌──────────────────────────▼───────────────────────────────────────┐
│                      API Gateway Layer                           │
│  Spring Boot Backend (Port 8080)                                 │
│  ├── JWT Authentication Filter                                   │
│  ├── Rate Limiting Filter                                        │
│  ├── CORS / XSS / CSRF Protection                               │
│  └── Role-Based Access Control                                   │
└──────────┬───────────────────────────────┬───────────────────────┘
           │                               │
┌──────────▼──────────┐         ┌──────────▼──────────┐
│   Service Layer     │         │   AI Microservice   │
│  ├── AuthService    │◄───────►│   Python/Flask      │
│  ├── MiningService  │  HTTP   │   scikit-learn      │
│  ├── AiServiceClient│         │   Port 5000         │
│  ├── UserService    │         └─────────────────────┘
│  ├── AuditService   │
│  ├── EncryptionSvc  │
│  └── SystemService  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│  Repository Layer   │
│  Spring Data JPA    │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   MySQL Database    │
│   Port 3306         │
└─────────────────────┘
```

## Layer Responsibilities

### Frontend (React)
- 15 dashboard pages with CGMiner-inspired dark theme
- JWT token management with automatic refresh
- Real-time data polling (30s intervals)
- Recharts visualization
- Role-based navigation

### Backend (Spring Boot)
- **Controller Layer**: REST API endpoints with validation
- **Service Layer**: Business logic, encryption, audit
- **Repository Layer**: JPA data access
- **Security Layer**: JWT, BCrypt, MFA, rate limiting
- **Integration Layer**: Mining pool API connectors
- **Scheduler**: Mining data collection (60s interval)

### AI Service (Python)
- Profit/revenue forecasting (RandomForest)
- Anomaly detection (IsolationForest)
- Worker failure prediction
- Energy consumption forecasting
- Optimization recommendations
- Trend analysis

### Cloud
- Docker Compose for local/staging deployment
- Kubernetes manifests for production
- GitHub Actions CI/CD pipeline

## Entity Relationship Diagram

```
┌─────────┐     ┌────────────┐     ┌─────────────┐
│  Users  │────►│ User_Roles │◄────│    Roles    │
└────┬────┘     └────────────┘     └──────┬──────┘
     │                                    │
     │         ┌──────────────────┐       │
     ├────────►│ Encrypted_User   │       │
     │         │     _Data        │       ▼
     │         └──────────────────┘  ┌─────────────┐
     │                               │ Permissions │
     ├────────►┌──────────────┐      └─────────────┘
     │         │Mining_Workers│◄───┐
     │         └──────┬───────┘    │
     │                │            │
     │         ┌──────▼───────┐   │  ┌─────────────┐
     │         │Mining_Stats  │   └──│Mining_Pools │
     │         └──────────────┘      └─────────────┘
     │
     ├────────►┌──────────────┐
     │         │AI_Predictions│
     │         └──────────────┘
     │
     ├────────►┌──────────────┐     ┌──────────────┐
     │         │Notifications │     │  Audit_Logs  │
     │         └──────────────┘     └──────────────┘
     │
     └────────►┌──────────────────┐
               │Mining_API_Integr.│
               └──────────────────┘
```

## Microservices-Ready Design

The monolithic backend is structured for future decomposition:

| Future Service | Current Module |
|---------------|----------------|
| Auth Service | `security/`, `service/AuthService` |
| Mining Service | `service/MiningService`, `integration/` |
| AI Service | Already separate (Python) |
| Notification Service | `service/NotificationService` |
| Audit Service | `service/AuditService` |

## Future Scope

- Stratum protocol communication
- CGMiner/BFGMiner direct integration
- ASIC device connectivity via SNMP/API
- Distributed mining node management
- Blockchain reward verification
- Multi-cloud deployment (AWS/GCP/Azure)
