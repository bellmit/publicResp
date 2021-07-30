-- liquibase formatted sql
-- changeset mohamedanees:create-index-package-charges splitStatements:false

CREATE OR REPLACE FUNCTION create_index_if_not_exists(createIndexName TEXT, constraints TEXT) RETURNS boolean AS $BODY$
	BEGIN	
    IF NOT EXISTS (
    SELECT *
        FROM pg_indexes WHERE indexname=quote_ident(createIndexName)
    )
    THEN
		EXECUTE FORMAT('CREATE INDEX %I ON ',createIndexName) || constraints;
    END IF;
	RETURN true;
	END; 
$BODY$ 
LANGUAGE plpgsql;

SELECT create_index_if_not_exists
    ('package_charges_org_id_idx','package_charges(org_id)');
SELECT create_index_if_not_exists
    ('dyna_package_category_limits_org_id_idx','dyna_package_category_limits(org_id)');

DROP FUNCTION create_index_if_not_exists(TEXT,TEXT);
