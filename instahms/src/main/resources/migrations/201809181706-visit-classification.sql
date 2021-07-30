-- liquibase formatted sql
-- changeset junaidahmed:adds-new-col-health-auth-pref

ALTER TABLE patient_registration ADD COLUMN classification varchar;
ALTER TABLE health_authority_preferences ADD COLUMN is_visit_classification_mandatory BOOLEAN DEFAULT 'N';
COMMENT ON COLUMN patient_registration.classification IS 'Visits may be classified as Medical Tourist(M), Selfpay Patients(S) or Other(O). Refer visitClassificationType.java'
;