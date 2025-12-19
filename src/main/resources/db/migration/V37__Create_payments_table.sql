-- V37: Create payments table for revenue tracking
-- This table stores all payment transactions for subscriptions

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
    stripe_payment_id VARCHAR(255) UNIQUE,
    stripe_invoice_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_subscription_id ON payments(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_paid_at ON payments(paid_at);
CREATE INDEX IF NOT EXISTS idx_payments_stripe_payment_id ON payments(stripe_payment_id);

-- Add comments
COMMENT ON TABLE payments IS 'Stores all payment transactions for subscriptions';
COMMENT ON COLUMN payments.status IS 'PENDING, SUCCEEDED, FAILED, REFUNDED, CANCELED';
COMMENT ON COLUMN payments.payment_type IS 'SUBSCRIPTION_NEW, SUBSCRIPTION_RENEWAL, SUBSCRIPTION_UPGRADE, REFUND';

-- Insert sample payment data if subscriptions exist
INSERT INTO payments (user_id, subscription_id, amount, currency, status, payment_type, description, paid_at)
SELECT 
    s.user_id,
    s.id,
    CASE WHEN s.plan_type = 'YEARLY' THEN 99.00 ELSE 9.99 END,
    'USD',
    'SUCCEEDED',
    'SUBSCRIPTION_NEW',
    'Initial subscription payment',
    s.created_at
FROM subscriptions s
WHERE s.status = 'ACTIVE'
ON CONFLICT DO NOTHING;
