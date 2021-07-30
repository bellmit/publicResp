-- liquibase formatted sql
-- changeset harishm18:adding-missing-columns-patient-package-contents

ALTER TABLE patient_package_contents ADD COLUMN content_id_ref INTEGER;
ALTER TABLE patient_package_contents ADD COLUMN operation_id CHARACTER VARYING(10);
ALTER TABLE patient_package_contents ADD COLUMN activity_units CHARACTER VARYING;
ALTER TABLE patient_package_contents ADD COLUMN activity_remarks CHARACTER VARYING;
ALTER TABLE patient_package_contents ADD COLUMN display_order INTEGER;
ALTER TABLE patient_package_contents ADD COLUMN bed_id INTEGER;
ALTER TABLE patient_package_contents ADD COLUMN panel_id INTEGER;
ALTER TABLE patient_package_contents ADD COLUMN visit_qty_limit INTEGER;
