-- liquibase formatted sql
-- changeset vakul-practo:allergy-allergen-code-master-table-creation-and-migration splitStatements:false
-- validCheckSum: ANY

-- Table for Allergy Type Masters.
CREATE SEQUENCE allergy_type_master_seq START 1;
COMMENT ON SEQUENCE allergy_type_master_seq IS '{ "type": "Master", "comment": "Holds sequence for allergy_type_master" }';

CREATE TABLE allergy_type_master (
 allergy_type_id INT DEFAULT nextval('allergy_type_master_seq'),
 allergy_type_code VARCHAR(50),
 allergy_type_name VARCHAR(2000),
 status VARCHAR(1) DEFAULT 'A',
 created_by  character varying(100) DEFAULT 'InstaAdmin',
 created_time timestamp without time zone DEFAULT now(),
 mod_user character varying(100) DEFAULT 'InstaAdmin',
 mod_time timestamp without time zone DEFAULT now() NOT NULL,
 PRIMARY KEY (allergy_type_id)
);
COMMENT ON table allergy_type_master is '{ "type": "Master", "comment": "Allergy types master" }';

-- Table for Allergen Code Masters.
CREATE SEQUENCE allergen_master_seq START 1;
COMMENT ON SEQUENCE allergen_master_seq IS '{ "type": "Master", "comment": "Holds sequence for allergen_master" }';

CREATE TABLE allergen_master (
 allergen_code_id INT DEFAULT nextval('allergen_master_seq'),
 allergy_type_id INT,
 allergen_description VARCHAR(2000),
 status VARCHAR(1) DEFAULT 'A',
 created_by  character varying(100) DEFAULT 'InstaAdmin',
 created_time timestamp without time zone DEFAULT now(),
 mod_user character varying(100) DEFAULT 'InstaAdmin',
 mod_time timestamp without time zone DEFAULT now() NOT NULL,
 PRIMARY KEY (allergen_code_id),
 CONSTRAINT fk_allergy_type FOREIGN KEY(allergy_type_id) REFERENCES allergy_type_master(allergy_type_id)
);
COMMENT ON table allergen_master is '{ "type": "Master", "comment": "Allergen master" }';

--Adding Master enteries to code_systems_categories
INSERT INTO code_system_categories (id, label, status, table_name, entity_name, entity_id)
VALUES (15 ,'Allergy Types', 'A', 'allergy_type_master', 'allergy_type_name', 'allergy_type_id'), 
(16 ,'Allergen Codes', 'A', 'allergen_master', 'allergen_description', 'allergen_code_id');

--Adding basic records to Allergy-type-master
INSERT INTO allergy_type_master (allergy_type_code, allergy_type_name)
VALUES ('M', 'Medicine Allergy'), ('O', 'Other Allergy'), ('F', 'Food Allergy');

--Migration for sanitized data into allergen_master table
----- Add allergen_code_id into generic_name
ALTER TABLE generic_name ADD COLUMN allergen_code_id INT;
ALTER TABLE generic_name ALTER COLUMN allergen_code_id SET DEFAULT nextval('allergen_master_seq');
UPDATE generic_name SET allergen_code_id=nextval('allergen_master_seq');

-- Create a temp table for allergy type and allergy from pateint_allergies table
CREATE TABLE pa_temp1 as SELECT allergy_type, allergy from patient_allergies;
COMMENT ON table pa_temp1 is '{ "type": "Txn", "comment": "Get only allergy_type and allergy data from patient_allergies" }';

-- Create table for allergy_id , allergy_type and allergy from patient_allergies table
CREATE TABLE pa_temp2 as SELECT allergy_id, allergy_type, allergy from patient_allergies;
COMMENT ON table pa_temp2 is '{ "type": "Txn", "comment": "Get allergy_id allergy_type and allergy data from patient_allergies" }';

--Sanitize trailing both spaces and dots --
-- / ? - : ; $ # % @ not done for now
UPDATE pa_temp1 SET allergy = BTRIM(BTRIM(allergy),'.');
UPDATE pa_temp2 SET allergy = BTRIM(BTRIM(allergy),'.');

