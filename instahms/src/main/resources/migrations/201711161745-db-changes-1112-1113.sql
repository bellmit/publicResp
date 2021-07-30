-- liquibase formatted sql
-- changeset adityabhatia02:db-changes-1112-1113 splitStatements:false
-- validCheckSum: ANY

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

ALTER TABLE hosp_grn_seq_prefs ADD COLUMN grn_number_seq_id integer;

CREATE SEQUENCE hosp_grn_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

UPDATE hosp_grn_seq_prefs SET grn_number_seq_id = nextval('hosp_grn_seq_prefs_seq');

ALTER TABLE hosp_grn_seq_prefs ADD CONSTRAINT hosp_grn_seq_prefs_pkey PRIMARY KEY (grn_number_seq_id);


ALTER TABLE hosp_po_seq_prefs ADD COLUMN po_seq_id integer;

CREATE SEQUENCE hosp_po_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

UPDATE hosp_po_seq_prefs SET po_seq_id = nextval('hosp_po_seq_prefs_seq');

ALTER TABLE hosp_po_seq_prefs ADD CONSTRAINT hosp_po_seq_prefs_pkey PRIMARY KEY (po_seq_id);

CREATE TABLE ia_tpa_insco_supported_services (
    ia_tpa_insco_service_id integer NOT NULL,
    ia_tpa_insco_id integer NOT NULL,
    service_name character varying(50),
    applicable character varying(1) DEFAULT 'Y'::character varying,
    created_by character varying(30),
    modified_by character varying(30),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone
);


ALTER TABLE ONLY ia_tpa_insco_supported_services
    ADD CONSTRAINT ia_tpa_insco_service_id PRIMARY KEY (ia_tpa_insco_service_id);
    

ALTER TABLE insurance_aggregator_center_config
    ADD COLUMN facility_id  character varying(20) NOT NULL;


    CREATE TABLE ia_tpa_insco_config (
    ia_tpa_insco_id integer NOT NULL,
    ia_id character varying(25) NOT NULL,
    tpa_id character varying(25),
    insurance_co_id character varying(25),
    status character varying(1) DEFAULT 'A'::character varying,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone,
    created_by character varying(30),
    modified_by character varying(30),
    health_authority_code character varying(25)
);


ALTER TABLE ONLY ia_tpa_insco_config
    ADD CONSTRAINT ia_tpa_insco_id_pkey PRIMARY KEY (ia_tpa_insco_id);


    CREATE TABLE insurance_aggregator_doctor_config (
    ia_doctor_id integer NOT NULL,
    ia_id character varying(25) NOT NULL,
    doctor_id character varying(25) NOT NULL,
    clinician_id character varying(25) NOT NULL,
    ia_doctor_conf character varying,
    status character varying(1) DEFAULT 'A'::character varying,
    modified_at timestamp without time zone,
    created_by character varying(30),
    modified_by character varying(30)
);


ALTER TABLE ONLY insurance_aggregator_doctor_config
    ADD CONSTRAINT ia_doctor_id_pkey PRIMARY KEY (ia_doctor_id);

CREATE TABLE insurance_aggregator_pharmacy_config (
    ia_pharmacy_id integer NOT NULL,
    ia_id character varying(25) NOT NULL,
    pharmacy_id integer NOT NULL,
    facility_id character varying(25) NOT NULL,
    ia_pharmacy_conf character varying,
    status character varying(1) DEFAULT 'A'::character varying,
    modified_at timestamp without time zone,
    created_by character varying(30),
    modified_by character varying(30)
);

ALTER TABLE ONLY insurance_aggregator_pharmacy_config
    ADD CONSTRAINT ia_pharmacy_id_pkey PRIMARY KEY (ia_pharmacy_id);
    
CREATE SEQUENCE ia_tpa_insco_config_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE SEQUENCE insurance_aggregator_doctor_config_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE SEQUENCE insurance_aggregator_pharmacy_config_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;
     
alter table scheduler_appointments add column patient_dob date;
alter table scheduler_appointments add column patient_age int;
alter table scheduler_appointments add column patient_age_units varchar(1);
alter table scheduler_appointments add column patient_gender varchar(1);
alter table scheduler_appointments add column patient_category int;
alter table scheduler_appointments add column patient_address varchar(250);
alter table scheduler_appointments add column patient_area varchar(50);
alter table scheduler_appointments add column patient_state varchar(50);
alter table scheduler_appointments add column patient_city varchar(50);
alter table scheduler_appointments add column patient_country varchar(50);
alter table scheduler_appointments add column patient_nationality varchar(50);
alter table scheduler_appointments add column patient_email_id varchar(250);
alter table scheduler_appointments add column patient_citizen_id text;

ALTER TABLE insurance_remittance_details ADD COLUMN warning integer DEFAULT 0;
ALTER TABLE insurance_remittance_activity_details ADD COLUMN warning integer DEFAULT 0;

insert into payment_mode_master values(-4,'Pine Labs EDC' , 'N', 'N','Y','N','A', (select max(displayorder)+1 from payment_mode_master), 'Pine Labs EDC','N', 'Y','N','Y','N', 'N', 'N');

CREATE SEQUENCE edc_machine_master_seq;

CREATE TABLE edc_machine_master (
  edc_id integer DEFAULT nextval('edc_machine_master_seq') NOT NULL PRIMARY KEY,
  imei character varying(100) NOT NULL UNIQUE,
  display_name character varying(100) NOT NULL UNIQUE,
  merchant_id integer NOT NULL,
  security_token character varying(100) NOT NULL,
  merchant_pos_code character varying(8) NOT NULL,
  center_id integer NOT NULL,
  status char NOT NULL DEFAULT 'A',
  created_at TIMESTAMP DEFAULT NOW()
);

insert into insta_integration (integration_name,url,environment)  values('pineLabs-uploadTxn','https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/UploadBilledTransaction','PROD');

insert into insta_integration (integration_name,url,environment)  values('pineLabs-cancelTxn','https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/CancelTransaction','PROD');

insert into insta_integration (integration_name,url,environment)  values('pineLabs-checkTxnStatus','https://www.plutuscloudserviceuat.in:8201/API/CloudBasedIntegration/V1/GetCloudBasedTxnStatus','PROD');

ALTER TABLE payment_transactions ADD COLUMN response_code integer;

ALTER TABLE payment_transactions ADD COLUMN plutus_txn_id integer;

ALTER TABLE payment_transactions ADD COLUMN mid character varying(100);

ALTER TABLE payment_transactions ADD COLUMN approval_code character varying(100);

