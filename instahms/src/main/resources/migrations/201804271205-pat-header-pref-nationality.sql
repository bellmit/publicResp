-- liquibase formatted sql
-- changeset satishl2772:adding-nationality-in-patient-header-preferences

INSERT INTO patient_header_preferences (SELECT 'nationality_name','Y','P',
CASE WHEN (nationality IS NULL OR nationality = '') THEN 'Nationality' ELSE nationality END,
'b', 'Both'
FROM registration_preferences);
