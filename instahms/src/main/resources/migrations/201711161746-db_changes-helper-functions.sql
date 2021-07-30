-- liquibase formatted sql
-- changeset raj-nt:db_changes-helper-functions splitStatements:false runAlways:true
-- validCheckSum: ANY

--
-- Useful functions that are often used in db_changes. Needs to be here,
-- Since we may run this on a fresh schema where vft.sql has not been run.
--
CREATE OR REPLACE FUNCTION remove_dups_on (tbl TEXT, on_column TEXT) RETURNS VOID AS $$
BEGIN
 EXECUTE 'CREATE TEMP TABLE tmp_for_dups AS SELECT DISTINCT ON('
	|| on_column || ') * FROM ' || quote_ident(tbl);
 EXECUTE 'DELETE FROM ' || quote_ident(tbl);
 EXECUTE 'INSERT INTO ' || quote_ident(tbl) || ' (SELECT * FROM tmp_for_dups)';
 DROP TABLE tmp_for_dups;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION comment_on_table_or_sequence_if_exists(relationName TEXT, isTable BOOLEAN, type TEXT, comment TEXT) RETURNS VOID AS $$
BEGIN
IF EXISTS (SELECT relname from pg_catalog.pg_class c
 JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace where relname = relationName
 AND n.nspname = current_schema) THEN
 IF isTABLE THEN
  EXECUTE 'COMMENT ON table ' || relationName || ' is ''{ "type": "' || type || '", "comment": "' || comment || '" }''';
 ELSE
  EXECUTE 'COMMENT ON SEQUENCE ' || relationName || ' is ''{ "type": "' || type || '", "comment": "' || comment || '" }''';
 END IF;
END IF;
END;
$$ LANGUAGE plpgsql;