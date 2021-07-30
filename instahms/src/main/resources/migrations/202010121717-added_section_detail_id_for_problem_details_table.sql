-- liquibase formatted sql
-- changeset javalkarvinay:added_section_detail_id_for_problem_details_table splitStatements:false
-- validCheckSum: ANY

ALTER TABLE patient_problem_list_details ADD COLUMN section_detail_id INTEGER;
UPDATE patient_problem_list_details ppld SET section_detail_id = pcd.section_detail_id FROM patient_section_details pcd WHERE pcd.section_status='A' AND pcd.section_id = -21 AND pcd.patient_id = ppld.visit_id;

ALTER TABLE patient_problem_list ADD COLUMN created_in_sec_detail_id INTEGER;
CREATE OR REPLACE FUNCTION update_created_section_id_for_problems() 
RETURNS VOID AS $BODY$
DECLARE
  problemRecord RECORD;
  problemDetailRecord RECORD;
BEGIN
  FOR problemRecord IN SELECT ppl_id FROM patient_problem_list LOOP
    FOR problemDetailRecord IN SELECT section_detail_id FROM patient_problem_list_details WHERE ppl_id = problemRecord.ppl_id ORDER BY ppld_id ASC LIMIT 1 LOOP
      UPDATE patient_problem_list SET created_in_sec_detail_id = problemDetailRecord.section_detail_id WHERE ppl_id = problemRecord.ppl_id;
    END LOOP;
  END LOOP;
END;

$BODY$
LANGUAGE 'plpgsql';

select update_created_section_id_for_problems();
DROP function update_created_section_id_for_problems();