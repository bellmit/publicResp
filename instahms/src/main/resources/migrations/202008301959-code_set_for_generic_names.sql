-- liquibase formatted sql
-- changeset javalkarvinay:code_set_for_generic_names

ALTER TABLE code_systems ADD COLUMN status VARCHAR(1) DEFAULT 'A';
ALTER TABLE code_system_categories ADD COLUMN status VARCHAR(1) DEFAULT 'A';
ALTER TABLE code_system_categories ADD COLUMN table_name VARCHAR(50);
ALTER TABLE code_system_categories ADD COLUMN entity_name VARCHAR(50);
ALTER TABLE code_system_categories ADD COLUMN entity_id VARCHAR(25);
ALTER TABLE generic_name ADD COLUMN generic_name_id serial;
SELECT comment_on_table_or_sequence_if_exists('generic_name_generic_name_id_seq', false, 'Master','');

UPDATE code_system_categories SET label='Gender',table_name='gender_master',status='I' WHERE id=1;
UPDATE code_system_categories SET label='Religion',table_name='religion_master',entity_name='religion_name',entity_id='religion_id' WHERE id=2;
UPDATE code_system_categories SET label='Marital Status',table_name='marital_status_master',entity_name='marital_status_name',entity_id='marital_status_id' WHERE id=3;
UPDATE code_system_categories SET label='Department',table_name='department',entity_name='dept_name',entity_id='id' WHERE id=4;
UPDATE code_system_categories SET label='Discharge Type',table_name='discharge_type_master',entity_name='discharge_type',entity_id='discharge_type_id' WHERE id=5;
UPDATE code_system_categories SET label='Nationality',table_name='country_master',entity_name='country_name',entity_id='id' WHERE id=6;
UPDATE code_system_categories SET label='State',table_name='state_master',entity_name='state_name',entity_id='state_id' WHERE id=7;
UPDATE code_system_categories SET label='Country',table_name='country_master',entity_name='country_name',entity_id='id' WHERE id=8;
UPDATE code_system_categories SET label='Diagnosis Statuses',table_name='diagnosis_statuses',entity_name='diagnosis_status_name',entity_id='diagnosis_status_id' WHERE id=9;
UPDATE code_system_categories SET label='Medicine Route',table_name='medicine_route',entity_name='route_name',entity_id='route_id' WHERE id=10;
INSERT INTO code_system_categories (id,label,status,table_name,entity_name,entity_id) VALUES (11,'Generic Medicines','A','generic_name','generic_name','generic_name_id');
INSERT INTO code_systems (label) SELECT health_authority FROM health_authority_master;