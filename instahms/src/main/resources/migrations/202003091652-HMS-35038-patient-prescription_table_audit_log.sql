-- liquibase formatted sql
-- changeset javalkarvinay:HMS-35038-patient-prescription_table_audit_audit

CREATE SEQUENCE patient_other_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_other_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient other prescription log revision id" }';

CREATE TABLE patient_other_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_other_prescriptions_audit_seq'),
    item_name VARCHAR(100),
    item_remarks VARCHAR(2000), 
    mod_time TIMESTAMP,
    activity_due_date TIMESTAMP,
    frequency VARCHAR(150),
    medicine_quantity INTEGER,
    strength VARCHAR(100),
    item_form_id INTEGER,
    item_strength VARCHAR(50),
    non_hosp_medicine BOOLEAN,
    duration INTEGER,
    duration_units CHARACTER(1),
    item_strength_units INTEGER,
    consumption_uom VARCHAR(50),
    prescription_id INTEGER,
    admin_strength VARCHAR(100),
    username VARCHAR(30),
    refills VARCHAR(100),
    time_of_intake CHARACTER(1),
    priority VARCHAR(1),
    PRIMARY KEY(revision_id,prescription_id)
);
COMMENT ON TABLE patient_other_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient other prescription log" }';

CREATE SEQUENCE patient_operation_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_operation_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient operation prescription log revision id" }';

CREATE TABLE patient_operation_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_operation_prescriptions_audit_seq'),
    operation_id VARCHAR(20),
    mod_time TIMESTAMP,
    remarks VARCHAR(2000),
    preauth_required CHARACTER(1),
    prescription_id INTEGER,
    admin_strength VARCHAR(100),
    username VARCHAR(30),
    PRIMARY KEY(revision_id,prescription_id)
);
COMMENT ON TABLE patient_operation_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient operation prescription log" }';

CREATE SEQUENCE patient_other_medicine_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_other_medicine_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient other_medicine prescription log revision id" }';

CREATE TABLE patient_other_medicine_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_other_medicine_prescriptions_audit_seq'),
    medicine_name VARCHAR(100),
    frequency VARCHAR(150),
    medicine_quantity INTEGER,
    medicine_remarks VARCHAR(2000),
    mod_time TIMESTAMP,
    activity_due_date TIMESTAMP,
    route_of_admin INTEGER,
    strength VARCHAR(100),
    item_form_id INTEGER,
    item_strength VARCHAR(50),
    visit_id VARCHAR(15),
    duration INTEGER,
    duration_units CHARACTER(1),
    item_strength_units INTEGER,
    obsolete_consumption_uom VARCHAR(50),
    prescription_id INTEGER,
    admin_strength VARCHAR(100),
    username VARCHAR(30),
    refills VARCHAR(100),
    time_of_intake CHARACTER(1),
    obsolete_start_date DATE,
    obsolete_end_date DATE,
    priority VARCHAR(1),
    prescription_format CHARACTER(1),
    expiry_date TIMESTAMP,
    cons_uom_id INTEGER,
    PRIMARY KEY(revision_id,prescription_id)
);
COMMENT ON TABLE patient_other_medicine_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient other_medicine prescription log" }';

CREATE SEQUENCE patient_consultation_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_consultation_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient consultation prescription log revision id" }';

CREATE TABLE patient_consultation_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_consultation_prescriptions_audit_seq'),
    cons_remarks VARCHAR(2000),
    username VARCHAR(30),
    mod_time TIMESTAMP,
    activity_due_date TIMESTAMP,
    doctor_id VARCHAR(50),
    prescription_id INTEGER,
    preauth_required VARCHAR(1),
    admin_strength VARCHAR(100),
    dept_id VARCHAR(50),
    presc_activity_type VARCHAR,
    PRIMARY KEY(revision_id,prescription_id)
);
COMMENT ON TABLE patient_consultation_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient consultation prescription log" }';

CREATE SEQUENCE patient_service_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_service_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient service prescription log revision id" }';

CREATE TABLE patient_service_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_service_prescriptions_audit_seq'),
    service_remarks VARCHAR(2000),
    mod_time TIMESTAMP,
    activity_due_date TIMESTAMP,
    service_id VARCHAR(50),
    username VARCHAR(30),
    tooth_unv_number VARCHAR(200),
    tooth_fdi_number VARCHAR(200),
    qty INTEGER,
    preauth_required CHARACTER(1),
    op_service_pres_id INTEGER,
    admin_strength VARCHAR(100),
    priority VARCHAR(1),
    PRIMARY KEY(revision_id,op_service_pres_id)
);
COMMENT ON TABLE patient_service_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient service prescription log" }';

CREATE SEQUENCE patient_test_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_test_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient investigation prescription log revision id" }';

CREATE TABLE patient_test_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_test_prescriptions_audit_seq'),
    test_remarks VARCHAR(2000),
    mod_time TIMESTAMP,
    ispackage BOOLEAN,
    activity_due_date TIMESTAMP,
    test_id VARCHAR(50),
    username VARCHAR(30),
    preauth_required CHARACTER(1),
    op_test_pres_id INTEGER,
    admin_strength VARCHAR(100),
    priority VARCHAR(1),
    reorder CHARACTER(1),
    clinical_note_for_conduction VARCHAR,
    clinical_justification_for_item VARCHAR,
    PRIMARY KEY(revision_id,op_test_pres_id)
);
COMMENT ON TABLE patient_test_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient investigation prescription log" }';

