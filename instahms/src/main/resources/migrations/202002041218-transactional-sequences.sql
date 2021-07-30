-- liquibase formatted sql
-- changeset adeshatole:transactional-sequences splitStatements:false

CREATE TABLE transactional_sequence (sequence_name character varying(300) primary key, value BIGINT);
COMMENT ON table transactional_sequence is '{ "type": "Master", "comment": "Table holding transactional sequences.
    The patterns in hosp_id_patterns which have is_transactional_sequence as TRUE, should have a coressponding entry in transactional_sequence table.
" }';

-- migrating bill sequences
INSERT INTO transactional_sequence (sequence_name, value) (select hip.sequence_name, nextval(hip.sequence_name) from hosp_id_patterns hip where pattern_id in (select distinct pattern_id from hosp_bill_seq_prefs));
ALTER TABLE hosp_id_patterns ADD COLUMN is_transactional_sequence boolean DEFAULT false;
UPDATE hosp_id_patterns SET is_transactional_sequence = TRUE WHERE pattern_id IN (SELECT DISTINCT pattern_id from hosp_bill_seq_prefs);


CREATE OR REPLACE FUNCTION migrate_bill_sequences()
RETURNS BOOLEAN AS $BODY$ 
DECLARE
  sequence_name varchar;
BEGIN
  FOR sequence_name IN
    SELECT hip.sequence_name FROM hosp_id_patterns hip WHERE is_transactional_sequence = TRUE
  LOOP
    EXECUTE FORMAT('ALTER SEQUENCE IF EXISTS %s RENAME TO %s_old', sequence_name,sequence_name);
  END LOOP;
  RETURN TRUE;
END;
$BODY$
LANGUAGE plpgsql;

SELECT migrate_bill_sequences();

DROP FUNCTION migrate_bill_sequences();