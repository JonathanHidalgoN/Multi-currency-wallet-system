-- Add idempotency key column to transactions table for idempotent operations
-- The idempotency key is provided by the client to prevent duplicate transactions

ALTER TABLE transactions
ADD COLUMN idempotency_key VARCHAR(255) UNIQUE;
