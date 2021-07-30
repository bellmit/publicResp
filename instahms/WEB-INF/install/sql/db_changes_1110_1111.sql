--
-- To run this script, use psql, set search path to the schema, and execute the file using \i
--

SET client_min_messages = warning;

--
-- Useful functions that are often used in db_changes. Needs to be here,
-- Since we may run this on a fresh schema where vft.sql has not been run.
--
CREATE OR REPLACE FUNCTION remove_dups_on (tbl TEXT, on_column TEXT) RETURNS VOID AS $$
BEGIN
 EXECUTE 'CREATE TEMP TABLE tmp_for_dups AS SELECT DISTINCT ON('
	|| on_column || ') * FROM ' || quote_ident(tbl);
 EXECUTE 'DELETE FROM ' || quote_ident(tbl);
 EXECUTE 'INSERT INTO ' || quote_ident(tbl) || ' (SELECT * FROM tmp_for_dups)';
 DROP TABLE tmp_for_dups;
END;
$$ LANGUAGE plpgsql;

-- db_changes starts here. --

CREATE SEQUENCE item_groups_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

CREATE SEQUENCE item_sub_groups_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

DROP TABLE IF EXISTS item_group_type;
CREATE TABLE item_group_type (
    item_group_type_id character varying(10) NOT NULL PRIMARY KEY,
    item_group_type_name character varying(50) NOT NULL,
    system_group character(1) NOT NULL,
    description character(1000),
    status character varying(1)
);

DROP TABLE IF EXISTS item_groups;
CREATE TABLE item_groups (
    item_group_id integer NOT NULL PRIMARY KEY,
    item_group_name character varying(50) NOT NULL,
    group_code character varying(6) NOT NULL,
    item_group_display_order integer,
    item_group_type_id character varying(10) NOT NULL,
    status character varying(1)
);

DROP TABLE IF EXISTS item_sub_groups;
CREATE TABLE item_sub_groups (
    item_subgroup_id integer NOT NULL PRIMARY KEY,
    item_subgroup_name character varying(50) NOT NULL,
    item_subgroup_display_order integer,
    subgroup_code character varying(6),
    item_group_id integer NOT NULL,
    status character varying(1)
);

DROP TABLE IF EXISTS item_sub_groups_tax_details;
CREATE TABLE item_sub_groups_tax_details (
    item_subgroup_id integer NOT NULL PRIMARY KEY,
    tax_rate numeric,
    tax_rate_expr character varying(1000),
    validity_start date NOT NULL,
    validity_end date
);

DROP TABLE IF EXISTS store_item_sub_groups;
CREATE TABLE store_item_sub_groups (
    medicine_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(medicine_id, item_subgroup_id)
);

DROP TABLE IF EXISTS store_po_tax_details;
CREATE TABLE store_po_tax_details (
    medicine_id integer NOT NULL,
    po_no character varying(20) NOT NULL,
    item_subgroup_id integer NOT NULL,
    tax_rate numeric,
    tax_amt numeric(15,2),
    PRIMARY KEY(medicine_id, po_no, item_subgroup_id)
);

INSERT INTO item_group_type VALUES ('TAX', 'TAX', 'S', '','A');


ALTER TABLE system_generated_sections ADD COLUMN op_follow_up_consult_form character varying(1) default 'Y';
INSERT INTO form_components(dept_id, doctor_id, id, sections, form_type, form_name) 
	VALUES('-1', '-1', nextval('form_components_seq'), '-6', 'Form_OP_FOLLOW_UP_CONS', 'OP Follow Up Consultation Form');

UPDATE form_components set form_name ='OP Follow Up Consultation' where form_name = 'OP Follow Up Consultation Form' AND
form_type ='Form_OP_FOLLOW_UP_CONS';

