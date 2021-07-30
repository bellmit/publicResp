--
-- Name: audit_logs; Type: TABLE; Schema: fresh; Owner: -; Tablespace:
--

CREATE TABLE audit_logs (
    log_id numeric NOT NULL,
    datetime timestamp without time zone NOT NULL,
    username character varying,
    db_table character varying NOT NULL,
    operation character varying NOT NULL,
    db_record_id character varying,
    db_field_name character varying,
    db_field_old_val character varying,
    db_field_new_val character varying
);

--
-- Name: anesthesia_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE anesthesia_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    anesthesia_type_id character varying,
    org_id character varying,
    bed_type character varying,
    min_charge numeric(10,2),
    slab_1_charge numeric(10,2),
    incr_charge numeric(10,2),
    min_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    incr_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    slab_1_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL
);

--
-- Name: bill_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE bill_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    bill_no character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);


--
-- Name: bill_charge_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE bill_charge_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    bill_no character varying(15),
    charge_id character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    charge_head character varying(20)
);

--
-- Name: bill_activity_charge_audit_log; Type: TABLE; Schema: fresh; Owner: -; Tablespace:
--

CREATE TABLE bill_activity_charge_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    charge_id character varying(15),
    activity_code character varying(20),
    activity_id character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    payment_charge_head character varying(20)
);




--
-- Name: bill_receipts_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE bill_receipts_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    receipt_no character varying(15)
);



--
-- Name: consultation_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE consultation_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    consultation_type_id integer,
    org_id character varying,
    bed_type character varying,
    charge numeric(10,2),
    discount numeric(20,2) DEFAULT 0.00
);


--
-- Name: deposits_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE deposits_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    deposit_no character varying(15)
);



--
-- Name: diagnostic_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE diagnostic_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    test_id character varying(10),
    org_name character varying(100),
    bed_type character varying(150)
);



--
-- Name: diagnostic_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE diagnostic_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    test_id character varying,
    org_name character varying,
    charge numeric(20,2),
    bed_type character varying,
    discount numeric(20,2) DEFAULT 0.00 NOT NULL
);



--
-- Name: diagnostics_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE diagnostics_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    test_id character varying(50)
);



--
-- Name: dialysis_prescriptions_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dialysis_prescriptions_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mr_no character varying(15),
    dialysis_presc_id integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: doctor_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE doctor_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    doctor_id character varying,
    bed_type character varying,
    org_id character varying,
    doctor_ip_charge numeric(10,2),
    night_ip_charge numeric(10,2),
    ot_charge numeric(10,2),
    co_surgeon_charge numeric(10,2),
    assnt_surgeon_charge numeric(10,2),
    doctor_ip_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    night_ip_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    ot_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    co_surgeon_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    assnt_surgeon_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    ward_ip_charge numeric(20,2) DEFAULT 0 NOT NULL,
    ward_ip_charge_discount numeric(20,2) DEFAULT 0 NOT NULL
);



--
-- Name: doctor_charges_op_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE doctor_charges_op_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    doctor_id character varying,
    org_id character varying,
    op_charge numeric(10,2),
    op_revisit_charge numeric(10,2),
    private_cons_charge numeric(10,2),
    private_cons_revisit_charge numeric(10,2),
    op_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    op_revisit_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    private_cons_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    private_revisit_discount numeric(20,2) DEFAULT 0.00 NOT NULL
);



--
-- Name: doctor_notes_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE doctor_notes_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    note_num integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: doctor_order_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE doctor_order_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    presc_type character(1),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    prescription_id integer
);



--
-- Name: dyna_package_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    dyna_package_id integer
);



--
-- Name: dyna_package_category_limits_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_category_limits_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    dyna_package_id integer,
    dyna_pkg_cat_id integer,
    bed_type character varying(50),
    org_id character varying(50)
);



--
-- Name: dyna_package_category_limits_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_category_limits_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    dyna_package_id integer,
    dyna_pkg_cat_id integer,
    bed_type character varying,
    org_id character varying,
    pkg_included character(1),
    amount_limit numeric(10,2),
    qty_limit numeric
);



