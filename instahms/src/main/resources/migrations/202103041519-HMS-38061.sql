-- liquibase formatted sql
-- changeset pranavpractoinsta:adding-two-new-field-in-edit-patient-detail-screen


-- add Other Document Id and value field in patient header
INSERT INTO patient_header_preferences VALUES (
'other_identification_doc_value', 'Y', 'P', 'Other Identification Doc Value', 'b', 'Both', 50, 'Text' );
INSERT INTO patient_header_preferences VALUES (
'other_identification_doc_id', 'Y', 'P', 'Other Identification Doc ID', 'b', 'Both', 50, 'Text' );