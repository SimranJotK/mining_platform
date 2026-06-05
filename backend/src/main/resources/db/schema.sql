-- AI-Powered Cryptocurrency Mining Analytics Platform
-- MySQL 8.0+ Database Schema

CREATE DATABASE IF NOT EXISTS crypto_mining_platform
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE crypto_mining_platform;

-- Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    rsa_public_key TEXT,
    account_status ENUM('ACTIVE', 'SUSPENDED', 'PENDING', 'LOCKED') DEFAULT 'ACTIVE',
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_status (account_status)
);

-- User-Role mapping
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_hash (token_hash)
);

-- Encrypted User Data (E2E encrypted - admins/creators cannot decrypt)
CREATE TABLE encrypted_user_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    encrypted_payload LONGBLOB NOT NULL,
    iv VARCHAR(64) NOT NULL,
    key_salt VARCHAR(128) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_encrypted_user_data (user_id, data_type)
);

-- Mining Pools
CREATE TABLE mining_pools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_url VARCHAR(500) NOT NULL,
    pool_type ENUM('BTC', 'ETH', 'LTC', 'OTHER') DEFAULT 'BTC',
    api_key_required BOOLEAN DEFAULT TRUE,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Mining Workers
CREATE TABLE mining_workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pool_id BIGINT,
    worker_name VARCHAR(100) NOT NULL,
    worker_id VARCHAR(100),
    device_type ENUM('GPU', 'ASIC', 'CPU', 'SIMULATED') DEFAULT 'SIMULATED',
    status ENUM('ONLINE', 'OFFLINE', 'IDLE', 'ERROR', 'MAINTENANCE') DEFAULT 'OFFLINE',
    hash_rate DECIMAL(20, 4) DEFAULT 0,
    hash_rate_unit VARCHAR(10) DEFAULT 'MH/s',
    temperature DECIMAL(5, 2),
    power_consumption DECIMAL(10, 2),
    uptime_seconds BIGINT DEFAULT 0,
    last_seen_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pool_id) REFERENCES mining_pools(id) ON DELETE SET NULL,
    INDEX idx_workers_user (user_id),
    INDEX idx_workers_status (status)
);

-- Mining Statistics
CREATE TABLE mining_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    hash_rate DECIMAL(20, 4) NOT NULL,
    accepted_shares BIGINT DEFAULT 0,
    rejected_shares BIGINT DEFAULT 0,
    stale_shares BIGINT DEFAULT 0,
    estimated_earnings DECIMAL(20, 8) DEFAULT 0,
    efficiency DECIMAL(5, 2) DEFAULT 0,
    power_consumption DECIMAL(10, 2),
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES mining_workers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_stats_worker_time (worker_id, recorded_at),
    INDEX idx_stats_user_time (user_id, recorded_at)
);

-- AI Predictions
CREATE TABLE ai_predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    worker_id BIGINT,
    prediction_type ENUM(
        'PROFIT_FORECAST', 'REVENUE_FORECAST', 'ANOMALY_DETECTION',
        'WORKER_FAILURE', 'ENERGY_FORECAST', 'OPTIMIZATION', 'TREND_ANALYSIS'
    ) NOT NULL,
    prediction_data JSON NOT NULL,
    confidence_score DECIMAL(5, 4),
    valid_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (worker_id) REFERENCES mining_workers(id) ON DELETE SET NULL,
    INDEX idx_predictions_user (user_id, prediction_type)
);

-- Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('INFO', 'WARNING', 'ALERT', 'SUCCESS', 'SYSTEM') DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id, is_read)
);

-- Audit Logs
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    details JSON,
    status ENUM('SUCCESS', 'FAILURE', 'WARNING') DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
);

-- System Configurations
CREATE TABLE system_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    is_encrypted BOOLEAN DEFAULT FALSE,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Mining API Integrations
CREATE TABLE mining_api_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pool_id BIGINT NOT NULL,
    api_key_encrypted LONGBLOB,
    api_key_iv VARCHAR(64),
    integration_mode ENUM('SIMULATION', 'API', 'CGMINER', 'BFGMINER', 'STRATUM') DEFAULT 'SIMULATION',
    is_active BOOLEAN DEFAULT TRUE,
    last_sync_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pool_id) REFERENCES mining_pools(id) ON DELETE CASCADE
);

