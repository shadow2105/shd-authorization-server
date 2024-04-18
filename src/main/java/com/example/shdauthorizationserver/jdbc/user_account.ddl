USE shd_dev;
-- USE shd_prod;

CREATE TABLE user_accounts (
id VARCHAR(36),
created_by VARCHAR(50) NOT NULL,
updated_by VARCHAR(50),
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP,
username VARCHAR(50) NOT NULL,
password VARCHAR(500) NOT NULL,
enabled BOOLEAN NOT NULL,
account_expired BOOLEAN NOT NULL,
account_locked BOOLEAN NOT NULL,
credentials_expired BOOLEAN NOT NULL
);

ALTER TABLE user_accounts
    ADD CONSTRAINT user_accounts_id_pk
        PRIMARY KEY( id );

ALTER TABLE user_accounts
    ADD CONSTRAINT user_accounts_username_uk
        UNIQUE KEY( username );

-- --------------------------------------------------------------------------------------

CREATE TABLE authorities (
username VARCHAR(50) NOT NULL,
authority VARCHAR(50) NOT NULL
);

ALTER TABLE authorities
    ADD CONSTRAINT authorities_username_fk
        FOREIGN KEY( username ) REFERENCES user_accounts ( username );

CREATE UNIQUE INDEX ix_auth_username ON authorities ( username, authority );