-- liquibase formatted sql
-- changeset adeshatole:fix-wrong-constraints

ALTER TABLE op_visit_type_rule_applicability DROP CONSTRAINT op_visit_type_rule_applicabil_center_id_tpa_id_dept_id_doct_key;
ALTER TABLE op_visit_type_rule_applicability ADD UNIQUE(center_id, tpa_id, dept_id, doctor_id);
ALTER TABLE op_visit_type_rule_details ADD CONSTRAINT no_overlapping_range EXCLUDE USING gist (rule_id WITH =, prev_main_visit_type WITH =, int4range(valid_from, valid_to, '[]') WITH &&);