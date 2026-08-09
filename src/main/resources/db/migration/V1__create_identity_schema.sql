CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       github_id VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(320) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       avatar_url TEXT,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE organizations (
                               id UUID PRIMARY KEY,
                               name VARCHAR(255) NOT NULL,
                               slug VARCHAR(100) NOT NULL UNIQUE,
                               created_at TIMESTAMPTZ NOT NULL,
                               updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE organization_members (
                                      id UUID PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      organization_id UUID NOT NULL,
                                      role VARCHAR(50) NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL,

                                      CONSTRAINT fk_organization_member_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_organization_member_organization
                                          FOREIGN KEY (organization_id)
                                              REFERENCES organizations(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uq_organization_member_user_organization
                                          UNIQUE (user_id, organization_id)
);