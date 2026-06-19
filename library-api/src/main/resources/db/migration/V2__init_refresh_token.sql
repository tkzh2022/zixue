CREATE TABLE refresh_token (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_account_id BIGINT       NOT NULL,
    token_hash      CHAR(64)     NOT NULL,
    issued_at       DATETIME(3)  NOT NULL,
    expires_at      DATETIME(3)  NOT NULL,
    revoked_at      DATETIME(3)  NULL,
    revoked_reason  VARCHAR(64)  NULL,
    user_agent      VARCHAR(255) NULL,
    ip              VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    KEY idx_refresh_token_user (user_account_id),
    KEY idx_refresh_token_expires (expires_at)
) COMMENT = 'JWT refresh token 持久化';
