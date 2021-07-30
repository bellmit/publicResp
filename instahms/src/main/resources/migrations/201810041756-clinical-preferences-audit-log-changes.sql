-- liquibase formatted sql
-- changeset tejakilaru:clinical_preferences_audit_log_changes

ALTER TABLE clinical_preferences ADD COLUMN username CHARACTER VARYING(50);

CREATE TABLE clinical_preferences_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    operation character varying
);
