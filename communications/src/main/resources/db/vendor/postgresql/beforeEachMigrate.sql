DO $$
BEGIN
  IF '${flywayMigratorRole}' <> '' THEN
    EXECUTE 'SET ROLE ' || quote_ident('${flywayMigratorRole}');
  END IF;
END
$$;
