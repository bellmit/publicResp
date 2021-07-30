-- liquibase formatted sql
-- changeset pranays:added-fields-shifted-to-clinical-prefs-from-generic-prefs-and-added-vitals-template-preferences

ALTER TABLE clinical_preferences RENAME COLUMN allow_prescription_format_override TO allow_op_prescription_format_override;

ALTER TABLE clinical_preferences ADD COLUMN historic_vitals_period INTEGER NOT NULL DEFAULT 1,
 ADD COLUMN historic_vitals_period_unit CHARACTER(1) NOT NULL DEFAULT 'D',
 ADD COLUMN allow_ip_prescription_format_override CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN op_allow_template CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN op_allow_template_save_with_data CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN triage_allow_template CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN triage_allow_template_save_with_data CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN ip_allow_template_save_with_data CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN ip_allow_template CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN op_consultation_edit_across_doctors CHARACTER(1) NOT NULL DEFAULT 'Y',
 ADD COLUMN ip_cases_across_doctors CHARACTER(1) NOT NULL DEFAULT 'Y',
 ADD COLUMN nurse_staff_ward_assignments_applicable CHARACTER(1) NOT NULL DEFAULT 'N',
 ADD COLUMN consultation_reopen_time_limit INTEGER NOT NULL DEFAULT 48,
 ADD COLUMN consultation_validity_units CHARACTER(1) NOT NULL DEFAULT 'T';