UPDATE system_generated_sections SET section_name = 'Pre-Anaesthetic Checkup (Sys)' WHERE section_id = -16;
UPDATE form_components SET sections ='-1,-2,-4,-6,-7' WHERE  form_name='OP Follow Up Consultation' AND form_type='Form_OP_FOLLOW_UP_CONS';
UPDATE system_generated_sections SET op_follow_up_consult_form = op;
-- db changes for availability view
insert into screen_rights (select role_id, 'doc_scheduler_available_slots', rights from screen_rights where screen_id in ('doc_scheduler'));
insert into url_action_rights (select role_id, 'doc_scheduler_available_slots', rights from url_action_rights where action_id in ('doc_scheduler'));
delete from url_action_rights where action_id = 'reg_registration';
delete from screen_rights where screen_id = 'reg_registration';

CREATE SEQUENCE insurance_plan_details_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE insurance_plan_details ADD COLUMN insurance_plan_details_id INTEGER DEFAULT nextval('insurance_plan_details_seq');

ALTER TABLE insurance_plan_details ADD CONSTRAINT insurance_plan_details_id_uq  UNIQUE (insurance_plan_details_id);

ALTER TABLE bill_charge ADD COLUMN tax_amt numeric(15,2) NOT NULL DEFAULT 0, ADD COLUMN sponsor_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;
ALTER TABLE bill ADD COLUMN total_tax numeric(15,2) NOT NULL DEFAULT 0, ADD COLUMN total_claim_tax numeric(15,2) NOT NULL DEFAULT 0.00;
ALTER TABLE auto_save_json_data DROP COLUMN auto_save_id, DROP COLUMN display_order, ADD COLUMN auto_save_section_id character varying not null;
ALTER TABLE hospital_center_master ADD COLUMN tin_number character varying(50);
ALTER TABLE insurance_company_master ADD COLUMN tin_number character varying(50);
ALTER TABLE tpa_master ADD COLUMN tin_number character varying(50), ADD COLUMN claim_amount_includes_tax character(1) default 'N', ADD COLUMN limit_includes_tax character(1) default 'N';

-- increase column size of rejection_reason_category_name to 250
ALTER TABLE rejection_reason_categories ALTER COLUMN rejection_reason_category_name type varchar(250);

--_INCR_ONLY_ ALTER TABLE store_supplier_contracts_item_rates ADD COLUMN mrp numeric(15,2) DEFAULT 0.00;

CREATE INDEX pbm_request_id_idx ON pbm_prescription USING btree(pbm_request_id);

-- Build 11.11.0-7891

-- Build 11.11.0-7895

-- Build 11.11.0-7897

-- Build 11.11.0-7901

-- Build 11.11.0-7903

CREATE INDEX idx_insurance_claim_status ON insurance_claim USING btree (status);
CREATE INDEX idx_insurance_claim_account_group ON insurance_claim USING btree (account_group);

-- Build 11.11.0-7908

-- Build 11.11.0-7909

-- Taxation DB Changes Starts Here --

-- Build 11.11.0-7912

CREATE TABLE operation_item_sub_groups (
    op_id character varying(10) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(op_id, item_subgroup_id)
);

CREATE TABLE service_item_sub_groups (
    service_id character varying(10) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(service_id, item_subgroup_id)
);

CREATE TABLE diagnostics_item_sub_groups (
    test_id character varying(50) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(test_id, item_subgroup_id)
);

CREATE TABLE consultation_item_sub_groups (
    consultation_type_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(consultation_type_id, item_subgroup_id)
);

CREATE TABLE bed_item_sub_groups (
    bed_type_name character varying(50) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(bed_type_name, item_subgroup_id)
);

CREATE TABLE drg_code_item_sub_groups (
	    drg_code character varying(15) NOT NULL,
	    item_subgroup_id integer NOT NULL,
	    PRIMARY KEY(drg_code, item_subgroup_id)
);

CREATE TABLE theatre_item_sub_groups (
    theatre_id character varying(10) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(theatre_id, item_subgroup_id)
);

CREATE TABLE anesthesia_item_sub_groups (
    anesthesia_type_id character varying(50) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(anesthesia_type_id, item_subgroup_id)
);

CREATE TABLE common_item_sub_groups (
    charge_name character varying(50) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(charge_name, item_subgroup_id)
);