--
-- Name: dyna_package_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    dyna_package_id integer,
    bed_type character varying(50),
    org_id character varying(50)
);



--
-- Name: dyna_package_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    dyna_package_id integer,
    bed_type character varying,
    org_id character varying,
    charge numeric(20,2)
);



--
-- Name: dyna_package_master_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE dyna_package_master_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    dyna_package_id integer,
    bed_type character varying(50),
    org_id character varying(50)
);



--
-- Name: equipment_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE equipment_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    equip_id character varying,
    bed_type character varying,
    org_id character varying,
    daily_charge numeric(10,2),
    min_charge numeric(10,2),
    incr_charge numeric(10,2),
    tax numeric(10,2),
    daily_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    min_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    incr_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL
);



--
-- Name: insurance_plan_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE insurance_plan_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    plan_id integer,
    insurance_category_id integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    old_value character varying,
    new_value character varying,
    field_name character varying
);



--
-- Name: insurance_plan_main_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE insurance_plan_main_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    plan_id integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    old_value character varying,
    new_value character varying,
    field_name character varying
);



--
-- Name: mrd_diagnosis_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE mrd_diagnosis_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    visit_id character varying(15),
    user_name character varying,
    mod_time timestamp with time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: nurse_notes_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE nurse_notes_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    note_num integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: obsolete_dyna_package_master_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE obsolete_dyna_package_master_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    dyna_package_id integer,
    bed_type character varying,
    org_id character varying,
    charge numeric(10,2),
    consultation_limit numeric(10,2),
    services_limit numeric(10,2),
    diag_limit numeric(10,2),
    medicine_limit numeric(10,2)
);



--
-- Name: operation_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE operation_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    op_id character varying(10),
    org_id character varying(10),
    bed_type character varying(150)
);



--
-- Name: operation_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE operation_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    op_id character varying,
    bed_type character varying,
    org_id character varying,
    surg_asstance_charge numeric(10,2),
    surgeon_charge numeric(10,2),
    anesthetist_charge numeric(10,2),
    surg_asst_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    surg_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    anest_discount numeric(20,2) DEFAULT 0.00 NOT NULL
);


--
-- Name: operation_master_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE operation_master_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    op_id character varying(10)
);



--
-- Name: patient_activities_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_activities_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    activity_id integer
);



--
-- Name: patient_allergies_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_allergies_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    obsolete_mr_no character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    section_detail_id integer
);



--
-- Name: patient_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mr_no character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: patient_documents; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_documents (
    doc_id integer NOT NULL,
    template_id integer,
    doc_format character varying(40) NOT NULL,
    doc_content_text text,
    doc_content_bytea bytea,
    content_type character varying,
    doc_type character varying NOT NULL,
    original_extension character varying(100),
    pheader_template_id integer,
    doc_number character varying(50),
    doc_location character varying(1500),
    doc_status character(1) DEFAULT 'P'::bpchar NOT NULL
);



--
-- Name: COLUMN patient_documents.doc_status; Type: COMMENT; Schema: fresh; Owner: postgres
--

COMMENT ON COLUMN patient_documents.doc_status IS 'Valid values are P/F In Progress/Finalized, Finalized documents cannot be edited.';


--
-- Name: patient_general_docs_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_general_docs_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mr_no character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: patient_pdf_form_doc_values_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_pdf_form_doc_values_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    doc_id integer,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: patient_registration_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE patient_registration_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: payment_rules_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE payment_rules_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    charge_head character varying(10)
);



--
-- Name: payments_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE payments_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    voucher_no character varying(20),
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: payments_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE payments_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    voucher_no character varying(20),
    payment_id character varying(20),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: per_diem_codes_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE per_diem_codes_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    per_diem_code character varying
);



--
-- Name: per_diem_codes_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE per_diem_codes_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    per_diem_code character varying,
    bed_type character varying(50),
    org_id character varying(50)
);



