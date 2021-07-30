-- liquibase formatted sql
-- changeset javalkarvinay:added_diag_dept_id_column

ALTER TABLE diagnostics_departments ADD COLUMN diag_dept_id serial;
SELECT comment_on_table_or_sequence_if_exists('diagnostics_departments_diag_dept_id_seq', false, 'Master','');

UPDATE code_system_categories SET entity_id='id' WHERE id=7;
UPDATE code_system_categories SET label='Diagnostics Department',table_name='diagnostics_departments',entity_name='ddept_name',entity_id='diag_dept_id',status='A' WHERE id=1;