CREATE TABLE equipment_item_sub_groups (
	eq_id character varying(100) NOT NULL,
	item_subgroup_id integer NOT NULL,
	PRIMARY KEY(eq_id, item_subgroup_id)
);

CREATE TABLE dietary_item_sub_groups (
    diet_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(diet_id, item_subgroup_id)
);

CREATE TABLE perdiem_code_item_sub_groups (
    per_diem_code character varying(15) NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(per_diem_code, item_subgroup_id)
);

CREATE TABLE package_item_sub_groups (
    package_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(package_id, item_subgroup_id)
);

-- Build 11.11.0-7895

-- Build 11.11.0-7897

--CREATE TABLE dyna_package_item_sub_groups (
-- 	dyna_package_id integer NOT NULL,
--    item_subgroup_id integer NOT NULL,
--    PRIMARY KEY(dyna_package_id, item_subgroup_id)
--);

-- Build 11.11.0-7914

-- Build 11.11.0-7916

-- Build 11.11.0-7918

-- Build 11.11.0-7920

-- Taxation DB Changes Starts Here --

CREATE TABLE bill_charge_tax(
 charge_id character varying(15) not null,
 tax_sub_group_id integer not null,
 tax_rate numeric(15,2) NOT NULL DEFAULT 0.00,
 tax_amount numeric(15,2) NOT NULL DEFAULT 0.00
);

-- Build 11.11.0-7895

-- Build 11.11.0-7897

ALTER TABLE bill_charge_claim ADD COLUMN tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

CREATE TABLE bill_charge_claim_tax(
 charge_id character varying(15) NOT NULL,
 claim_id character varying(15) NOT NULL,
 tax_sub_group_id integer NOT NULL,
 tax_rate numeric(15,2) NOT NULL default 0.00,
 sponsor_tax_amount numeric(15,2) NOT NULL default 0
);

-- Taxation DB Changes ENDS Here --

-- Build 11.11.0-7914

DROP TABLE IF EXISTS store_sales_tax_details;
CREATE TABLE store_sales_tax_details(
	sale_item_id integer not null,
	item_subgroup_id integer not null,
	tax_rate numeric,
	tax_amt numeric(10,2) default 0,
	PRIMARY KEY(sale_item_id, item_subgroup_id)
);

-- Build 11.11.0-7920

ALTER TABLE sales_claim_details ADD COLUMN tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

CREATE TABLE sales_claim_tax_details(
 sale_item_id integer NOT NULL,
 claim_id character varying(15) NOT NULL,
 item_subgroup_id integer NOT NULL,
 tax_rate numeric(15,2) NOT NULL default 0.00,
 tax_amt numeric(15,2) NOT NULL default 0
);

-- Build 11.11.0-7922

-- Build 11.11.0-7923

-- Build 11.11.0-7925

-- Build 11.11.0-7932

-- Build 11.11.0-7933

-- Build 11.11.0-7935

-- Build 11.11.0-7937

-- Build 11.11.0-7939

ALTER TABLE ip_doctor_notes ALTER COLUMN notes TYPE text;

-- Build 11.11.0-7941


-- Build 11.11.0-7942

-- Build 11.11.0-7944

-- Build 11.11.0-7947

ALTER TABLE store_sales_details ADD COLUMN sponsor_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00, ADD COLUMN return_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

-- Build 11.11.0-7950

-- Build 11.11.0-7952

-- Build 11.11.0-7954

-- Build 11.11.0-7956

-- Build 11.11.0-7957

-- Build 11.11.0-7958

-- Build 11.11.0-7960

-- Build 11.11.0-7961

insert into insta_integration (integration_name, url, userid, password, status, environment) values('DBAJ_SMS_gateway', 'http://www.ducont.ae/smscompanion/smsadmin/rapidegovsmssubmit.asp', 'DBAJ', 'DBAJ2017', 'I', 'PROD');

-- Build 11.11.0-7964

-- Build 11.11.0-7966

-- Build 11.11.0-7967

-- Build 11.11.0-7968

ALTER TABLE bill_charge ADD COLUMN return_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

