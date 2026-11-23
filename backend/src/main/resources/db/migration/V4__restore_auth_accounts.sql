CREATE TABLE IF NOT EXISTS auth_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_accounts_provider_account
        UNIQUE (provider, provider_account_id),
    CONSTRAINT fk_auth_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_auth_accounts_provider
        CHECK (provider IN ('LOCAL', 'GOOGLE'))
);

CREATE INDEX IF NOT EXISTS idx_auth_accounts_user_id
    ON auth_accounts (user_id);
