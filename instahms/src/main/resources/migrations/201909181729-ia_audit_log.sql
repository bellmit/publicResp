-- liquibase formatted sql
-- changeset sonam009:initial-assesssment-form-audit-log

ALTER TABLE doctor_consultation ADD COLUMN reopen_remarks_ia VARCHAR(2000);
ALTER TABLE doctor_consultation ADD COLUMN ia_complete_time timestamp with time zone;

COMMENT ON COLUMN doctor_consultation.reopen_remarks_ia IS 'Reopen remarks column for Initial Assessment Form';

CREATE TABLE doctor_consultation_ia_audit_log (
    log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
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

COMMENT ON TABLE doctor_consultation_ia_audit_log IS '{ "type": "Txn", "comment": "Initial Assessment Details Audit Logs" }';

INSERT INTO screen_rights (SELECT distinct role_id, 'ia_summary_audit_log' as screen_id, 'A' as rights FROM screen_rights where screen_id = 'ia_audit_log' and rights='A');
INSERT INTO url_action_rights (SELECT distinct role_id, 'ia_summary_audit_log' as action_id, 'A' as rights FROM url_action_rights where action_id = 'ia_audit_log' and rights='A');