-- Build 11.11.0-7969

-- Build 11.11.0-7970

-- Build 11.11.0-7970


--_INCR_ONLY_ ALTER TABLE store_supplier_contracts_item_rates ALTER COLUMN discount DROP NOT NULL, ALTER COLUMN discount DROP DEFAULT;
--_INCR_ONLY_ ALTER TABLE store_supplier_contracts_item_rates ALTER COLUMN mrp DROP NOT NULL, ALTER COLUMN mrp DROP DEFAULT;

-- Build 11.11.0-7971

-- Build 11.11.0-7972

-- Build 11.11.0-7972

-- Build 11.11.0-7973

-- Build 11.11.1-7974

-- Build 11.11.1-7975

-- Build 11.11.1-7977

-- Build 11.11.1-7979

-- Build 11.11.1-7980

-- Build 11.11.1-7982

-- Build 11.11.1-7983

-- Build 11.11.1-7985

-- Build 11.11.1-7986

-- Build 11.11.1-7987

-- Build 11.11.1-7989

DROP INDEX IF EXISTS diagnostic_dependent_test_id_idx;
CREATE INDEX diagnostic_dependent_test_id_idx ON diagnostics(dependent_test_id);

-- Build 11.11.1-7991

-- Build 11.11.1-7993

-- Build 11.11.1-7994

-- Build 11.11.1-7996

-- Build 11.11.1-7998

-- Build 11.11.1-7999

-- Build 11.11.1-8000

-- Build 11.11.1-8002

-- Build 11.11.1-8003

-- Build 11.11.2-8005

-- Build 11.11.2-8006

CREATE SEQUENCE bill_charge_tax_seq;
ALTER TABLE bill_charge_tax ADD COLUMN charge_tax_id integer not null default nextval('bill_charge_tax_seq'), ADD CONSTRAINT charge_tax_id_pkey PRIMARY KEY(charge_tax_id);
UPDATE prescription_print_template SET prescription_template_content=REPLACE(prescription_template_content, 'PhysicianForms[tpf.section_title]', 'PhysicianForms[''sd_'' + tpf.section_detail_id]');

UPDATE print_templates SET print_template_content=REPLACE(print_template_content, 'PhysicianForms[tpf.section_title]', 'PhysicianForms[''sd_'' + tpf.section_detail_id]') WHERE template_type in ('Assessment', 'Triage', 'VisitSummaryRecord', 'S') AND built_in=false;

UPDATE common_print_templates SET template_content=REPLACE(template_content, 'insta_sections_data[record.section_title]', 'insta_sections_data[''sd_'' + record.section_detail_id]') WHERE template_type='InstaGenericForm';

-- Build 11.11.2-8008

-- Build 11.11.2-8009

-- Build 11.11.2-8010

-- Build 11.11.2-8013

-- Build 11.11.2-8019

-- Build 11.11.3-8025

-- Build 11.11.3-8028

-- Build 11.11.3-8030
--_INCR_ONLY_ CREATE INDEX activity_id_idx ON package_componentdetail(activity_id);
--_INCR_ONLY_ CREATE INDEX pkg_comp_charge_head_idx ON package_componentdetail(charge_head);
--_INCR_ONLY_ CREATE INDEX pack_operation_id_idx ON pack_master(operation_id);
--_INCR_ONLY_ CREATE INDEX pkg_comp_consultation_type_idx ON package_componentdetail(consultation_type_id);
--_INCR_ONLY_ ALTER TABLE store_po_main ADD COLUMN last_modified_by character varying(30);
--_INCR_ONLY_ COMMENT ON COLUMN store_po_main.user_id IS 'This column is referred as Raised By in PO related reports';
--_INCR_ONLY_ UPDATE store_po_main SET last_modified_by = user_id;




-- Build 11.11.3-8034

-- Build 11.11.3-8038

-- Build 11.11.3-8040

-- For Migration
--_INCR_ONLY_ update store_supplier_contracts_item_rates set discount = null  where discount = 0;
--_INCR_ONLY_ update store_supplier_contracts_item_rates set mrp = null where mrp=0.00;