--
-- Name: registration_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE registration_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    org_id character varying(50),
    ip_reg_charge numeric(20,2),
    op_reg_charge numeric(20,2),
    gen_reg_charge numeric(20,2),
    reg_renewal_charge numeric(20,2),
    mrcharge numeric(20,2),
    ip_mlccharge numeric(20,2),
    gen_reg_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    op_reg_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    reg_renewal_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    ip_reg_charge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    ip_mlccharge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    mrcharge_discount numeric(20,2) DEFAULT 0.00 NOT NULL,
    bed_type character varying(50),
    op_mlccharge numeric(20,2) DEFAULT 0.00 NOT NULL,
    op_mlccharge_discount numeric(20,2) DEFAULT 0.00 NOT NULL
);



--
-- Name: sample_collection_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE sample_collection_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(50),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    sample_collection_id integer
);



--
-- Name: scheduler_appointment_items_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE scheduler_appointment_items_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    appointment_id integer,
    appointment_item_id integer,
    resource_type character varying(10),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: scheduler_appointments_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE scheduler_appointments_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    mr_no character varying(15),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    appointment_id integer DEFAULT 0,
    patient_name character varying(100)
);



--
-- Name: secondary_complaints_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE secondary_complaints_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    visit_id character varying(15),
    user_name character varying,
    mod_time timestamp with time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: service_master_charges_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE service_master_charges_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    service_id character varying(50),
    bed_type character varying(50),
    org_id character varying(50)
);



--
-- Name: service_master_charges_backup; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE service_master_charges_backup (
    user_name character varying,
    bkp_time timestamp without time zone,
    service_id character varying,
    bed_type character varying,
    org_id character varying,
    unit_charge numeric(10,2),
    discount numeric(20,2) DEFAULT 0.00 NOT NULL
);



--
-- Name: services_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE services_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    service_id character varying(10)
);



--
-- Name: store_checkpoint_details; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE store_checkpoint_details (
    checkpoint_id integer NOT NULL,
    batch_no character varying(50) NOT NULL,
    qty numeric,
    medicine_id integer,
    store_id integer,
    cp numeric(10,2),
    mrp numeric(10,2)
);



--
-- Name: store_item_batch_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE store_item_batch_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    medicine_id integer,
    item_batch_id integer,
    batch_no character varying(50)
);



--
-- Name: store_stock_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE store_stock_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    batch_no character varying(50),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying,
    record_type character(1) DEFAULT 'P'::bpchar,
    medicine_id integer,
    dept_id integer
);



--
-- Name: test_details_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE test_details_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    id integer,
    prescribed_id integer,
    test_id character varying(10),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: test_visit_reports_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE test_visit_reports_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    report_id integer,
    patient_id character varying(15),
    category character varying(10),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: tests_conducted_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE tests_conducted_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    prescribed_id integer,
    patient_id character varying(15),
    test_id character varying(10),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: tests_prescribed_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE tests_prescribed_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    prescribed_id integer,
    pat_id character varying(15),
    test_id character varying(10),
    user_name character varying,
    mod_time timestamp without time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: visit_vitals_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE visit_vitals_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    patient_id character varying(15),
    vital_reading_id integer,
    user_name character varying,
    mod_time timestamp with time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: vital_reading_audit_log; Type: TABLE; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE TABLE vital_reading_audit_log (
    log_id integer DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    vital_reading_id integer,
    param_id integer,
    user_name character varying,
    mod_time timestamp with time zone DEFAULT now() NOT NULL,
    operation character varying,
    field_name character varying,
    old_value character varying,
    new_value character varying
);



--
-- Name: patient_documents_docid_pkey; Type: CONSTRAINT; Schema: fresh; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY patient_documents
    ADD CONSTRAINT patient_documents_docid_pkey PRIMARY KEY (doc_id);


--
-- Name: bill_audit_log_bill_no_idx; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX bill_audit_log_bill_no_idx ON bill_audit_log USING btree (bill_no);


--
-- Name: bill_audit_log_field_name_idx; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX bill_audit_log_field_name_idx ON bill_audit_log USING btree (field_name);


--
-- Name: bill_audit_log_mod_date_idx; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX bill_audit_log_mod_date_idx ON bill_audit_log USING btree (mod_time);


--
-- Name: bill_charge_audit_log_bill_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX bill_charge_audit_log_bill_index ON bill_charge_audit_log USING btree (bill_no);


