-- This file only for excute Fresh schemas ,when we will build,before DB Changes file excute.

-- Function to reset sequences where sequences were not being used earlier
 
DROP FUNCTION IF EXISTS reset_sequence_to_max_value(text,text);

CREATE or REPLACE FUNCTION reset_sequence_to_max_value(tableName text, idColumn text) 
	RETURNS void as $$
	DECLARE 
	prefixLength integer;
	prefixValue text;
	val integer;
	BEGIN
	
	-- update unique_number set type_number = tableName where type_number = typeNumber;
	
	select prefix from unique_number where type_number = tableName into prefixValue;
	
	IF (prefixValue IS NULL) THEN 
		prefixValue := '';
	END IF;
	
	select length(prefixValue) into prefixLength;
	
	execute 'select TO_NUMBER(SUBSTRING(' || quote_ident(idColumn) || ' FROM ' || prefixLength ||  '+ 1 FOR 10),' || E' \'999999\'' || ') from ' ||   quote_ident(tableName) ||  ' where ' || quote_ident(idColumn) || ' SIMILAR TO ' || E' \'' || prefixValue || E'[0-9]%\'' || ' ORDER BY TO_NUMBER(SUBSTRING(' || quote_ident(idColumn) || ' FROM ' || prefixLength || '+ 1 FOR 10),' || E' \'999999\'' || ') DESC LIMIT 1;' into val;
	
	IF val IS NULL THEN
		val := 1;
	ELSE 
		val := val + 1;
	END IF;
	
	execute 'alter sequence ' || quote_ident(tableName) || '_seq' || ' restart ' || val;
	END
$$
LANGUAGE plpgsql ;


DROP FUNCTION IF EXISTS remove_dups_on(text,text);

CREATE OR REPLACE FUNCTION remove_dups_on (tbl TEXT, on_column TEXT) RETURNS VOID AS $$
BEGIN
 EXECUTE 'CREATE TEMP TABLE tmp_for_dups AS SELECT DISTINCT ON('
	|| on_column || ') * FROM ' || quote_ident(tbl);
 EXECUTE 'DELETE FROM ' || quote_ident(tbl);
 EXECUTE 'INSERT INTO ' || quote_ident(tbl) || ' (SELECT * FROM tmp_for_dups)';
 DROP TABLE tmp_for_dups;
END;
$$ LANGUAGE plpgsql;