-- liquibase formatted sql
-- changeset janakivg:enable-immunization-op-forms
update system_generated_sections set op='Y' where section_id =-17;
update system_generated_sections set op_follow_up_consult_form='Y' where section_id =-17;
