-- liquibase formatted sql
-- changeset riyapoddar-13:add-patient-form-details-audit-log-table

CREATE TABLE patient_form_details_audit_log (
    log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    operation character varying,
    mr_no character varying(15),
    patient_id character varying(15)
);

COMMENT ON TABLE patient_form_details_audit_log IS '{ "type": "Txn", "comment": "Patient Form Details Audit Logs" }';