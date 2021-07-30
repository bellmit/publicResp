-- liquibase formatted sql
-- changeset junaidahmed:capture-eligibility-reference

ALTER TABLE ha_tpa_code ADD COLUMN enable_eligibility_authorization BOOLEAN DEFAULT FALSE;
ALTER TABLE ha_tpa_code ADD COLUMN enable_eligibility_auth_in_xml VARCHAR(1) NOT NULL DEFAULT 'N';
COMMENT ON COLUMN ha_tpa_code.enable_eligibility_auth_in_xml IS 'T:Brings as tag in claim xml, N:Doesnt bring in claim xml, O:Brings as observation in claim xml';


CREATE TABLE eligibility_authorization_status (id SERIAL PRIMARY KEY, status_label varchar UNIQUE NOT NULL);
INSERT INTO eligibility_authorization_status VALUES(nextval('eligibility_authorization_status_id_seq'),'Taken');
INSERT INTO eligibility_authorization_status VALUES(nextval('eligibility_authorization_status_id_seq'),'Not Taken');
INSERT INTO eligibility_authorization_status VALUES(nextval('eligibility_authorization_status_id_seq'),'Not Applicable');
COMMENT ON TABLE eligibility_authorization_status IS '{ "type": "Master", "comment": "Stores the statuses of eligibility authorization"}';
COMMENT ON SEQUENCE eligibility_authorization_status_id_seq IS '{ "type": "Master", "comment": "Stores the statuses of eligibility authorization"}';


ALTER TABLE patient_policy_details ADD COLUMN eligibility_reference_number VARCHAR(50);
ALTER TABLE patient_policy_details ADD COLUMN eligibility_authorization_status INTEGER;
ALTER TABLE patient_policy_details ADD COLUMN eligibility_authorization_remarks VARCHAR(300);