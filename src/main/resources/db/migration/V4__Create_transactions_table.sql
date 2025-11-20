CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fee NUMERIC(10, 6),
    recipient_currency VARCHAR(3),
    exchange_rate NUMERIC(10, 6),
    recipient_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);
