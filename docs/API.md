# API Reference

Base URL: `http://localhost:8080/api/v1`

All authenticated endpoints require: `Authorization: Bearer <access_token>`

## Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register new user | Public |
| POST | `/auth/login` | Login (returns JWT) | Public |
| POST | `/auth/refresh` | Refresh access token | Public |
| POST | `/auth/logout` | Revoke refresh token | Required |

### Register Request
```json
{
  "email": "user@example.com",
  "username": "miner01",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "encryptionPassphrase": "my-vault-key"
}
```

### Login Request
```json
{
  "email": "user@platform.local",
  "password": "User@123",
  "mfaCode": "123456"
}
```

### Auth Response
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "mfaRequired": false,
    "user": {
      "id": 1,
      "email": "user@platform.local",
      "username": "user",
      "roles": ["ROLE_USER"],
      "mfaEnabled": false
    }
  }
}
```

## Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard/summary` | Dashboard overview stats |

## Mining

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/mining/workers` | List user's workers |
| POST | `/mining/workers` | Create new worker |
| GET | `/mining/statistics?hours=24` | Historical statistics |
| GET | `/mining/pools` | Available mining pools |

## AI Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/ai/predictions` | Get stored predictions |
| POST | `/ai/predictions/generate` | Generate new AI predictions |

## User Profile

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/profile` | Get user profile |
| PUT | `/users/profile` | Update profile |
| POST | `/users/encrypted-data` | Store encrypted data |
| POST | `/users/encrypted-data/retrieve` | Decrypt and retrieve data |
| POST | `/users/mfa/enable` | Start MFA setup |
| POST | `/users/mfa/confirm` | Confirm MFA with TOTP code |

## Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | List notifications |
| POST | `/notifications/{id}/read` | Mark as read |
| POST | `/notifications/read-all` | Mark all as read |

## Admin (ROLE_ADMIN, ROLE_CREATOR)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | List all users |
| POST | `/admin/users/{id}/suspend` | Suspend user |
| POST | `/admin/users/{id}/activate` | Activate user |
| GET | `/admin/audit-logs?page=0&size=20` | Audit log history |
| GET | `/admin/analytics` | Platform analytics |
| POST | `/admin/notifications/broadcast` | Broadcast notification |

## Creator (ROLE_CREATOR only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/creator/health` | Infrastructure health |
| GET | `/creator/configurations` | System configurations |
| PUT | `/creator/configurations/{key}` | Update configuration |
| POST | `/creator/deploy` | Deploy service |

## System

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/system/health` | System health check |
| GET | `/simulation/pool` | Simulated pool stats |

## AI Microservice (Port 5000)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Service health |
| POST | `/api/predict` | Generate ML predictions |
| POST | `/api/anomaly` | Anomaly detection only |

## Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-06-05T10:00:00"
}
```

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized |
| 403 | Forbidden (insufficient role) |
| 404 | Resource Not Found |
| 429 | Rate Limit Exceeded |
| 500 | Internal Server Error |
