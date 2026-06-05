# Database Schema

Database: `crypto_mining_platform` (MySQL 8.0+)

## Tables

### users
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | Auto-increment ID |
| email | VARCHAR(255) UNIQUE | User email |
| username | VARCHAR(100) UNIQUE | Display name |
| password_hash | VARCHAR(255) | BCrypt hashed password |
| first_name | VARCHAR(100) | First name |
| last_name | VARCHAR(100) | Last name |
| mfa_enabled | BOOLEAN | MFA status |
| mfa_secret | VARCHAR(255) | TOTP secret |
| rsa_public_key | TEXT | User RSA public key |
| account_status | ENUM | ACTIVE, SUSPENDED, PENDING, LOCKED |
| failed_login_attempts | INT | Brute force counter |
| last_login_at | TIMESTAMP | Last login time |

### encrypted_user_data
Stores **only encrypted values**. Admins/creators cannot decrypt.

| Column | Type | Description |
|--------|------|-------------|
| user_id | BIGINT FK | Owner |
| data_type | VARCHAR(100) | WALLET_ADDRESS, POOL_CREDENTIALS, etc. |
| encrypted_payload | LONGBLOB | AES-256-GCM ciphertext |
| iv | VARCHAR(64) | Initialization vector |
| key_salt | VARCHAR(128) | PBKDF2 salt for user key |

### mining_workers
| Column | Type | Description |
|--------|------|-------------|
| user_id | BIGINT FK | Owner |
| pool_id | BIGINT FK | Connected pool |
| worker_name | VARCHAR(100) | Display name |
| device_type | ENUM | GPU, ASIC, CPU, SIMULATED |
| status | ENUM | ONLINE, OFFLINE, IDLE, ERROR, MAINTENANCE |
| hash_rate | DECIMAL(20,4) | Current hash rate |
| temperature | DECIMAL(5,2) | Device temperature |
| power_consumption | DECIMAL(10,2) | Watts |
| uptime_seconds | BIGINT | Total uptime |

### mining_statistics
| Column | Type | Description |
|--------|------|-------------|
| worker_id | BIGINT FK | Source worker |
| hash_rate | DECIMAL(20,4) | Recorded hash rate |
| accepted_shares | BIGINT | Accepted share count |
| rejected_shares | BIGINT | Rejected share count |
| estimated_earnings | DECIMAL(20,8) | BTC earnings estimate |
| efficiency | DECIMAL(5,2) | Mining efficiency % |
| recorded_at | TIMESTAMP | Recording timestamp |

### ai_predictions
| Column | Type | Description |
|--------|------|-------------|
| prediction_type | ENUM | PROFIT_FORECAST, ANOMALY_DETECTION, etc. |
| prediction_data | JSON | ML model output |
| confidence_score | DECIMAL(5,4) | Model confidence |

### audit_logs
| Column | Type | Description |
|--------|------|-------------|
| action | VARCHAR(100) | Action performed |
| resource_type | VARCHAR(100) | Affected resource |
| ip_address | VARCHAR(45) | Client IP |
| status | ENUM | SUCCESS, FAILURE, WARNING |
| details | JSON | Additional context |

## Role-Permission Matrix

| Permission | USER | ADMIN | CREATOR |
|-----------|------|-------|---------|
| VIEW_OWN_STATS | ✓ | ✓ | ✓ |
| CONNECT_MINING | ✓ | - | - |
| VIEW_AI_PREDICTIONS | ✓ | ✓ | ✓ |
| VIEW_ENCRYPTED_DATA | Own | ✗ | ✗ |
| MANAGE_USERS | - | ✓ | ✓ |
| SUSPEND_USERS | - | ✓ | ✓ |
| VIEW_SYSTEM_LOGS | - | ✓ | ✓ |
| MANAGE_INFRASTRUCTURE | - | - | ✓ |
| DEPLOY_SERVICES | - | - | ✓ |

## Indexes

- `idx_users_email` — Fast login lookup
- `idx_workers_user` — User worker queries
- `idx_stats_worker_time` — Time-series statistics
- `idx_audit_created` — Audit log pagination
- `idx_predictions_user` — AI prediction lookup

## Initialization

Schema and seed data: `backend/src/main/resources/db/schema.sql`

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```
