-- liquibase formatted sql
-- changeset chandrapalsingh:salucro-location-role-audit-log-tables
CREATE TABLE  salucro_location_mapping_audit_log (
    log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    counter_id character varying(20),
  	user_name character varying,
    operation character varying,
	salucro_location_mapping_id integer
);


CREATE TABLE  salucro_role_mapping_audit_log (
    log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    role character varying(100),
	user_name character varying,
	operation character varying,
	salucro_role_mapping_id integer
);

COMMENT ON TABLE salucro_location_mapping_audit_log IS '{ "type": "Txn", "comment": "Salucro Location Mapping Audit Logs" }';

COMMENT ON TABLE salucro_role_mapping_audit_log IS '{ "type": "Txn", "comment": "Salucro Role Mapping Audit Logs" }';