-- Build 11.11.3-8042

-- Build 11.11.3-8043

-- Build 11.11.3-8045

-- Build 11.11.3-8048

-- Build 11.11.3-8051

-- Build 11.11.3-8052

-- Build 11.11.3-8053

-- Build 11.11.3-8055
ALTER TABLE insurance_remittance ADD COLUMN mod_time timestamp without time zone DEFAULT now() NOT NULL;
-- Build 11.11.3-8057

CREATE TABLE bill_charge_details_adjustment (
	charge_adjustment_detail_id character varying(15) not null,
	charge_id character varying(15) NOT NULL,
	claim_id character varying(15),
	txn_id integer NOT NULL,
	txn_type character varying(1) NOT NULL,
	mod_time timestamp with time zone NOT NULL,
    tax_sub_group_id integer NOT NULL,
    tax_rate numeric(15,2) DEFAULT 0.00 NOT NULL,
    tax_amt numeric(15,2) DEFAULT 0.00 NOT NULL,
    sponsor_tax_amount numeric(15,2) DEFAULT 0 NOT NULL,
    old_claim_id character varying(15),
    old_tax_sub_group_id integer,
    old_tax_rate numeric(15,2),
    old_tax_amt numeric(15,2),
    old_sponsor_tax_amount numeric(15,2)
);

CREATE SEQUENCE bill_charge_details_adjustment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

INSERT INTO hosp_id_patterns VALUES('BILL_CHARGE_DETAILS_ADJ_DEFAULT','CADE','','99000000','bill_charge_details_adjustment_seq',null,'','Txn');
UPDATE insurance_remittance SET mod_time = received_date;
-- Build 11.11.3-8058

-- Build 11.11.3-8059

-- Build 11.11.3-8061

-- Build 11.11.3-8063

