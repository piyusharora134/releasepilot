CREATE TABLE feature_flags (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    key VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    flag_type VARCHAR(50) NOT NULL,
    default_serve_value TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_feature_flags_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_feature_flags_project_key
        UNIQUE (project_id, key)
);

CREATE TABLE flag_environments (
    id UUID PRIMARY KEY,
    flag_id UUID NOT NULL,
    environment_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    serve_value TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_flag_environments_flag
        FOREIGN KEY (flag_id)
        REFERENCES feature_flags(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_flag_environments_environment
        FOREIGN KEY (environment_id)
        REFERENCES environments(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_flag_environments_flag_env
        UNIQUE (flag_id, environment_id)
);

CREATE TABLE targeting_rules (
    id UUID PRIMARY KEY,
    flag_environment_id UUID NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    attribute VARCHAR(100) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    values_json TEXT NOT NULL,
    serve_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_targeting_rules_flag_env
        FOREIGN KEY (flag_environment_id)
        REFERENCES flag_environments(id)
        ON DELETE CASCADE
);

CREATE TABLE rollout_rules (
    id UUID PRIMARY KEY,
    flag_environment_id UUID NOT NULL,
    attribute VARCHAR(100) NOT NULL DEFAULT 'userId',
    percentage INT NOT NULL,
    serve_value_a TEXT NOT NULL,
    serve_value_b TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_rollout_rules_flag_env
        FOREIGN KEY (flag_environment_id)
        REFERENCES flag_environments(id)
        ON DELETE CASCADE
);
