CREATE TABLE ai_configs (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    description VARCHAR(255),
    is_encrypted BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE ai_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50),
    message TEXT,
    severity VARCHAR(20),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE,
    metadata TEXT
);

CREATE INDEX idx_ai_alerts_read ON ai_alerts(is_read);
CREATE INDEX idx_ai_alerts_created_at ON ai_alerts(created_at);