ALTER TABLE payment_transactions ADD COLUMN rrn character varying(100);

ALTER TABLE payment_transactions ADD COLUMN invoice character varying(100);

ALTER TABLE payment_transactions ADD COLUMN card_number character varying(100);

ALTER TABLE payment_transactions ADD COLUMN edc_imei character varying(100);

ALTER TABLE payment_transactions ADD COLUMN initiated_by character varying(100);

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) VALUES (nextval('message_config_seq'), 'email_phr_diag_report', 'check_patient_due', 'Y', 'Check for patient due for OP PHR incase of auto triggered message');

CREATE SEQUENCE orderable_item_seq;

CREATE TABLE orderable_item (
  orderable_item_id integer DEFAULT nextval('orderable_item_seq') NOT NULL PRIMARY KEY,
  entity character varying(50) NOT NULL,
  entity_id character varying(50) NOT NULL,
  item_name character varying(600) NOT NULL,
  item_codes character varying(50),
  module_id character varying(50),
  orderable character(1),
  operation_applicable character(1),
  package_applicable character(1),
  is_multi_visit_package character(1),
  service_group_id integer,
  service_sub_group_id integer,
  insurance_category_id integer,
  visit_type character(1),
  direct_billing character(1),
  status character(1)
);

CREATE SEQUENCE mapping_tpa_id_seq;

CREATE TABLE mapping_tpa_id (
  id integer DEFAULT nextval('mapping_tpa_id_seq') NOT NULL,
  orderable_item_id integer,
  tpa_id character varying(50),
  status character(1),
  FOREIGN KEY (orderable_item_id) REFERENCES orderable_item(orderable_item_id)
);

CREATE SEQUENCE mapping_org_id_seq;

CREATE TABLE mapping_org_id(
  id integer DEFAULT nextval('mapping_org_id_seq') NOT NULL,
  orderable_item_id integer,
  status character(1),
  org_id character varying(50),
  FOREIGN KEY (orderable_item_id) REFERENCES orderable_item(orderable_item_id)
);

CREATE SEQUENCE mapping_center_id_seq;

CREATE TABLE mapping_center_id(
  id integer DEFAULT nextval('mapping_center_id_seq') NOT NULL,
  orderable_item_id integer,
  status character(1),
  center_id integer,
  FOREIGN KEY (orderable_item_id) REFERENCES orderable_item(orderable_item_id)
);


-- migrating doctors to orderable_item 
-- doctors depend on center_id
-- Inserting seprate entry for doctor package
CREATE FUNCTION migrate_doctors()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT d.doctor_id as entity_id, 'Doctor' as entity, lower(d.doctor_name) as item_name,
        'mod_basic' as module_id, 'Y' as orderable, d.ot_doctor_flag as operation_applicable, 
        'N' as package_applicable, 'Y' as is_multi_visit_package, d.service_sub_group_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Doctor') as direct_billing, 
        ssg.service_group_id as service_group_id, d.status as status
        FROM doctors d 
        JOIN service_sub_groups ssg using(service_sub_group_id);

    INSERT INTO mapping_center_id (orderable_item_id, center_id, status)
        SELECT DISTINCT oi.orderable_item_id, dcm.center_id, dcm.status 
        FROM doctor_center_master dcm
        JOIN orderable_item oi ON (oi.entity_id = dcm.doctor_id)
        WHERE oi.entity = 'Doctor';
       
    INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, visit_type, service_group_id, status, insurance_category_id) 
        VALUES( 'Doctor', 'Doctor', 'doctor','mod_basic', 'N', 'Y', 'Y', 'Y', -1, '*', -1, 'A', 0);
END;
$$
LANGUAGE plpgsql;

SELECT migrate_doctors();
DROP FUNCTION migrate_doctors();

