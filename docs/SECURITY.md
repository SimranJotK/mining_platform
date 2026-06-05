# Security Model

## Zero-Trust Architecture

Every request is authenticated and authorized regardless of network origin. No implicit trust between services or users.

## Authentication

### JWT Tokens
- **Access Token**: 15-minute expiration (configurable)
- **Refresh Token**: 7-day expiration, stored as SHA-256 hash
- **Algorithm**: HMAC-SHA256 (HS256)
- Token type claim prevents refresh token misuse as access token

### Password Security
- **BCrypt** hashing with strength factor 12
- Account lockout after 5 failed login attempts
- Password minimum 8 characters

### Multi-Factor Authentication
- TOTP-based (RFC 6238)
- QR code generation for authenticator apps
- ±1 time step tolerance for clock drift

## Encryption

### User Data (E2E)
```
User Passphrase → PBKDF2 (65536 iterations) → AES-256 Key
Plaintext → AES-256-GCM Encrypt → Ciphertext + IV + Salt → Database
```

**Critical Rule**: Only the user with the correct passphrase can decrypt. Administrators and creators see only encrypted blobs.

### API Keys
- Encrypted with master key + user context
- Stored as encrypted blobs with IV prefix

### RSA Key Exchange
- 2048-bit RSA key pairs supported
- Public keys stored in user profile
- For future secure key exchange protocols

## Authorization (RBAC)

| Role | Access Level |
|------|-------------|
| ROLE_USER | Own data, mining stats, AI predictions |
| ROLE_ADMIN | User management, audit logs, platform analytics |
| ROLE_CREATOR | Full infrastructure, deploy services, configurations |

### Data Access Restrictions
- Admins **cannot** access `encrypted_user_data` decrypted content
- Creators **cannot** access customer encrypted information
- Audit logs track all access attempts

## Network Security

- **Rate Limiting**: 100 requests/minute per IP
- **CORS**: Configurable allowed origins
- **XSS Protection**: X-XSS-Protection header enabled
- **CSP**: Content-Security-Policy header
- **CSRF**: Disabled for stateless JWT API (no cookies)
- **SQL Injection**: Parameterized queries via JPA

## Audit System

All security-relevant actions are logged:
- Login/logout (success and failure)
- MFA events
- User suspension/activation
- Encrypted data operations
- Profile changes
- Admin actions

Audit log fields: user, action, resource, IP, user-agent, status, timestamp

## Session Management

- Stateless JWT (no server-side sessions)
- Refresh token rotation on use
- Logout revokes all refresh tokens for user
- No sensitive data in JWT claims (only email and roles)

## HTTPS

Production deployment requires HTTPS:
- Kubernetes Ingress with TLS
- Nginx reverse proxy with SSL termination
- Docker Compose: add Traefik or nginx SSL proxy

## Security Headers

```
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

## Threat Mitigation

| Threat | Mitigation |
|--------|-----------|
| Brute force | Account lockout + rate limiting |
| Token theft | Short-lived access tokens + refresh rotation |
| Data breach | E2E encryption, only ciphertext in DB |
| Privilege escalation | RBAC with method-level security |
| SQL injection | JPA parameterized queries |
| XSS | CSP headers + React auto-escaping |
| CSRF | Stateless API, no cookie auth |
