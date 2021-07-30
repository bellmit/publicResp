-- liquibase formatted sql
-- changeset janakivg:patient_notes_audit_log

CREATE TABLE patient_notes_audit_log (
    log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    note_id integer,
    patient_id  character varying,
    note_type_id integer, 
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    operation character varying
);

CREATE INDEX patient_notes_audit_log_patient_id_index ON patient_notes_audit_log USING btree (patient_id);
COMMENT ON sequence note_type_master_seq is '{ "type": "Master", "comment": "note type master sequence" }';
COMMENT ON table note_type_master is '{ "type": "Master", "comment": "Note types with template content defining" }';
COMMENT ON sequence note_type_template_master_seq is '{ "type": "Master", "comment": "note type template master sequence" }';
COMMENT ON table note_type_template_master is '{ "type": "Master", "comment": "Holds note template content" }';
COMMENT ON sequence patient_notes_seq is '{ "type": "Txn", "comment": "patient notes sequence" }';
COMMENT ON table patient_notes is '{ "type": "Txn", "comment": "Holds visit wise patient notes information" }';
COMMENT ON table patient_notes_audit_log is '{ "type": "Txn", "comment": "Patient notes audit log" }';

INSERT INTO patient_notes_audit_log(log_id,note_id,patient_id,note_type_id,user_name,mod_time,field_name,old_value,new_value,operation)
SELECT nextval('audit_logid_sequence'),pn.note_id,da.patient_id,pn.note_type_id,da.user_name,da.mod_time,
da.field_name,da.old_value,da.new_value,da.operation 
FROM patient_notes pn JOIN doctor_notes_audit_log da ON (da.patient_id = pn.patient_id AND pn.old_note_num=da.note_num);


INSERT INTO patient_notes_audit_log(log_id,note_id,patient_id,note_type_id,user_name,mod_time,field_name,old_value,new_value,operation)
SELECT nextval('audit_logid_sequence'),pn.note_id,nn.patient_id,pn.note_type_id,nn.user_name,nn.mod_time,
nn.field_name,nn.old_value,nn.new_value,nn.operation
FROM patient_notes pn JOIN nurse_notes_audit_log nn ON (nn.patient_id = pn.patient_id AND pn.old_note_num=nn.note_num);

ALTER TABLE patient_notes DROP COLUMN old_note_num;

