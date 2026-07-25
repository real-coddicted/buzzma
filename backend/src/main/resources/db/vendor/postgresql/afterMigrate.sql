DO $$
BEGIN
  IF '${appRuntimeRole}' <> '' THEN
    EXECUTE 'GRANT SELECT, INSERT, UPDATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA ${flyway:defaultSchema} TO ' || quote_ident('${appRuntimeRole}');
    EXECUTE 'GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${flyway:defaultSchema} TO ' || quote_ident('${appRuntimeRole}');
    EXECUTE 'GRANT DELETE ON REFRESH_TOKENS TO ' || quote_ident('${appRuntimeRole}');
  END IF;
END
$$;
