CREATE TABLE oauth_accounts (
    oauth_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    CONSTRAINT uq_oauth_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_oauth_accounts_user_id ON oauth_accounts (user_id);
