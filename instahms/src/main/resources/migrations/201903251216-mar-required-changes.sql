-- liquibase formatted sql
-- changeset tejakilaru:mar-required-changes

ALTER TABLE clinical_preferences ADD COLUMN late_serving_period INTEGER DEFAULT 0;
COMMENT ON COLUMN clinical_preferences.late_serving_period IS 'value in hours';
ALTER TABLE clinical_preferences ADD COLUMN advance_setup_period INTEGER DEFAULT 48;
COMMENT ON COLUMN clinical_preferences.advance_setup_period IS 'value in hours';
ALTER TABLE clinical_preferences ADD COLUMN serving_window INTEGER DEFAULT 1;
COMMENT ON COLUMN clinical_preferences.serving_window IS 'value in hours';

CREATE SEQUENCE serving_frequency_master_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
COMMENT ON SEQUENCE serving_frequency_master_seq is '{ "type": "Master", "comment": "Sequence for serving_frequency_master" }';

CREATE TABLE serving_frequency_master(
	serving_frequency_id INTEGER DEFAULT nextval('serving_frequency_master_seq'::regclass) NOT NULL PRIMARY KEY,
	name character varying(50),
	occurrences INTEGER,
	timings character varying(100),
	recurrence_daily_id INTEGER,
	mod_time timestamp without time zone DEFAULT ('now'::text)::timestamp,
	username character varying(30),
	status character varying(1)
);
COMMENT ON table serving_frequency_master is '{ "type": "Master", "comment": "Master for MAR serving frequency" }';

INSERT INTO serving_frequency_master(name, occurrences, timings, recurrence_daily_id, mod_time, username, status)
select display_name, num_activities, timings, recurrence_daily_id, mod_time, username, status from recurrence_daily_master;

CREATE SEQUENCE patient_mar_setup_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
COMMENT ON SEQUENCE patient_mar_setup_seq is '{ "type": "Txn", "comment": "Sequence for patient_mar_setup" }';

CREATE TABLE patient_mar_setup(
	setup_id INTEGER DEFAULT nextval('patient_mar_setup_seq'::regclass) NOT NULL PRIMARY KEY,
	serving_frequency_id INTEGER,
	prescription_id INTEGER,
	remarks character varying,
	mod_time timestamp without time zone DEFAULT ('now'::text)::timestamp,
	username character varying(30)
);
COMMENT ON table patient_mar_setup is '{ "type": "Txn", "comment": "Patient MAR Setup data" }';
CREATE INDEX prescription_id_idx ON patient_mar_setup USING btree (prescription_id);

ALTER TABLE patient_prescription ADD COLUMN max_doses INTEGER;
COMMENT ON COLUMN patient_prescription.max_doses IS 'maximum number of times to be given in case of PRN Priority ';

ALTER TABLE patient_activities ADD COLUMN setup_id INTEGER;
ALTER TABLE patient_activities ADD COLUMN stock character;
ALTER TABLE patient_activities ADD COLUMN infusion_site INTEGER;
ALTER TABLE patient_activities ADD COLUMN iv_status character;
ALTER TABLE patient_activities ADD COLUMN serving_remarks_id INTEGER;

CREATE TABLE patient_iv_administered_details(
	activity_id INTEGER,
	state character,
	username character varying(30),
	mod_time timestamp without time zone
);
COMMENT ON table patient_iv_administered_details is '{ "type": "Txn", "comment": "to record the iv line actions" }';

ALTER TABLE patient_medicine_prescriptions ADD COLUMN flow_rate NUMERIC(10,2);
ALTER TABLE patient_medicine_prescriptions ADD COLUMN flow_rate_units character varying;
ALTER TABLE patient_medicine_prescriptions ADD COLUMN infusion_period INTEGER;
ALTER TABLE patient_medicine_prescriptions ADD COLUMN infusion_period_units character varying;
ALTER TABLE patient_medicine_prescriptions ADD COLUMN iv_administer_instructions character varying;
ALTER TABLE patient_medicine_prescriptions ADD COLUMN medication_type character varying(2) DEFAULT 'M'; 

CREATE SEQUENCE medication_serving_remarks_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
COMMENT ON SEQUENCE medication_serving_remarks_seq is '{ "type": "Master", "comment": "Sequence for medication_serving_remarks" }';

CREATE TABLE medication_serving_remarks(
	serving_remarks_id INTEGER DEFAULT nextval('medication_serving_remarks_seq'::regclass) NOT NULL PRIMARY KEY,
	remark_name character varying NOT NULL,
	status character DEFAULT 'A',
	mod_time timestamp without time zone DEFAULT ('now'::text)::timestamp,
	username character varying(30)
);
COMMENT ON table medication_serving_remarks is '{ "type": "Master", "comment": "list for medication serving remarks" }';

CREATE SEQUENCE iv_infusionsites_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
COMMENT ON SEQUENCE iv_infusionsites_seq is '{ "type": "Master", "comment": "Sequence for iv_infusionsites" }';

CREATE TABLE iv_infusionsites(
	id INTEGER DEFAULT nextval('iv_infusionsites_seq'::regclass) NOT NULL PRIMARY KEY,
	site_name character varying NOT NULL,
	status character DEFAULT 'A',
	mod_time timestamp without time zone DEFAULT ('now'::text)::timestamp,
	username character varying(30)
);
COMMENT ON table iv_infusionsites is '{ "type": "Master", "comment": "list for iv infusion sites" }';

