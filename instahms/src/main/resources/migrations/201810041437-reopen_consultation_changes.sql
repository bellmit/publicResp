-- liquibase formatted sql
-- changeset tejakilaru:reopen_and_auditlog_changes_for_consultation

ALTER TABLE doctor_consultation ADD COLUMN reopen_remarks character varying(2000);

CREATE TABLE doctor_consultation_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    consultation_id integer,
    operation character varying,
    mr_no character varying(15),
    patient_id character varying(15)
);
