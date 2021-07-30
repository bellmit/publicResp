-- liquibase formatted sql
-- changeset javalkarvinay:HMS-35038_consumption_uom_master_table_1 splitStatements:false

CREATE OR REPLACE FUNCTION consumption_uom_function() 
RETURNS VOID AS $BODY$
BEGIN
    IF EXISTS (SELECT * FROM pg_tables where tablename='consumption_uom_master' and schemaname =current_schema()) THEN
    	ALTER TABLE consumption_uom_master ALTER COLUMN consumption_uom TYPE VARCHAR(50);
    ELSE
        CREATE SEQUENCE consumption_uom_master_seq START 1;
        COMMENT ON SEQUENCE consumption_uom_master_seq IS '{ "type": "Master", "comment": "Holds sequence for consumption uom id" }';

        CREATE TABLE consumption_uom_master (
            cons_uom_id INTEGER DEFAULT nextval('consumption_uom_master_seq'),
            consumption_uom VARCHAR(50),
            status CHARACTER(1),
            PRIMARY KEY (cons_uom_id)
        );
        COMMENT ON TABLE consumption_uom_master IS '{ "type": "Master", "comment": "Holds sequence for consumption uom" }';

        ALTER TABLE store_item_details ADD COLUMN cons_uom_id INTEGER;
        ALTER TABLE patient_medicine_prescriptions ADD COLUMN cons_uom_id INTEGER;
        ALTER TABLE doctor_medicine_favourites ADD COLUMN cons_uom_id INTEGER;
        ALTER TABLE discharge_medication_details ADD COLUMN cons_uom_id INTEGER;
        ALTER TABLE patient_other_medicine_prescriptions ADD COLUMN cons_uom_id INTEGER;
        ALTER TABLE doctor_other_medicine_favourites ADD COLUMN cons_uom_id INTEGER;

        INSERT INTO consumption_uom_master (consumption_uom,status) 
            SELECT DISTINCT consumption_uom,status 
                FROM (SELECT DISTINCT consumption_uom,'A' AS status FROM patient_medicine_prescriptions WHERE consumption_uom is not null AND consumption_uom != ''
                      UNION
                      SELECT DISTINCT consumption_uom, 'A' AS status FROM store_item_details WHERE (consumption_uom is not null AND consumption_uom != '')
                      UNION
                      SELECT DISTINCT consumption_uom, 'A' AS status FROM doctor_medicine_favourites WHERE (consumption_uom is not null AND consumption_uom != '')
                      UNION
                      SELECT DISTINCT consumption_uom, 'A' AS status FROM discharge_medication_details WHERE (consumption_uom is not null AND consumption_uom != '')
                      UNION
                      SELECT DISTINCT consumption_uom, 'A' AS status FROM patient_other_medicine_prescriptions WHERE (consumption_uom is not null AND consumption_uom != '')
                      UNION
                      SELECT DISTINCT consumption_uom, 'A' AS status FROM doctor_other_medicine_favourites WHERE (consumption_uom is not null AND consumption_uom != ''))
                as foo;

        UPDATE store_item_details s SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE s.consumption_uom = c.consumption_uom;
        UPDATE patient_medicine_prescriptions pmp SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE pmp.consumption_uom = c.consumption_uom;
        UPDATE doctor_medicine_favourites dmf SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE dmf.consumption_uom = c.consumption_uom;
        UPDATE discharge_medication_details dmd SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE dmd.consumption_uom = c.consumption_uom;
        UPDATE patient_other_medicine_prescriptions pomp SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE pomp.consumption_uom = c.consumption_uom;
        UPDATE doctor_other_medicine_favourites domf SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE domf.consumption_uom = c.consumption_uom;

        ALTER TABLE store_item_details RENAME COLUMN consumption_uom TO obsolete_consumption_uom;
        ALTER TABLE patient_medicine_prescriptions RENAME COLUMN consumption_uom TO obsolete_consumption_uom;
        ALTER TABLE doctor_medicine_favourites RENAME COLUMN consumption_uom TO obsolete_consumption_uom;
        ALTER TABLE discharge_medication_details RENAME COLUMN consumption_uom TO obsolete_consumption_uom;
        ALTER TABLE patient_other_medicine_prescriptions RENAME COLUMN consumption_uom TO obsolete_consumption_uom;
        ALTER TABLE doctor_other_medicine_favourites RENAME COLUMN consumption_uom TO obsolete_consumption_uom;

        INSERT INTO screen_rights select distinct role_id,'mas_consumption_uom','A' from screen_rights where rights='A' AND screen_id='mas_medicines';
    END IF;
END;
$BODY$
LANGUAGE 'plpgsql';

SELECT consumption_uom_function();
DROP FUNCTION consumption_uom_function();