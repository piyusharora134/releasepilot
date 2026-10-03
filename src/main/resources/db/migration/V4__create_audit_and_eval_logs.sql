CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    organization_id UUID,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    details_json TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE evaluation_logs (
    id UUID PRIMARY KEY,
    environment_id UUID NOT NULL,
    flag_key VARCHAR(100) NOT NULL,
    user_context_id VARCHAR(255),
    user_context_json TEXT,
    result_value TEXT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL
);