--
-- Name: bill_charge_audit_log_bill_no_idx; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX bill_charge_audit_log_bill_no_idx ON bill_charge_audit_log USING btree (bill_no);


--
-- Name: diagnostic_charges_audit_log_test_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX diagnostic_charges_audit_log_test_index ON diagnostic_charges_audit_log USING btree (test_id);


--
-- Name: diagnostics_audit_log_test_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX diagnostics_audit_log_test_index ON diagnostics_audit_log USING btree (test_id);


--
-- Name: dialysis_prescriptions_audit_log_mr_no_idx; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX dialysis_prescriptions_audit_log_mr_no_idx ON dialysis_prescriptions_audit_log USING btree (mr_no);


--
-- Name: dyna_package_audit_log_pkg_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX dyna_package_audit_log_pkg_index ON dyna_package_audit_log USING btree (dyna_package_id);


--
-- Name: dyna_package_master_charges_audit_log_pkg_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX dyna_package_master_charges_audit_log_pkg_index ON dyna_package_master_charges_audit_log USING btree (dyna_package_id);


--
-- Name: operation_charges_audit_log_op_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX operation_charges_audit_log_op_index ON operation_charges_audit_log USING btree (op_id);


--
-- Name: operation_master_audit_log_op_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX operation_master_audit_log_op_index ON operation_master_audit_log USING btree (op_id);


--
-- Name: patient_details_audit_log_mr_no_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX patient_details_audit_log_mr_no_index ON patient_details_audit_log USING btree (mr_no);


--
-- Name: patient_registration_audit_log_patient_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX patient_registration_audit_log_patient_id_index ON patient_registration_audit_log USING btree (patient_id);


--
-- Name: payment_rules_audit_log_rule_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX payment_rules_audit_log_rule_index ON payment_rules_audit_log USING btree (charge_head);


--
-- Name: payments_audit_log_voucher_no_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX payments_audit_log_voucher_no_index ON payments_audit_log USING btree (voucher_no);


--
-- Name: payments_details_audit_log_voucher_no_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX payments_details_audit_log_voucher_no_index ON payments_details_audit_log USING btree (voucher_no);


--
-- Name: scheduler_appointment_items_audit_log_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX scheduler_appointment_items_audit_log_index ON scheduler_appointment_items_audit_log USING btree (appointment_id);


--
-- Name: scheduler_appointments_audit_log_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX scheduler_appointments_audit_log_index ON scheduler_appointments_audit_log USING btree (appointment_id);


--
-- Name: service_master_charges_audit_log_service_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX service_master_charges_audit_log_service_id_index ON service_master_charges_audit_log USING btree (service_id);


--
-- Name: services_audit_log_service_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX services_audit_log_service_id_index ON services_audit_log USING btree (service_id);


--
-- Name: stk_chkpt; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX stk_chkpt ON store_checkpoint_details USING btree (checkpoint_id);


--
-- Name: store_stock_details_audit_log_med_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX store_stock_details_audit_log_med_index ON store_stock_details_audit_log USING btree (medicine_id);


--
-- Name: test_details_audit_log_presc_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX test_details_audit_log_presc_id_index ON test_details_audit_log USING btree (prescribed_id);


--
-- Name: test_visit_reports_audit_log_patient_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX test_visit_reports_audit_log_patient_id_index ON test_visit_reports_audit_log USING btree (patient_id);


--
-- Name: tests_conducted_audit_log_presc_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX tests_conducted_audit_log_presc_id_index ON tests_conducted_audit_log USING btree (prescribed_id);


--
-- Name: tests_prescribed_audit_log_prescribed_id_index; Type: INDEX; Schema: fresh; Owner: postgres; Tablespace:
--

CREATE INDEX tests_prescribed_audit_log_prescribed_id_index ON tests_prescribed_audit_log USING btree (prescribed_id);


--
-- Name: chkpt_id_fk; Type: FK CONSTRAINT; Schema: fresh; Owner: postgres
--

ALTER TABLE ONLY store_checkpoint_details
    ADD CONSTRAINT chkpt_id_fk FOREIGN KEY (checkpoint_id) REFERENCES store_checkpoint_main(checkpoint_id);


