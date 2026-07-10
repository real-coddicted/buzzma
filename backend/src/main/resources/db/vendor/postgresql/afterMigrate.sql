GRANT SELECT, INSERT, UPDATE, REFERENCES, TRIGGER
    ON ALL TABLES IN SCHEMA ${flyway:defaultSchema}
    TO ${appRuntimeRole};

GRANT USAGE, SELECT
    ON ALL SEQUENCES IN SCHEMA ${flyway:defaultSchema}
    TO ${appRuntimeRole};

-- Specific grants for REFRESH_TOKENS table for delete privilege needed for token revocation/ rotation
GRANT DELETE ON REFRESH_TOKENS  TO ${appRuntimeRole};