-- Seed Roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER', 'Standard user with mining monitoring access'),
    ('ROLE_ADMIN', 'Administrator with user and system management'),
    ('ROLE_CREATOR', 'Platform creator with full infrastructure control');

-- Seed Permissions
INSERT INTO permissions (name, description) VALUES
    ('VIEW_OWN_STATS', 'View personal mining statistics'),
    ('CONNECT_MINING', 'Connect mining accounts'),
    ('VIEW_AI_PREDICTIONS', 'View AI predictions'),
    ('VIEW_ENCRYPTED_DATA', 'View own encrypted data'),
    ('MANAGE_SETTINGS', 'Manage own settings'),
    ('MANAGE_USERS', 'Manage platform users'),
    ('SUSPEND_USERS', 'Suspend user accounts'),
    ('VIEW_SYSTEM_LOGS', 'Review system logs'),
    ('VIEW_PLATFORM_ANALYTICS', 'View platform analytics'),
    ('MANAGE_INFRASTRUCTURE', 'Full infrastructure control'),
    ('MANAGE_ADMINS', 'Manage administrator accounts'),
    ('DEPLOY_SERVICES', 'Deploy platform services'),
    ('CONFIGURE_APIS', 'Configure API integrations'),
    ('MONITOR_PLATFORM', 'Monitor platform health');

-- Role-Permission mappings
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN (
    'VIEW_OWN_STATS', 'CONNECT_MINING', 'VIEW_AI_PREDICTIONS',
    'VIEW_ENCRYPTED_DATA', 'MANAGE_SETTINGS'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name IN (
    'VIEW_OWN_STATS', 'VIEW_AI_PREDICTIONS', 'MANAGE_SETTINGS',
    'MANAGE_USERS', 'SUSPEND_USERS', 'VIEW_SYSTEM_LOGS', 'VIEW_PLATFORM_ANALYTICS'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_CREATOR' AND p.name IN (
    'VIEW_OWN_STATS', 'VIEW_AI_PREDICTIONS', 'MANAGE_SETTINGS',
    'MANAGE_USERS', 'SUSPEND_USERS', 'VIEW_SYSTEM_LOGS', 'VIEW_PLATFORM_ANALYTICS',
    'MANAGE_INFRASTRUCTURE', 'MANAGE_ADMINS', 'DEPLOY_SERVICES', 'CONFIGURE_APIS', 'MONITOR_PLATFORM'
);

-- Seed Mining Pools
INSERT INTO mining_pools (name, api_url, pool_type, api_key_required) VALUES
    ('Slush Pool', 'https://slushpool.com/accounts/profile/json/btc', 'BTC', TRUE),
    ('F2Pool', 'https://api.f2pool.com/btc', 'BTC', TRUE),
    ('Antpool', 'https://antpool.com/api', 'BTC', TRUE),
    ('Ethermine', 'https://api.ethermine.org', 'ETH', FALSE),
    ('Simulation Pool', 'http://localhost:8080/api/v1/simulation/pool', 'BTC', FALSE);

-- Seed System Configurations
INSERT INTO system_configurations (config_key, config_value, description) VALUES
    ('mining.simulation.enabled', 'true', 'Enable mining simulation mode'),
    ('ai.service.url', 'http://ai-service:5000', 'AI microservice URL'),
    ('security.rate_limit.requests', '100', 'Rate limit requests per minute'),
    ('security.jwt.expiration', '900', 'JWT expiration in seconds'),
    ('security.refresh.expiration', '604800', 'Refresh token expiration in seconds');

-- Default users (passwords: Creator@123, Admin@123, User@123)
-- BCrypt hashes generated with strength 12
INSERT INTO users (email, username, password_hash, first_name, last_name, account_status) VALUES
    ('creator@platform.local', 'creator', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oYbP0zqK9K0u', 'Platform', 'Creator', 'ACTIVE'),
    ('admin@platform.local', 'admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oYbP0zqK9K0u', 'System', 'Admin', 'ACTIVE'),
    ('user@platform.local', 'user', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oYbP0zqK9K0u', 'Demo', 'User', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'creator@platform.local' AND r.name = 'ROLE_CREATOR';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@platform.local' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'user@platform.local' AND r.name = 'ROLE_USER';