-- Insert into allergen master for medicine type by removing records found in generic_name -- 
INSERT into allergen_master (allergen_description) SELECT allergy from pa_temp1 where allergy_type = 'M' EXCEPT SELECT generic_name from generic_name;
-- Update allergy_type_id in allergen_master for medicine type allergy
UPDATE allergen_master SET allergy_type_id = (Select allergy_type_id from allergy_type_master where allergy_type_code = 'M');
-- Insert into allergen_master all the rest of the records except medicine type allergy
INSERT into allergen_master (allergy_type_id, allergen_description) SELECT atm.allergy_type_id, pa.allergy from pa_temp1 pa join allergy_type_master atm on atm.allergy_type_code = pa.allergy_type where pa.allergy_type <> 'M';
-- Wherever there is a null allergen_description gets sanitized to 
UPDATE pa_temp2 set allergy = 'No Known Allergy' where allergy = '' and allergy_type <> 'N';
UPDATE allergen_master set allergen_description = 'No Known Allergy' where allergen_description = '';
-- Remove duplicate entries from allergen_master and keep distinct records -- uppercase matching
DELETE FROM allergen_master
    WHERE allergen_code_id NOT IN
    (
        SELECT MAX(allergen_code_id)
        FROM allergen_master
        GROUP BY allergy_type_id, 
                 Upper(allergen_description)
    );

-- Update generic name pattern in unique number
UPDATE unique_number set pattern= lpad('',CASE WHEN length(prefix) > 4 THEN 10-length(prefix) ELSE 6 END,'0') where type_number='GENERICNAME';

-- Update generic name column size
ALTER TABLE generic_name ALTER COLUMN generic_name TYPE varchar(2000);
    
-- Put entries into generic names
CREATE OR REPLACE FUNCTION genericNameFreeTextEntry() RETURNS VOID AS $BODY$
DECLARE
	genericFreeText RECORD;
BEGIN
 FOR genericFreeText IN SELECT allergen_code_id, allergen_description FROM allergen_master where allergy_type_id = (Select allergy_type_id from allergy_type_master where allergy_type_code = 'M') LOOP
  	INSERT into generic_name (generic_code, generic_name,allergen_code_id) 
	VALUES ((SELECT concat(prefix, trim(to_char(nextval('generic_sequence'),pattern))) FROM  unique_number WHERE type_number = 'GENERICNAME'),genericFreeText.allergen_description,genericFreeText.allergen_code_id);
 END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';

select genericNameFreeTextEntry();
DROP function genericNameFreeTextEntry();

--remove records from allergen master
DELETE FROM allergen_master where allergy_type_id = (Select allergy_type_id from allergy_type_master where allergy_type_code = 'M');

--Add Columns to pa_temp2
ALTER TABLE pa_temp2 ADD allergy_type_id INT;
ALTER TABLE pa_temp2 ADD allergen_code_id INT;

--Update allergy type id from its master
UPDATE pa_temp2
	set allergy_type_id = atm.allergy_type_id
	from allergy_type_master atm
	where atm.allergy_type_code = pa_temp2.allergy_type;

--Update Allergen_code_id for allergy type 'M' from generic name
UPDATE pa_temp2
	set allergen_code_id = gn.allergen_code_id
	from generic_name gn
	where upper(gn.generic_name) = upper(pa_temp2.allergy)
	and pa_temp2.allergy_type = 'M';

--Update Allergen_code_id for allergy type other than 'M' from allergen master
UPDATE pa_temp2
	set allergen_code_id = am.allergen_code_id
	from allergen_master am
	where upper(am.allergen_description) = upper(pa_temp2.allergy) and pa_temp2.allergy_type_id = am.allergy_type_id
	and pa_temp2.allergy_type <> 'M' and pa_temp2.allergy_type_id is not NULL;

-- Create Index on temp table to join faster and under finite time limit
CREATE INDEX idx_temp_allergy_id ON pa_temp2(allergy_id);
-- WIRING DATA TO TRANSACTIONAL TABLE---
ALTER TABLE patient_allergies ADD allergy_type_id INT;
ALTER TABLE patient_allergies ADD allergen_code_id INT;

UPDATE patient_allergies
	set allergy_type_id = pa2.allergy_type_id,
		allergen_code_id = pa2.allergen_code_id 
	from pa_temp2 pa2
	where pa2.allergy_id = patient_allergies.allergy_id;

ALTER TABLE clinical_preferences ADD COLUMN allow_free_text_in_allergies character(1) default 'Y';

----- IN CASE A RESTORE IS REQUIRED ---
--Alter table patient_allergies drop column allergy_type_id;
--Alter table patient_allergies drop column allergen_code_id;
--ALTER TABLE generic_name DROP COLUMN allergen_code_id;
--DELETE FROM generic_name WHERE generic_code like '%GN0000%';
--ALTER TABLE clinical_preferences DROP COLUMN allow_free_text_in_allergies;
--DELETE FROM code_system_categories WHERE id in (13,14);
--DROP TABLE allergen_master;
--DROP SEQUENCE allergen_master_seq;
--DROP TABLE allergy_type_master;
--DROP SEQUENCE allergy_type_master_seq;


