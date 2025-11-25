CREATE TABLE wallet_balances (
    wallet_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (wallet_id, currency),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);