--_INCR_ONLY_ CREATE INDEX patient_details_original_mrno_ends_with_idx on patient_details(reverse(COALESCE(original_mr_no,'')) varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX patient_details_oldmrno_ends_with_idx on patient_details(reverse(COALESCE(oldmrno,'')) varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX patient_details_lower_govt_id_idx on patient_details(lower(government_identifier) varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX patient_details_sanitized_patient_phone_idx on patient_details(replace(patient_phone, CASE WHEN patient_phone_country_code IS NULL THEN '' ELSE patient_phone_country_code END, '') varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX scheduler_appointments_sanitized_patient_contact_idx on scheduler_appointments(replace(patient_contact, CASE WHEN patient_contact_country_code IS NULL THEN '' ELSE patient_contact_country_code END, '') varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX scheduler_appointments_patient_contact_idx on scheduler_appointments(patient_contact varchar_pattern_ops);
--_INCR_ONLY_ CREATE INDEX scheduler_appointments_patient_name_idx on scheduler_appointments(patient_name varchar_pattern_ops);

-- Build 11.11.3-8064

-- Build 11.11.3-8066

ALTER TABLE bill_charge_details_adjustment ALTER COLUMN txn_id TYPE bigint;
ALTER TABLE generic_preferences ADD COLUMN registration_on_arrival character(1) NOT NULL DEFAULT 'Y';

-- Build 11.11.3-8068

-- Build 11.11.3-8070

-- Build 11.11.3-8071

-- Build 11.11.3-8072

-- Build 11.11.3-8074

-- Build 11.11.3-8077

-- Build 11.11.3-8080

-- Build 11.11.4-8085

-- Build 11.11.4-8090

CREATE INDEX idx_tpa_master_status ON tpa_master USING btree(status);
CREATE INDEX idx_insurance_company_master_status ON insurance_company_master USING btree(status);
CREATE INDEX idx_pbm_medicine_prescriptions_consultation_id ON pbm_medicine_prescriptions USING btree(consultation_id);

-- Build 11.11.4-8093

-- Build 11.11.4-8095

-- Build 11.11.4-8097

CREATE INDEX idx_mrd_diagnosis_doctor_id ON mrd_diagnosis USING btree(doctor_id);
CREATE INDEX idx_mrd_diagnosis_diagnosis_status_id ON mrd_diagnosis USING btree(diagnosis_status_id);
create index clinical_lab_recorded_mr_no_idx on clinical_lab_recorded(mrno);
alter table ip_prescription add primary key (prescription_id);
create index ip_prescription_patient_id_idx on ip_prescription(patient_id);
create index patient_activities_presc_id_idx on patient_activities(prescription_id);

-- Build 11.11.4-8099

-- Build 11.11.4-8102

-- Build 11.11.4-8105
CREATE INDEX idx_store_sales_main_charge ON store_sales_main USING btree(charge_id);
CREATE INDEX idx_bill_charge_claim_tax_sugrp ON bill_charge_claim_tax USING btree(tax_sub_group_id);
CREATE INDEX idx_bill_charge_claim_tax_claim_id ON bill_charge_claim_tax USING btree(claim_id);
CREATE INDEX idx_bill_charge_claim_tax_charge_id ON bill_charge_claim_tax USING btree(charge_id);
CREATE INDEX idx_bill_adjustment_alerts_visit_id ON bill_adjustment_alerts USING btree(visit_id);
CREATE INDEX idx_message_attachments_message_type_id ON message_attachments USING btree(message_type_id);
CREATE INDEX idx_message_config_message_type_id ON message_config USING btree(message_type_id);
CREATE INDEX idx_doctor_medicine_favourites_medicine_id ON doctor_medicine_favourites USING btree(medicine_id);
CREATE INDEX patient_registration_dept_name_idx ON patient_registration USING btree (dept_name);
CREATE INDEX patient_registration_admitted_dept_idx ON patient_registration USING btree (admitted_dept);
CREATE INDEX patient_registration_ward_id_idx ON patient_registration USING btree (ward_id);
CREATE INDEX patient_registration_doctor_idx ON patient_registration USING btree (doctor);
CREATE INDEX patient_registration_org_id_idx ON patient_registration USING btree (org_id);
CREATE INDEX patient_registration_plan_id_idx ON patient_registration USING btree (plan_id);
CREATE INDEX patient_registration_patient_category_id_idx ON patient_registration USING btree (patient_category_id);
CREATE INDEX patient_registration_secondary_sponsor_id_idx ON patient_registration USING btree (secondary_sponsor_id);
CREATE INDEX patient_registration_secondary_insurance_co_idx ON patient_registration USING btree (secondary_insurance_co);
CREATE INDEX patient_registration_patient_corporate_id_idx ON patient_registration USING btree (patient_corporate_id);
CREATE INDEX patient_registration_patient_national_sponsor_id_idx ON patient_registration USING btree (patient_national_sponsor_id);
CREATE INDEX patient_details_salutation_idx ON patient_details USING btree (salutation);
CREATE INDEX patient_details_patient_city_idx ON patient_details USING btree (patient_city);
CREATE INDEX patient_details_patient_state_idx ON patient_details USING btree (patient_state);
CREATE INDEX patient_details_country_idx ON patient_details USING btree (country);
CREATE INDEX patient_details_nationality_id_idx ON patient_details USING btree (nationality_id);
CREATE INDEX patient_details_death_reason_id_idx ON patient_details USING btree (death_reason_id);
CREATE INDEX patient_policy_details_status_idx ON patient_policy_details USING btree(status);
CREATE INDEX patient_insurance_plans_priority_idx ON patient_insurance_plans USING btree(priority);
CREATE INDEX admission_bed_id_idx ON admission USING btree (bed_id);
CREATE INDEX bed_names_ward_no_idx ON bed_names USING btree (ward_no);
CREATE INDEX mrd_casefile_attributes_issued_to_dept_idx ON mrd_casefile_attributes USING btree (issued_to_dept);
 
 -- Index to optimize performance of services prescribed queries
CREATE INDEX services_prescribed_presc_date_idx on services_prescribed using btree(presc_date);
CREATE INDEX services_prescribed_service_id_idx on services_prescribed using btree(service_id);
CREATE INDEX services_prescribed_conducted_idx on services_prescribed using btree(conducted);
CREATE INDEX service_documents_coalesce_signed_off_idx on service_documents using btree(coalesce(signed_off,false));
CREATE INDEX services_serv_dept_id_text_idx ON services using btree(CAST (serv_dept_id AS TEXT));
 
 CREATE INDEX bill_bill_rate_plan_id_idx ON bill USING btree(bill_rate_plan_id);
 CREATE INDEX consultation_org_details_org_id_idx ON consultation_org_details USING btree(org_id);
 CREATE INDEX consultation_org_details_consultation_type_id_idx ON consultation_org_details USING btree(consultation_type_id);
 CREATE INDEX consultation_org_details_code_type_idx ON consultation_org_details USING btree(code_type);
 CREATE INDEX consultation_org_details_item_code_type_idx ON consultation_org_details USING btree(item_code);
 CREATE INDEX mrd_codes_master_code_type_idx ON mrd_codes_master USING btree(code_type);
 CREATE INDEX mrd_codes_master_code_idx ON mrd_codes_master USING btree(code);
 CREATE INDEX service_sub_groups_status_idx ON service_sub_groups USING btree(status);
 CREATE INDEX doctor_consultation_cancel_status_idx ON doctor_consultation USING btree(cancel_status);
-- Build 11.11.4-8107

-- Build 11.11.4-8109

-- Build 11.11.4-8112

-- Build 11.11.4-8114

-- Build 11.11.4-8116

-- Build 11.11.4-8118

create index patient_section_forms_form_type_idx on patient_section_forms(form_type);
--_INCR_ONLY_ ALTER TABLE generic_preferences ADD COLUMN appointment_name_order character varying(5) default 'FML';

CREATE INDEX sid_insurance_category_id_idx on store_item_details(insurance_category_id);
CREATE INDEX pmp_item_form_idx on patient_medicine_prescriptions(item_form_id);
CREATE INDEX pmp_generic_code_idx on patient_medicine_prescriptions(generic_code);
CREATE INDEX pmp_route_of_admin_idx on patient_medicine_prescriptions(route_of_admin);
CREATE INDEX pmp_item_strength_units_idx on patient_medicine_prescriptions(item_strength_units);
CREATE INDEX ptp_test_id_idx on patient_test_prescriptions(test_id);
CREATE INDEX psm_pack_id_idx on package_sponsor_master(pack_id);
CREATE INDEX psm_tpa_id_idx on package_sponsor_master(tpa_id);
CREATE INDEX psp_service_id_idx on patient_service_prescriptions(service_id);
CREATE INDEX s_insurance_category_id on services(insurance_category_id);
CREATE INDEX dmf_doctor_id_idx on doctor_medicine_favourites(doctor_id);
CREATE INDEX dmf_item_form_id_idx on doctor_medicine_favourites(item_form_id);
CREATE INDEX dmf_item_strength_units_idx on doctor_medicine_favourites(item_strength_units);
CREATE INDEX dmf_medicine_id_idx on doctor_medicine_favourites(medicine_id);
CREATE INDEX dmf_route_of_admin_idx on doctor_medicine_favourites(route_of_admin);
CREATE INDEX dtf_doctor_id_idx on doctor_test_favourites(doctor_id);
CREATE INDEX dtf_test_id_idx on doctor_test_favourites(test_id);
CREATE INDEX dsf_doctor_id_idx on doctor_service_favourites(doctor_id);
CREATE INDEX dsf_service_id_idx on doctor_service_favourites(service_id);
CREATE INDEX dof_doctor_id_idx on doctor_operation_favourites(doctor_id);
CREATE INDEX dcf_cons_doctor_id_idx on doctor_consultation_favourites(doctor_id);
CREATE INDEX domf_doctor_id_idx on doctor_other_medicine_favourites(doctor_id);
CREATE INDEX domf_item_form_id_idx on doctor_other_medicine_favourites(item_form_id);
CREATE INDEX domf_item_strength_units_idx on doctor_other_medicine_favourites(item_strength_units);
CREATE INDEX domf_route_of_admin_idx on doctor_other_medicine_favourites(route_of_admin);
CREATE INDEX dof_item_form_id_idx on doctor_other_favourites(item_form_id);
CREATE INDEX dof_item_strength_units_idx on doctor_other_favourites(item_strength_units);
CREATE INDEX pop_item_form_id_idx on patient_other_prescriptions(item_form_id);
CREATE INDEX pop_operation_id_idx on patient_operation_prescriptions(prescription_id);
CREATE INDEX om_insurance_category_id_idx on operation_master(insurance_category_id);
CREATE INDEX dmd_issued_idx on discharge_medication_details(issued);
CREATE INDEX dmd_medicine_id_idx on discharge_medication_details(medicine_id);
CREATE INDEX pp_status_idx on patient_prescription(status);
-- Build 11.11.4-8121

CREATE INDEX bill_label_master_bill_label_id_idx ON bill_label_master USING btree(bill_label_id);
CREATE INDEX bill_claim_priority_idx ON bill_claim USING btree(priority);


-- Build 11.11.4-8124

-- Build 11.11.4-8126

-- Build 11.11.4-8127

-- Build 11.11.4-8128

-- Build 11.11.4-8131

-- Build 11.11.4-8133

-- Build 11.11.4-8135

-- Build 11.11.5-8137

-- Build 11.11.5-8139

-- Build 11.11.5-8141

-- Build 11.11.5-8143

-- Build 11.11.5-8145

-- Build 11.11.5-8146

-- Build 11.11.5-8148

-- Build 11.11.6-8150

-- Build 11.11.6-8153

-- Build 11.11.6-8156

-- Build 11.11.6-8159

CREATE TABLE codification_message_types
(
	id SERIAL PRIMARY KEY ,
	message_type VARCHAR(95) NOT NULL,
	message_title VARCHAR(200) NOT NULL,
	message_content TEXT NOT NULL
);


CREATE TABLE codification_message_type_role
(
	id SERIAL PRIMARY KEY,
	message_type_id SERIAL REFERENCES CODIFICATION_MESSAGE_TYPES(ID),
	role_id NUMERIC(5,0) REFERENCES U_ROLE(ROLE_ID),
	UNIQUE (message_type_id, role_id)
);

ALTER TABLE codification_message_types ADD CONSTRAINT codification_message_type_unique UNIQUE (message_type);
CREATE TABLE tickets
(
	id SERIAL PRIMARY KEY,
	title VARCHAR(200) NOT NULL,
	body TEXT NOT NULL,
	created_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
	created_at TIMESTAMP NOT NULL,
	status VARCHAR(30) NOT NULL,
	updated_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
	updated_at TIMESTAMP NOT NULL
);
CREATE TABLE codification_ticket_details
(
	id SERIAL PRIMARY KEY,
	ticket_id INTEGER REFERENCES tickets(id) NOT NULL,
	accepted BOOL,
	message_type_id INTEGER REFERENCES codification_message_types(id),
	assigned_to_role INTEGER 
);

CREATE TABLE ticket_recipients
(
	id SERIAL PRIMARY KEY,
	user_id VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
	ticket_id SERIAL REFERENCES TICKETS(ID) NOT NULL,
	read_status INTEGER DEFAULT 0,
	UNIQUE (user_id, ticket_id)
);

CREATE TABLE ticket_comments
(
	id SERIAL PRIMARY KEY,
	ticket_id SERIAL REFERENCES tickets(id),
	comment TEXT NOT NULL,
	comment_by VARCHAR(30) REFERENCES u_user(emp_username) NOT NULL,
	comment_at TIMESTAMP DEFAULT now(),
	sender_read_status SMALLINT DEFAULT 0,
	recipient_read_status SMALLINT DEFAULT 0
);

ALTER TABLE codification_message_types ADD COLUMN message_category  character varying(200);

ALTER TABLE tickets ALTER created_at set default now();
ALTER TABLE tickets ALTER updated_at set default now();
ALTER TABLE tickets ADD COLUMN patient_id character varying(15) NOT NULL;