CREATE SEQUENCE patient_prescription_audit_seq START 1;
COMMENT ON SEQUENCE patient_prescription_audit_seq IS '{ "type": "Txn", "comment": "Patient prescription log revision id" }';

CREATE TABLE patient_prescription_audit (
    revision_id BIGINT DEFAULT nextval('patient_prescription_audit_seq'),
    patient_presc_id INTEGER,
    mr_no VARCHAR(15),
    consultation_id INTEGER,
    presc_type VARCHAR(50),
    store_item boolean,
    status VARCHAR(5),
    no_order_reason VARCHAR(1000),
    prescribed_date TIMESTAMP,
    conducting_personnel VARCHAR(30),
    visit_id VARCHAR(15),
    cancelled_datetime TIMESTAMP,
    cancelled_by VARCHAR(30),
    pri_pre_auth_no VARCHAR(15),
    pri_pre_auth_mode_id INTEGER,
    sec_pre_auth_no VARCHAR(15),
    sec_pre_auth_mode_id INTEGER,
    special_instr VARCHAR(2000),
    external_order_no VARCHAR,
    username VARCHAR(30),
    item_excluded_from_doctor VARCHAR(3),
    item_excluded_from_doctor_remarks VARCHAR(500),
    doctor_id VARCHAR(15),
    prior_med CHARACTER(1),
    freq_type CHARACTER(1),
    recurrence_daily_id INTEGER,
    repeat_interval INTEGER,
    start_datetime TIMESTAMP,
    end_datetime TIMESTAMP,
    no_of_occurrences INTEGER,
    end_on_discontinue CHARACTER(1),
    discontinued CHARACTER(1),
    repeat_interval_units CHARACTER(1),
    adm_request_id INTEGER,
    max_doses INTEGER,
    doc_presc_id INTEGER,
    created_by VARCHAR(30),
    created_at TIMESTAMP,
    modified_by VARCHAR(30),
    modified_at TIMESTAMP,
    PRIMARY KEY (revision_id,patient_presc_id)
);
COMMENT ON TABLE patient_prescription_audit IS '{ "type": "Txn", "comment": "Patient prescription log" }';


CREATE SEQUENCE patient_medicine_prescriptions_audit_seq START 1;
COMMENT ON SEQUENCE patient_medicine_prescriptions_audit_seq IS '{ "type": "Txn", "comment": "Patient medicine prescription log revision id" }';

CREATE TABLE patient_medicine_prescriptions_audit (
    revision_id BIGINT DEFAULT nextval('patient_medicine_prescriptions_audit_seq'),
    frequency VARCHAR(150),
    medicine_quantity INTEGER,
    medicine_remarks VARCHAR(2000),
    mod_time TIMESTAMP,
    issued_qty NUMERIC,
    initial_sale_id VARCHAR(15),
    final_sale_id VARCHAR(15),
    activity_due_date TIMESTAMP,
    medicine_id INTEGER,
    route_of_admin INTEGER,
    strength VARCHAR(100),
    generic_code VARCHAR(10),
    item_form_id INTEGER,
    item_strength VARCHAR(50),
    visit_id VARCHAR(15),
    duration INTEGER,
    duration_units CHARACTER(1),
    pbm_presc_id INTEGER,
    item_strength_units INTEGER,
    erx_status CHARACTER(1),
    erx_denial_code VARCHAR(15),
    erx_denial_remarks VARCHAR,
    obsolete_consumption_uom VARCHAR(50),
    erx_approved_quantity NUMERIC,
    op_medicine_pres_id INTEGER,
    admin_strength VARCHAR(100),
    send_for_erx CHARACTER(1),
    username VARCHAR(30),
    refills VARCHAR(100),
    time_of_intake CHARACTER(1),
    obsolete_start_date DATE,
    obsolete_end_date DATE,
    priority VARCHAR(1),
    controlled_drug_number VARCHAR(50),
    prescription_format CHARACTER(1),
    expiry_date TIMESTAMP,
    flow_rate NUMERIC(10,2),
    flow_rate_units VARCHAR,
    infusion_period INTEGER,
    infusion_period_units VARCHAR,
    iv_administer_instructions VARCHAR,
    medication_type VARCHAR(2),
    cons_uom_id INTEGER,
    PRIMARY KEY(revision_id,op_medicine_pres_id)
);
COMMENT ON TABLE patient_medicine_prescriptions_audit IS '{ "type": "Txn", "comment": "Patient medicine prescription log" }';

CREATE SEQUENCE discharge_medication_details_audit_seq START 1;
COMMENT ON SEQUENCE discharge_medication_details_audit_seq IS '{ "type": "Txn", "comment": "discharge medication revision id" }';

CREATE TABLE discharge_medication_details_audit (
    revision_id BIGINT DEFAULT nextval('discharge_medication_details_audit_seq'),
    medicine_presc_id integer,
    medication_id integer,
    generic_code character varying(10),
    medicine_id integer,
    medicine_quantity integer,
    medicine_remarks character varying,
    route_of_admin integer,
    strength character varying(100),
    item_strength character varying(50),
    item_strength_units integer,
    admin_strength character varying(100),
    item_form_id integer,
    duration integer,
    duration_units character(1),
    obsolete_consumption_uom character varying(50),
    frequency character varying(50),
    special_instr character varying,
    mod_time timestamp without time zone,
    prescribed_date timestamp without time zone,
    issued character varying(5),
    cons_uom_id integer
);
COMMENT ON TABLE discharge_medication_details_audit IS '{ "type": "Txn", "comment": "discharge medication log" }';