-- migrating other charges to orderable_item
CREATE FUNCTION migrate_other_charges()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT ccm.charge_name as entity_id, 'Other Charge' as entity, lower(ccm.charge_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'Y' as operation_applicable, 
        'Y' as package_applicable, 'Y' as is_multi_visit_package, ccm.service_sub_group_id, ccm.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Other Charge') as direct_billing, 
        ssg.service_group_id as service_group_id, lower(ccm.othercharge_code) as item_codes, ccm.status as status  
        FROM common_charges_master  ccm
        JOIN service_sub_groups ssg using(service_sub_group_id);
END;
$$
LANGUAGE plpgsql;

SELECT migrate_other_charges();
DROP FUNCTION migrate_other_charges();

-- migrating meal to orderable_item
CREATE FUNCTION migrate_meal()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT dm.diet_id as entity_id, 'Meal' as entity, lower(dm.meal_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'Y' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, dm.service_sub_group_id, dm.insurance_category_id, 'i' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Meal') as direct_billing,
        ssg.service_group_id as service_group_id, dm.status
        FROM diet_master dm
        JOIN service_sub_groups ssg using(service_sub_group_id);
END;
$$
LANGUAGE plpgsql;

SELECT migrate_meal();
DROP FUNCTION migrate_meal();

-- migrating meal to orderable_item
CREATE FUNCTION migrate_equipment()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT em.eq_id as entity_id, 'Equipment' as entity, lower(em.equipment_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'Y' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, em.service_sub_group_id, em.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Equipment') as direct_billing,
        ssg.service_group_id as service_group_id, lower(em.equipment_code) as item_codes, em.status as status  
        FROM equipment_master em
        JOIN service_sub_groups ssg using(service_sub_group_id);
END;
$$
LANGUAGE plpgsql;

SELECT migrate_equipment();
DROP FUNCTION migrate_equipment();

-- migrating bed to orderable items
-- if need to search for only icu beds seach using entity
CREATE FUNCTION migrate_bed()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT bt.bed_type_name as entity_id, 'Bed' as entity, lower(bt.bed_type_name) as item_name, 
        'mod_adt' as module_id, 'I' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, cc.service_sub_group_id, bt.insurance_category_id, 'i' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Bed') as direct_billing,
        ssg.service_group_id as service_group_id, bt.status as status
        FROM bed_types bt
        JOIN chargehead_constants cc on ( chargehead_id = 'BBED' )
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE bt.is_icu = 'N';

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT bt.bed_type_name as entity_id, 'ICU' as entity, lower(bt.bed_type_name) as item_name, 
        'mod_adt' as module_id, 'I' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, cc.service_sub_group_id, bt.insurance_category_id, 'i' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'ICU') as direct_billing,
        ssg.service_group_id as service_group_id, bt.status as status  
        FROM bed_types bt
        JOIN chargehead_constants cc on ( chargehead_id = 'BICU' )
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE bt.is_icu = 'Y';
END;
$$
LANGUAGE plpgsql;

SELECT migrate_bed();
DROP FUNCTION migrate_bed();


-- migrating direct_charge to orderable_item
CREATE FUNCTION migrate_direct_charge()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT cc.chargehead_id as entity_id, 'Direct Charge' as entity, lower(cc.chargehead_name) as item_name, 
        'mod_basic' as module_id, 'N' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, cc.service_sub_group_id, cc.insurance_category_id, 'i' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Direct Charge') as direct_billing,
        ssg.service_group_id as service_group_id, 'A' as status 
        FROM chargehead_constants cc
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE cc.associated_module = 'mod_billing' AND cc.ip_applicable = 'Y' AND cc.op_applicable = 'N';

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT cc.chargehead_id as entity_id, 'Direct Charge' as entity, lower(cc.chargehead_name) as item_name, 
        'mod_basic' as module_id, 'N' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, cc.service_sub_group_id, cc.insurance_category_id, 'o' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Direct Charge') as direct_billing,
        ssg.service_group_id as service_group_id, 'A' as status    
        FROM chargehead_constants cc
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE cc.associated_module = 'mod_billing' AND cc.op_applicable = 'Y' AND cc.ip_applicable = 'N';

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT cc.chargehead_id as entity_id, 'Direct Charge' as entity, lower(cc.chargehead_name) as item_name, 
        'mod_basic' as module_id, 'N' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, cc.service_sub_group_id, cc.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Direct Charge') as direct_billing,
        ssg.service_group_id as service_group_id, 'A' as status    
        FROM chargehead_constants cc
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE cc.associated_module = 'mod_billing' AND cc.op_applicable = 'Y' AND cc.ip_applicable = 'Y';

END;
$$
LANGUAGE plpgsql;

SELECT migrate_direct_charge();
DROP FUNCTION migrate_direct_charge();

-- maigrating laboratory to orderable table
CREATE FUNCTION migrate_laboratory()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT d.test_id as entity_id, 'Laboratory' as entity, lower(d.test_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'Y' as is_multi_visit_package, d.service_sub_group_id, d.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Laboratory') as direct_billing,
        ssg.service_group_id as service_group_id, lower(d.diag_code) as item_codes, d.status as status 
        FROM diagnostics d
        JOIN diagnostics_departments ddept USING (ddept_id)
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE ddept.category='DEP_LAB';

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT d.test_id as entity_id, 'Radiology' as entity, lower(d.test_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'Y' as is_multi_visit_package, d.service_sub_group_id, d.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Radiology') as direct_billing,
        ssg.service_group_id as service_group_id, lower(d.diag_code) as item_codes, d.status as status 
        FROM diagnostics d
        JOIN diagnostics_departments ddept USING (ddept_id)
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE ddept.category='DEP_RAD';

    INSERT INTO mapping_org_id ( orderable_item_id, org_id, status )
        SELECT DISTINCT oi.orderable_item_id, tod.org_id as org_id,
        CASE WHEN (tod.applicable = 't') THEN 'A' ELSE 'I' END as status
        FROM orderable_item oi
        JOIN test_org_details tod ON (tod.test_id = oi.entity_id)
        WHERE (oi.entity = 'Laboratory' OR oi.entity = 'Radiology');
END;
$$
LANGUAGE plpgsql;

SELECT migrate_laboratory();
DROP FUNCTION migrate_laboratory();

-- migrating services to orderable table
CREATE FUNCTION migrate_services()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT s.service_id as entity_id, 'Service' as entity, lower(s.service_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'Y' as is_multi_visit_package, s.service_sub_group_id, s.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Service') as direct_billing,
        ssg.service_group_id as service_group_id, lower(s.service_code) as item_codes, s.status as status 
        FROM services s
        JOIN service_sub_groups ssg using(service_sub_group_id);

    INSERT INTO mapping_org_id ( orderable_item_id, org_id, status )
        SELECT DISTINCT oi.orderable_item_id, sod.org_id as org_id,
        CASE WHEN (sod.applicable = 't') THEN 'A' ELSE 'I' END as status
        FROM service_org_details sod 
        JOIN orderable_item oi ON (sod.service_id = oi.entity_id)
        WHERE oi.entity = 'Service';
END;
$$
LANGUAGE plpgsql;

SELECT migrate_services();
DROP FUNCTION migrate_services();

-- maigrating operation to orderable table
-- making visit type as * as need opearation item for both Op and Ip visits.
-- Opeartion items have linkage to opeartion_appliable_for field in generic preferences.
CREATE FUNCTION migrate_operation()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT om.op_id as entity_id, 'Operation' as entity, lower(om.operation_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'N' as package_applicable, 'N' as is_multi_visit_package, om.service_sub_group_id, om.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Operation') as direct_billing,
        ssg.service_group_id as service_group_id, lower(om.operation_code) as item_codes, om.status as status 
        FROM operation_master om
        JOIN service_sub_groups ssg using(service_sub_group_id);

    INSERT INTO mapping_org_id ( orderable_item_id, org_id, status )
        SELECT DISTINCT oi.orderable_item_id, ood.org_id as org_id,
        CASE WHEN (ood.applicable = 't') THEN 'A' ELSE 'I' END as status
        FROM operation_org_details ood 
        JOIN orderable_item oi ON (ood.operation_id = oi.entity_id)
        WHERE oi.entity = 'Operation';
END;
$$
LANGUAGE plpgsql;

SELECT migrate_operation();
DROP FUNCTION migrate_operation();

-- maigrating package to orderable table
-- diagPackage here is combination of Lab and Rad Package
-- Previously Lab or Rad Package entry where seprate, while going lab screen user can select lab package
-- while going to rad screen can select rad package
-- and lab package and rad package are same, just type are different. So combining under Diag Package

-- Exisiting Diag Package entry in OrderMasterDao under rate dependent items depricating it
-- user can search for both Laboratoty Items and Radiology items and prevoiusly DiagPackage was not actually 
-- an package but items.
CREATE FUNCTION migrate_packages()
  RETURNS void as $$
BEGIN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT pm.package_id::text as entity_id, 'Package' as entity, lower(pm.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, pm.service_sub_group_id, pm.insurance_category_id, 'i' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(pm.package_code) as item_codes,
        CASE WHEN (pm.package_active = 'A' AND  pm.approval_status='A' ) THEN 'A' ELSE 'I' END as status
        FROM pack_master pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE package_type = 'i' AND multi_visit_package = false;

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT pm.package_id::text as entity_id, 'Package' as entity, lower(pm.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, pm.service_sub_group_id, pm.insurance_category_id, 'o' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(pm.package_code) as item_codes,
        CASE WHEN (pm.package_active = 'A' AND  pm.approval_status='A' ) THEN 'A' ELSE 'I' END as status
        FROM pack_master pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE package_type = 'o' AND multi_visit_package = false;
    
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT pm.package_id::text as entity_id, 'MultiVisitPackage' as entity, lower(pm.package_name) as item_name, 
        'mod_adv_packages' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'N' as package_applicable, 'N' as is_multi_visit_package, pm.service_sub_group_id, pm.insurance_category_id, 'o' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'MultiVisitPackage') as direct_billing,
        ssg.service_group_id as service_group_id, lower(pm.package_code) as item_codes,
        CASE WHEN (pm.package_active = 'A' AND  pm.approval_status='A' ) THEN 'A' ELSE 'I' END as status
        FROM pack_master pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE package_type = 'o' AND multi_visit_package = true;

    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT pm.package_id::text as entity_id, 'DiagPackage' as entity, lower(pm.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, pm.service_sub_group_id, pm.insurance_category_id, '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'DiagPackage') as direct_billing,
        ssg.service_group_id as service_group_id, lower(pm.package_code) as item_codes,
        CASE WHEN (pm.package_active = 'A' AND  pm.approval_status='A' ) THEN 'A' ELSE 'I' END as status
        FROM pack_master pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE package_type = 'd' AND multi_visit_package = false;

    
    INSERT INTO mapping_org_id ( orderable_item_id, org_id, status )
        SELECT DISTINCT oi.orderable_item_id, pod.org_id as org_id,
        CASE WHEN (pod.applicable = 't') THEN 'A' ELSE 'I' END as status
        FROM pack_org_details pod
        JOIN orderable_item oi ON (pod.package_id::text = oi.entity_id)
        WHERE oi.entity = 'Package' OR oi.entity = 'MultiVisitPackage' OR oi.entity = 'DiagPackage';

    INSERT INTO mapping_center_id ( orderable_item_id, center_id, status )
        SELECT DISTINCT oi.orderable_item_id, pcm.center_id as center_id, pcm.status as status
        FROM package_center_master pcm
        JOIN orderable_item oi ON (pcm.pack_id::text = oi.entity_id)
        WHERE oi.entity = 'Package' OR oi.entity = 'MultiVisitPackage' OR oi.entity = 'DiagPackage';

    INSERT INTO mapping_tpa_id ( orderable_item_id, tpa_id, status )
        SELECT DISTINCT oi.orderable_item_id, psm.tpa_id as tpa_id, psm.status as status
        FROM package_sponsor_master psm
        JOIN orderable_item oi ON (psm.pack_id::text = oi.entity_id)
        WHERE oi.entity = 'Package' OR oi.entity = 'MultiVisitPackage' OR oi.entity = 'DiagPackage';
END;
$$
LANGUAGE plpgsql;

SELECT migrate_packages();
DROP FUNCTION migrate_packages();

CREATE INDEX item_name_orderable_item_index ON orderable_item (item_name);
CREATE INDEX item_code_orderable_item_index ON orderable_item (item_codes);
CREATE INDEX orderable_item_id_mapping_center_id_idx ON mapping_center_id (orderable_item_id);
CREATE INDEX orderable_item_id_mapping_tpa_id_idx ON mapping_tpa_id (orderable_item_id);
CREATE INDEX orderable_item_id_mapping_org_id_idx ON mapping_org_id (orderable_item_id);
CREATE INDEX entity_id_orderable_item_index ON orderable_item (entity_id);

alter table patient_test_prescriptions add column priority character varying(1) NOT NULL default 'N';
comment on column patient_test_prescriptions.priority is 'possible values are N → Normal, P → PRN/SOS, S → Stat, U → Urgent';

alter table patient_service_prescriptions add column priority character varying(1) NOT NULL default 'N';
comment on column patient_service_prescriptions.priority is 'possible values are N → Normal, P → PRN/SOS, S → Stat, U → Urgent';

ALTER TABLE scheduler_appointments ADD COLUMN cancel_type character varying(50);
UPDATE scheduler_appointments SET cancel_type ='Other' WHERE appointment_status ='Cancel';

ALTER TABLE antenatal ALTER COLUMN gestation_age TYPE character varying(100);

ALTER TABLE system_generated_sections ADD COLUMN edd_expression_value INTEGER default '280';

ALTER TABLE antenatal ADD COLUMN lmp date;

ALTER TABLE antenatal ADD COLUMN edd date;

ALTER TABLE antenatal ADD COLUMN final_edd date;

alter table diagnostics add column isconfidential boolean default false;

ALTER TABLE scheduler_appointments add column vip_status char(1) not null default 'N';

alter table mrd_codes_doctor_master alter COLUMN code_type type character varying(50);
alter table mrd_codes_doctor_master alter COLUMN code type character varying(100);

-- Build 11.13.0-8249

-- Build 11.13.0-8263

-- Build 11.13.0-8267

-- Build 11.13.0-8269


create table patient_section_images(
  image_id integer primary key,
  file_content bytea,
  content_type character varying
);

create sequence patient_section_images_seq start with 1;

create sequence patient_section_fields_seq start with 1;
create table patient_section_fields as 
  select nextval('patient_section_fields_seq')::integer as field_detail_id, 
    section_detail_id, field_id, user_name, field_remarks, date, date_time, mod_time 
  from (
    select section_detail_id, field_id, user_name, option_remarks as field_remarks, 
      case when option_id=-3 then date_time::date else null end as date, date_time, mod_time, rank() over (partition by section_detail_id, field_id order by option_id) as rank
    from patient_section_values
  ) as foo where rank = 1 order by section_detail_id;

alter table patient_section_fields add primary key (field_detail_id);
alter table patient_section_fields add column image_id integer;
create index patient_section_fields_section_detail_id_idx on patient_section_fields(section_detail_id);
create index patient_section_fields_field_id_idx on patient_section_fields(field_id);


create sequence patient_section_options_seq start with 1;
create table patient_section_options as (select nextval('patient_section_options_seq')::integer as option_detail_id, field_detail_id, option_id, option_remarks, psv.mod_time, psv.user_name, available from patient_section_values psv join patient_section_fields psf ON (psf.section_detail_id=psv.section_detail_id and psf.field_id=psv.field_id) where option_id > -2 order by psv.section_detail_id, psv.field_id, option_id);

alter table patient_section_options add primary key (option_detail_id);
create index patient_section_options_field_detail_id_idx on patient_section_options(field_detail_id);

insert into patient_section_fields(field_detail_id, section_detail_id, field_id, user_name, mod_time) 
  select nextval('patient_section_fields_seq'), section_detail_id, field_id, user_name, mod_time 
  from patient_section_details psd
    join section_field_desc sfd ON (psd.section_id=sfd.section_id and sfd.field_type='image'); 

alter table patient_section_image_details add column field_detail_id integer;
update patient_section_image_details img set field_detail_id=psf.field_detail_id 
  from patient_section_fields psf where img.section_detail_id=psf.section_detail_id and img.field_id=psf.field_id;
create index patient_section_image_details_field_detail_id_idx on patient_section_image_details(field_detail_id);

create sequence patient_section_image_details_seq start with 1;
alter table patient_section_image_details add column marker_detail_id integer;
update patient_section_image_details set marker_detail_id = nextval('patient_section_image_details_seq');

alter table patient_section_image_details rename column section_detail_id to obsolete_section_detail_id;
alter table patient_section_image_details rename column field_id to obsolete_field_id;


update print_templates set print_template_content=regexp_replace(print_template_content, 'PhysicianFieldsImage.do\?_method=viewImage&amp;field_id=\$\{field\[0\].field_id\}', 'PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id}', 'g')
where template_type in ('Assessment', 'Triage', 'VisitSummaryRecord', 'S', 'ConsultationDetails', 'OTDetails') AND built_in=false;

update prescription_print_template set prescription_template_content=regexp_replace(prescription_template_content, 'PhysicianFieldsImage.do\?_method=viewImage&amp;field_id=\$\{field\[0\].field_id\}', 'PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id}', 'g');

update common_print_templates set template_content=regexp_replace(template_content, 'PhysicianFieldsImage.do\?_method=viewImage&amp;field_id=\$\{field\[0\].field_id\}', 'PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id}', 'g')
WHERE template_type in ('InstaGenericForm', 'OtRecord');

update print_templates set print_template_content=regexp_replace(print_template_content, E'value.field_type == \'image\'', 
E'value.field_type == \'image\' \&\& value.marker_id?has_content', 'g')
where template_type in ('Assessment', 'Triage', 'VisitSummaryRecord', 'S', 'ConsultationDetails', 'OTDetails') AND built_in=false;

update prescription_print_template set prescription_template_content=regexp_replace(prescription_template_content, 
E'value.field_type == \'image\'', 
E'value.field_type == \'image\' \&\& value.marker_id?has_content', 'g');

update common_print_templates set template_content=regexp_replace(template_content, E'value.field_type == \'image\'', 
E'value.field_type == \'image\' \&\& value.marker_id?has_content', 'g')
WHERE template_type in ('InstaGenericForm', 'OtRecord');

CREATE SEQUENCE practitioner_types_seq;

CREATE TABLE practitioner_types (
  practitioner_id integer DEFAULT nextval('practitioner_types_seq') NOT NULL PRIMARY KEY,
  practitioner_name character varying(100) NOT NULL,
  status character(1),
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE SEQUENCE practitioner_types_mapping_seq;

CREATE TABLE practitioner_types_mapping (
  mapping_id integer DEFAULT nextval('practitioner_types_mapping_seq') NOT NULL PRIMARY KEY,
  practitioner_id integer NOT NULL,
  center_id integer NOT NULL,
  consultation_type_id integer,
  visit_type character(1),
  created_at TIMESTAMP DEFAULT NOW()
);

ALTER TABLE doctors ADD COLUMN practitioner_id Integer;
INSERT INTO practitioner_types(practitioner_name, status) values('G','A');
INSERT INTO practitioner_types(practitioner_name, status) values('S','A');
UPDATE doctors d set practitioner_id = pt.practitioner_id FROM practitioner_types pt WHERE pt.practitioner_name = d.practition_type;
UPDATE practitioner_types set practitioner_name = 'General Practitioner' WHERE practitioner_name = 'G';
UPDATE practitioner_types set practitioner_name = 'Specialist Practitioner' WHERE practitioner_name = 'S';
INSERT INTO practitioner_types(practitioner_name, status) values('Consultant','A');

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_gp_first_consultation, 'M' 
from hospital_center_master hcm Join health_authority_preferences hap on (hap.health_authority = hcm.health_authority) 
JOIN practitioner_types pt on (pt.practitioner_name = 'General Practitioner' )
where  hap.default_gp_first_consultation is not null;

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_gp_first_consultation, 'R' 
from hospital_center_master hcm Join health_authority_preferences hap on (hap.health_authority = hcm.health_authority) 
JOIN practitioner_types pt on (pt.practitioner_name = 'General Practitioner' )
where  hap.default_gp_first_consultation is not null;

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_gp_revisit_consultation, 'F' 
from hospital_center_master hcm Join health_authority_preferences hap on (hap.health_authority = hcm.health_authority) 
JOIN practitioner_types pt on (pt.practitioner_name = 'General Practitioner' )
where  hap.default_gp_revisit_consultation is not null;

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_sp_first_consultation, 'M' 
from hospital_center_master hcm JOIN health_authority_preferences hap on (hap.health_authority = hcm.health_authority) 
JOIN practitioner_types pt on (pt.practitioner_name = 'Specialist Practitioner' )
where  hap.default_sp_first_consultation is not null;

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_sp_first_consultation, 'R' 
from hospital_center_master hcm JOIN health_authority_preferences hap on (hap.health_authority = hcm.health_authority) 
JOIN practitioner_types pt on (pt.practitioner_name = 'Specialist Practitioner' )
where  hap.default_sp_first_consultation is not null;

INSERT INTO practitioner_types_mapping(practitioner_id, center_id, consultation_type_id, visit_type) 
select pt.practitioner_id, hcm.center_id, hap.default_sp_revisit_consultation, 'F' 
from hospital_center_master hcm JOIN health_authority_preferences hap on (hap.health_authority = hcm.health_authority)
JOIN practitioner_types pt on (pt.practitioner_name = 'Specialist Practitioner' )

where  hap.default_sp_revisit_consultation is not null;

DELETE FROM patient_consultation_field_values where field_id = '-1';

INSERT INTO patient_consultation_field_values (value_id,doc_id,field_id,field_value)  
(
   SELECT DISTINCT nextval('patient_consultation_field_values_seq'), foo.doc_id, -1, string_agg(foo.field_name_values, E'\n')
    FROM 
      (
        SELECT dhtf.field_name || ':-' || pc.field_value as field_name_values, pc.doc_id 
        FROM patient_consultation_field_values pc
        JOIN doc_hvf_template_fields dhtf on (dhtf.field_id = pc.field_id)
      ) as foo GROUP BY doc_id 
    HAVING NOT EXISTS (SELECT 1 FROM patient_consultation_field_values WHERE doc_id = foo.doc_id AND field_id = -1)
);

CREATE INDEX idx_bill_no_store_sales_main on store_sales_main(bill_no);

CREATE INDEX idx_manf_name_store_item_details on store_item_details(manf_name);

-- Build 11.13.0-8271

insert into u_role (role_id,role_name,role_status,portal_id,mod_user,mod_date) values((SELECT ( COALESCE(MAX(role_id),0) + 1 ) FROM u_role),'AddonsAdmin','A','N','InstaAdmin',now());
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'reg_general','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'mas_states','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'mas_cities','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'mas_salutation','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'mas_patient_cat','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'mas_doctors','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'scheduler_message_send','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'edit_visit_details','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'doc_scheduler','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'cat_resource_scheduler','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'res_availability','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'clinical_data_lab_results','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'patient_details_search','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'visit_details_search','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'lab_schedules_list','A';
INSERT INTO url_action_rights select (select role_id FROM u_role where role_name='AddonsAdmin'), 'visit_emr_screen','A';

-- Adding Addons as Source of Appointment in Master
INSERT INTO appointment_source_master(appointment_source_id,appointment_source_name,status) 
SELECT nextval('appointment_source_master_seq'::regclass), 'widget', 'A';

INSERT INTO appointment_source_master(appointment_source_id,appointment_source_name,status) 
SELECT nextval('appointment_source_master_seq'::regclass), 'kiosk', 'A';

ALTER TABLE sample_collection ADD COLUMN sample_conduction_status character(1) DEFAULT 'N';

UPDATE sample_collection sc SET sample_conduction_status = 'P' 
WHERE EXISTS (SELECT 1 FROM tests_prescribed tp WHERE tp.sample_collection_id = sc.sample_collection_id AND tp.conducted NOT IN ('N', 'NRN', 'X') LIMIT 1);

create index sample_collection_is_conduction_started_idx ON sample_collection (sample_conduction_status);
create index sample_collection_receive_idx ON sample_collection (sample_receive_status);
create index sample_collection_transfer_idx ON sample_collection (sample_transfer_status);

ALTER TABLE patient_medicine_prescriptions ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE patient_other_medicine_prescriptions ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE patient_other_prescriptions ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE pbm_medicine_prescriptions ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE doctor_medicine_favourites ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE doctor_other_medicine_favourites ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE doctor_other_favourites ALTER COLUMN frequency TYPE character varying(150);

ALTER TABLE store_sales_details  ALTER COLUMN frequency TYPE character varying(150);
-- Build 11.13.0-8276


CREATE INDEX idx_med_category_id_store_item_details ON store_item_details(med_category_id);

CREATE INDEX idx_medicine_id_ha_item_code_type ON ha_item_code_type(medicine_id);

CREATE INDEX idx_code_type_store_item_codes ON store_item_codes(code_type);

CREATE INDEX idx_medicine_id_store_item_codes ON store_item_codes(medicine_id);

-- Build 11.13.0-8278

-- Build 11.13.0-8280

-- Build 11.13.0-8283


CREATE SEQUENCE hosp_lab_number_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE TABLE hosp_lab_number_seq_prefs
(
lab_number_seq_id integer NOT NULL default nextval('hosp_lab_number_seq_prefs_seq'),
  priority integer NOT NULL,
  pattern_id character varying NOT NULL,
  constraint hosp_lab_number_seq_prefs_pkey primary key(lab_number_seq_id)
);

INSERT INTO hosp_lab_number_seq_prefs(priority, pattern_id) VALUES (1000, 'LAB_NUMBER_DEFAULT');

INSERT INTO hosp_id_patterns(
pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type,transaction_type)
VALUES ('LAB_NUMBER_DEFAULT', 'LAB', '', '99000000', 'labno_seq', '', '', 'Txn','LAB');


CREATE SEQUENCE hosp_radiology_number_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE TABLE hosp_radiology_number_seq_prefs
(
radiology_number_seq_id integer NOT NULL default nextval('hosp_radiology_number_seq_prefs_seq'),
  priority integer NOT NULL,
  pattern_id character varying NOT NULL,
  constraint hosp_radiology_number_seq_prefs_pkey primary key(radiology_number_seq_id)
);

INSERT INTO hosp_radiology_number_seq_prefs(priority, pattern_id) VALUES (1000, 'RADIOLOGY_NUMBER_DEFAULT');

INSERT INTO hosp_id_patterns(
pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type,transaction_type)
VALUES ('RADIOLOGY_NUMBER_DEFAULT', 'RAD', '', '99000000', 'radno_seq', '', '', 'Txn','RAD');

-- Build 11.13.0-8285

-- Build 11.13.0-8287

-- Build 11.13.0-8289

-- Build 11.13.0-8291

-- Build 11.13.0-8294

-- Build 11.13.0-8306

ALTER TABLE  bill_receipts ADD COLUMN edc_imei character varying(100);
ALTER TABLE bill_account_heads ADD COLUMN status character varying(1) DEFAULT 'A';
ALTER TABLE payments_details ALTER COLUMN category TYPE character varying(250);

-- Build 11.13.0-8309

-- Build 11.13.0-8311

-- Build 11.13.0-8314

-- Build 11.13.0-8316

-- Build 11.13.0-8318

-- Build 11.13.0-8320

-- Build 11.13.0-8323

-- Build 11.13.0-8328

create table prescription_print_templates_temp as ( select template_name || '_old' as template_name, prescription_template_content, 
  template_mode, user_name, reason from prescription_print_template where template_mode='H');

update prescription_print_template set prescription_template_content = regexp_replace(
  prescription_template_content, E'value.date_time\\?string\\(''dd-MM-yyyy''\\)', 'value.date?string(''dd-MM-yyyy'')') where template_mode='H';

insert into doc_print_configuration(document_type,center_id,printer_settings,page_settings,template_name)
  ( select dpc.document_type || '_old',dpc.center_id,dpc.printer_settings,dpc.page_settings,dpc.template_name
  from doc_print_configuration dpc, prescription_print_template ppt
  where ppt.template_mode='H' and dpc.document_type='prescription_' || ppt.template_name );

insert into prescription_print_template(template_name, prescription_template_content, template_mode, user_name, reason) 
  ( select template_name, prescription_template_content, template_mode, user_name, reason from prescription_print_templates_temp);

drop table prescription_print_templates_temp;

create table print_templates_temp as (select * from print_templates where template_type in 
  ('D', 'S', 'Triage', 'Assessment', 'ConsultationDetails', 'OTDetails') and built_in=false);

update print_templates set print_template_content = regexp_replace(print_template_content, 
  E'value.date_time\\?string\\(''dd-MM-yyyy''\\)', 'value.date?string(''dd-MM-yyyy'')') 
where template_type in ('D', 'S', 'Triage', 'Assessment', 'ConsultationDetails', 'OTDetails', 'VisitSummaryRecord') and built_in=false;


create table common_print_templates_temp as ( select template_name || '_old' as template_name, template_content, template_mode, user_name, 
  reason from common_print_templates where template_mode in ('OtRecord', 'InstaGenericForm'));

update common_print_templates set template_content = regexp_replace(
  template_content, 
  E'value.date_time\\?string\\(''dd-MM-yyyy''\\)', 'value.date?string(''dd-MM-yyyy'')') where template_mode in ('OtRecord', 'InstaGenericForm');

insert into common_print_templates(template_name, template_content, template_mode, user_name, reason) 
  ( select template_name, template_content, template_mode, user_name, reason from common_print_templates_temp);

drop table common_print_templates_temp;

-- Build 11.13.0-8331

alter table appointment_source_master add column patient_day_appt_limit integer default -1;
update appointment_source_master set patient_day_appt_limit=3 where appointment_source_name='widget';


-- Build 11.13.0-8335

-- Build 11.13.0-8335

-- Build 11.13.0-0

-- Build 11.13.0-0

ALTER TABLE discharge_medication_details ALTER COLUMN frequency TYPE character varying(150);




-- Build 11.13.0-8337

-- Build 11.13.0-8339

INSERT INTO bill_charge_tax (charge_id, tax_sub_group_id, tax_rate, tax_amount, original_tax_amt)
  SELECT charge_id, tax_sub_group_id, COALESCE(tax_rate,0) as tax_rate, COALESCE(tax_amount,0) as tax_amount , COALESCE(tax_amount,0) as original_tax_amt
    FROM 
    (SELECT ssm.charge_id, sstd.item_subgroup_id as tax_sub_group_id, max(sstd.tax_rate) as tax_rate, SUM(sstd.tax_amt) as tax_amount
      FROM store_sales_main ssm
      JOIN bill b ON (b.bill_no = ssm.bill_no)
      JOIN (SELECT sssm.sale_id FROM store_sales_main sssm
          JOIN bill bb ON (bb.bill_no = sssm.bill_no) 
          JOIN bill_charge bcc ON (bcc.charge_id = sssm.charge_id)
          LEFT JOIN bill_charge_tax bct ON (bct.charge_id = bcc.charge_id) 
          where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2018-01-01' AND bb.open_date <= now() group by sssm.sale_id having count(bct.tax_sub_group_id) = 0 order by sssm.sale_id) as tc 
      ON (tc.sale_id = ssm.sale_id)
      JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
      JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id)
    WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2018-01-01' AND b.open_date <= now()
    group by sstd.item_subgroup_id,ssm.sale_id,ssm.charge_id order by charge_id) as foo;
    
INSERT INTO bill_charge_claim_tax (charge_id, claim_id, tax_sub_group_id, tax_rate, sponsor_tax_amount, charge_tax_id)
  SELECT charge_id, claim_id, tax_sub_group_id, COALESCE(tax_rate,0) as tax_rate, COALESCE(tax_amount,0) as tax_amount, charge_tax_id 
    FROM 
    (SELECT ssm.charge_id, sctd.claim_id, sctd.item_subgroup_id as tax_sub_group_id, max(sctd.tax_rate) as tax_rate, SUM(sctd.tax_amt) as tax_amount, bcht.charge_tax_id
      FROM store_sales_main ssm
      JOIN bill b ON (b.bill_no = ssm.bill_no)
      JOIN (SELECT sssm.sale_id FROM store_sales_main sssm
          JOIN bill bb ON (bb.bill_no = sssm.bill_no) 
          JOIN bill_charge bct ON (bct.charge_id = sssm.charge_id)
          JOIN bill_charge_claim bcc ON (bcc.charge_id = sssm.charge_id)
          LEFT JOIN bill_charge_claim_tax bcct ON (bcct.charge_id = bcc.charge_id AND bcct.claim_id = bcc.claim_id) 
          where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2018-01-01' AND bb.open_date <= now() AND sssm.type='S' group by sssm.sale_id having count(bcct.tax_sub_group_id) = 0 order by sssm.sale_id) as tc 
      ON (ssm.sale_id = tc.sale_id)
      JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
      JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id)
      JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id)
      JOIN bill_charge_tax bcht ON (bcht.charge_id = ssm.charge_id AND bcht.tax_sub_group_id = sctd.item_subgroup_id)
    WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2018-01-01' AND b.open_date <= now() AND ssm.type = 'S' AND b.is_tpa = true
    group by sctd.item_subgroup_id,sctd.claim_id,ssm.sale_id,ssm.charge_id,bcht.charge_tax_id order by charge_id) as foo;

-- Build 11.13.0-8340

CREATE SEQUENCE contact_details_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

create table contact_details (
contact_id integer DEFAULT nextval('contact_details_seq'::regclass) PRIMARY KEY NOT NULL,
patient_name character varying(30),
patient_gender character varying(30),
patient_address character varying(250),
patient_city character varying(50),
patient_state character varying(50),
patient_phone character varying(16) NOT NULL,
salutation character varying(50),
middle_name character varying(50),
last_name character varying(50),
dateofbirth date,
country character varying(50),
email_id character varying(200),
nationality_id character varying(50),
government_identifier character varying(50),
mr_no character varying(15)
);

ALTER TABLE scheduler_appointments add column contact_id integer;

create table patient_section_text_fields_audit_log as (select  section_detail_id, field_id, 
  user_name,  mod_time, operation, case when field_name = 'option_remarks' then 'field_remarks' else field_name end as field_name, old_value, new_value from patient_section_values_audit_log where option_id=-2 
  and field_name in ('option_remarks', 'field_id', 'section_detail_id')); 

create table patient_section_date_fields_audit_log as (select  section_detail_id, field_id, user_name, 
  mod_time, operation, case when field_name = 'date_time' then 'date' else field_name end as field_name, old_value, new_value 
  from patient_section_values_audit_log where option_id=-3 and field_name in ('date_time', 'field_id', 'section_detail_id'));

create table patient_section_datetime_fields_audit_log as (select section_detail_id, field_id, user_name, mod_time,
 operation, field_name, old_value, new_value from patient_section_values_audit_log where option_id=-4 and field_name in ('date_time', 'field_id', 'section_detail_id'));


create table patient_section_dropcheck_fields_audit_log as (select distinct section_detail_id, field_id, user_name, mod_time,
  operation, field_name, old_value, new_value from patient_section_values_audit_log where option_id > 0 and 
  field_name in ('field_id', 'section_detail_id'));

create table patient_section_fields_audit_log as (
  select nextval('audit_logid_sequence') as log_id, field_detail_id, section_detail_id, field_id, field_name, new_value, old_value,
    foo.user_name, foo.mod_time, operation, action from (
    select *, case operation when 'INSERT' then 1 when 'UPDATE' then 2 else 3 end as action from patient_section_text_fields_audit_log
    union all
    select *, case operation when 'INSERT' then 1 when 'UPDATE' then 2 else 3 end as action from patient_section_date_fields_audit_log
    union all
    select *, case operation when 'INSERT' then 1 when 'UPDATE' then 2 else 3 end as action from patient_section_datetime_fields_audit_log
    union all
    select *, case operation when 'INSERT' then 1 when 'UPDATE' then 2 else 3 end as action from patient_section_dropcheck_fields_audit_log
  ) as foo
  join patient_section_fields psf using (section_detail_id, field_id)
  order by section_detail_id, action 
);

alter table patient_section_fields_audit_log drop column action;


create table patient_section_options_audit_log_temp as (select * from patient_section_values_audit_log);
delete from patient_section_options_audit_log_temp where option_id in (-2, -3, -4);
create table patient_section_options_audit_log as (select nextval('audit_logid_sequence') as log_id, psf.field_detail_id, option_detail_id, option_id, old_value, new_value, foo.user_name, foo.mod_time, foo.operation, field_name 
  from patient_section_options_audit_log_temp as foo
  join patient_section_fields psf using (section_detail_id, field_id)
  join patient_section_options pso using (field_detail_id, option_id));


alter table patient_section_fields_audit_log alter column log_id set default nextval('audit_logid_sequence');
alter table patient_section_options_audit_log alter column log_id set default nextval('audit_logid_sequence');
alter table patient_section_fields_audit_log add constraint log_id_pkey primary key  (log_id);
alter table patient_section_fields_audit_log alter column mod_time set default now();
alter table patient_section_options_audit_log alter column mod_time set default now();

-- dropping the temp tables created for migration.
drop table patient_section_text_fields_audit_log cascade;
drop table patient_section_date_fields_audit_log cascade;
drop table patient_section_datetime_fields_audit_log cascade;
drop table patient_section_dropcheck_fields_audit_log cascade;
drop table patient_section_options_audit_log_temp cascade;

UPDATE system_generated_sections SET display_name='Management' Where section_id=-7;

CREATE SEQUENCE patient_appointment_plan_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE SEQUENCE patient_appointment_plan_details_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE TABLE patient_appointment_plan (
    plan_id  integer DEFAULT nextval('patient_appointment_plan_seq'::regclass) NOT NULL,
    plan_name character varying(100),
    mr_no character varying(100),
    presc_doc_id character varying(100),
    plan_appointment_status character varying(10),
    plan_status char(1) DEFAULT 'A',
    creation_time timestamp without time zone DEFAULT now() NOT NULL,
    mod_time timestamp without time zone,
    created_by character varying(30) NOT NULL,
    modified_by character varying(30),
    center_id integer DEFAULT 0 NOT NULL
);

ALTER TABLE ONLY patient_appointment_plan ADD CONSTRAINT plan_pkey PRIMARY KEY (plan_id);

ALTER TABLE ONLY patient_appointment_plan ADD CONSTRAINT presc_doc_id_fkey FOREIGN KEY (presc_doc_id) REFERENCES doctors(doctor_id);

CREATE TABLE patient_appointment_plan_details (
    plan_details_id integer DEFAULT nextval('patient_appointment_plan_details_seq'::regclass) NOT NULL,
    plan_visit_date date,
    appointment_category character varying(3) ,
    consultation_reason_id integer,
    primary_resource_id character varying(100),
    secondary_resource_id character varying(100),
    appointment_id integer,
    plan_id integer
);

ALTER TABLE ONLY patient_appointment_plan_details ADD CONSTRAINT plan_details_pkey PRIMARY KEY (plan_details_id);

ALTER TABLE ONLY patient_appointment_plan_details ADD CONSTRAINT plan_id_fkey FOREIGN KEY (plan_id) REFERENCES patient_appointment_plan(plan_id);

ALTER TABLE ONLY scheduler_appointments ADD CONSTRAINT appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES scheduler_appointments(appointment_id);

CREATE SEQUENCE complaint_type_master_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE complaint_type_master (complaint_type_id Integer PRIMARY KEY NOT NULL,complaint_type varchar(40),duration Integer,status char(1),created_date date,created_by varchar(30),mod_date date,mod_by varchar(30));

CREATE TABLE form_components_center_applicability ( form_components_center_id int primary key not null , form_components_id int , center_id int , status char(1) , status_on_practo char(500) );

CREATE sequence form_components_center_applicability_seq start with 1 increment by 1 no maxvalue no minvalue cache 1;

insert into form_components_center_applicability (SELECT nextval('form_components_center_applicability_seq'), id, 0, 'A' FROM form_components);

CREATE SEQUENCE form_department_details_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE TABLE form_department_details (
  form_department_id INTEGER DEFAULT nextval('form_department_details_seq'::regclass) PRIMARY KEY NOT NULL,
  id Integer NOT NULL,
  dept_id character varying(50) NOT NULL
  );

INSERT INTO form_department_details (SELECT nextval('form_department_details_seq'), id, dept_id from form_components);

ALTER TABLE form_components RENAME COLUMN dept_id TO obsolete_dept_id;

CREATE TABLE form_template_data (
  section_id Integer NOT NULL,
  form_id Integer NOT NULL,
  data text NOT NULL
  );

-- Don't append migrations to this file. Create a new liquibase migration using steps mentioned here
-- https://github.com/practo/insta-hms/wiki/Adding-a-new-liquibase-migration
