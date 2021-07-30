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

alter table patient_medicine_prescriptions add column time_of_intake character(1) NOT NULL default 'N';
comment on column patient_medicine_prescriptions.time_of_intake is 'Possible values are N → None, B → Before Food, A → After Food';

alter table patient_other_medicine_prescriptions add column time_of_intake character(1) NOT NULL default 'N';
comment on column patient_other_medicine_prescriptions.time_of_intake is 'Possible values are N → None, B → Before Food, A → After Food';

alter table patient_other_prescriptions add column time_of_intake character(1)  NOT NULL default 'N';
comment on column patient_other_prescriptions.time_of_intake is 'Possible values are N → None, B → Before Food, A → After Food';

alter table patient_medicine_prescriptions add column start_date date;
alter table patient_other_medicine_prescriptions add column start_date date;
alter table patient_other_prescriptions add column start_date date;
alter table patient_medicine_prescriptions add column end_date date;
alter table patient_other_medicine_prescriptions add column end_date date;
alter table patient_other_prescriptions add column end_date date;

alter table patient_medicine_prescriptions add column priority character varying(1) NOT NULL default 'N';
comment on column patient_medicine_prescriptions.priority is 'possible values are N → Normal, P → PRN/SOS, S → Stat, U → Urgent';

alter table patient_other_medicine_prescriptions add column priority character varying(1) NOT NULL default 'N';
comment on column patient_other_medicine_prescriptions.priority is 'possible values are N → Normal, P → PRN/SOS, S → Stat, U → Urgent';

alter table patient_other_prescriptions add column priority character varying(1) NOT NULL default 'N';
comment on column patient_other_prescriptions.priority is 'possible values are N → Normal, P → PRN/SOS, S → Stat, U → Urgent';

alter table patient_medicine_prescriptions add column controlled_drug_number character varying(50);

CREATE SEQUENCE packages_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS packages;
CREATE TABLE packages(
	package_id             integer DEFAULT nextval('packages_seq'::regclass) PRIMARY KEY NOT NULL,
	package_name           varchar(50) UNIQUE NOT NULL,
	package_code		   varchar(50),
	type			       char,
	status                 char NOT NULL DEFAULT 'A',
	description            varchar(4000),
	visit_applicability    char NOT NULL,
	gender_applicability   char NOT NULL,
	service_sub_group_id   integer,
	valid_from             date,
	valid_till             date,
	created_by             varchar(50),
	created_at			   timestamp without time zone DEFAULT NOW(),
	modified_by            varchar(50),
	modified_at            timestamp without time zone
);

CREATE SEQUENCE center_package_applicability_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS center_package_applicability;
CREATE TABLE center_package_applicability(
	center_package_id      integer DEFAULT nextval('center_package_applicability_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id             integer NOT NULL,
	center_id			   integer NOT NULL,
	created_by             varchar(50),
	created_at			   timestamp without time zone DEFAULT NOW(),
	modified_by            varchar(50),
	modified_at            timestamp without time zone
);

CREATE SEQUENCE dept_package_applicability_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS dept_package_applicability;
CREATE TABLE dept_package_applicability(
	dept_package_id       integer DEFAULT nextval('dept_package_applicability_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id            integer NOT NULL,
	dept_id				  varchar(10) NOT NULL,
	created_by            varchar(50),
	created_at			  timestamp without time zone DEFAULT NOW(),
	modified_by           varchar(50),
	modified_at           timestamp without time zone
);

CREATE SEQUENCE tpa_package_applicability_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS tpa_package_applicability;
CREATE TABLE tpa_package_applicability(
	tpa_package_id         integer DEFAULT nextval('tpa_package_applicability_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id             integer NOT NULL,
	tpa_id				   varchar(15) NOT NULL,
	created_by             varchar(50),
	created_at			   timestamp without time zone DEFAULT NOW(),
	modified_by            varchar(50),
	modified_at            timestamp without time zone
);

CREATE SEQUENCE package_contents_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS package_contents;
CREATE TABLE package_contents(
	package_content_id     integer DEFAULT nextval('package_contents_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id             integer NOT NULL,
	activity_id            varchar(200),
	activity_type		   varchar(50),
	activity_qty           integer  DEFAULT 1,
	activity_qty_uom       varchar,
	activity_remarks   	   varchar,
	doctor_id			   varchar(20),
	dept_id				   varchar(20),
	display_order          integer,
	modified_by            varchar(50),
	modified_at            timestamp without time zone,
	created_by             varchar(50),
	created_at			   timestamp without time zone DEFAULT NOW()
);

CREATE SEQUENCE sold_packages_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS sold_packages;
CREATE TABLE sold_packages(
	sold_packages_id	  integer DEFAULT nextval('sold_packages_seq'::regclass) PRIMARY KEY NOT NULL,
	package_id			  integer NOT NULL,
	mr_no				  varchar(15) NOT NULL,
	status				  char NOT NULL
);

CREATE SEQUENCE sold_packages_contents_seq
	START WITH 1
	INCREMENT BY 1
	NO MINVALUE
	NO MAXVALUE
	CACHE 1;

DROP TABLE IF EXISTS sold_packages_contents;
CREATE TABLE sold_packages_contents(
	sold_packages_contents_id	  integer DEFAULT nextval('sold_packages_contents_seq'::regclass) PRIMARY KEY NOT NULL,
	sold_package_id				  integer NOT NULL,
	activity_id            		  varchar(200),
	activity_type		   		  varchar(50),
	activity_qty           		  integer  DEFAULT 1,
	activity_qty_uom       		  varchar,
	activity_remarks   	   		  varchar,
	doctor_id			   		  varchar(20),
	dept_id				   		  varchar(20),
	display_order          		  integer,
	modified_by            		  varchar(50),
	modified_at            		  timestamp without time zone,
	created_by             		  varchar(50),
	created_at			   		  timestamp without time zone DEFAULT NOW()
);

ALTER TABLE hospital_center_master ADD COLUMN ha_username character varying(50);
ALTER TABLE hospital_center_master ADD COLUMN ha_password character varying(50);

UPDATE hospital_center_master SET ha_username= shafafiya_preauth_user_id, ha_password= shafafiya_preauth_password where health_authority='HAAD';
UPDATE hospital_center_master SET ha_username= dhpo_facility_user_id, ha_password= dhpo_facility_password where health_authority='DHA';

ALTER TABLE hosp_pharmacy_sale_seq_prefs ADD COLUMN pharmacy_bill_seq_id integer;

CREATE SEQUENCE hosp_pharmacy_sale_seq_prefs_seq
     START WITH 1
 	 INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;
UPDATE hosp_pharmacy_sale_seq_prefs SET pharmacy_bill_seq_id = nextval('hosp_pharmacy_sale_seq_prefs_seq');
ALTER TABLE hosp_pharmacy_sale_seq_prefs ADD CONSTRAINT hosp_pharmacy_sale_seq_prefs_pkey PRIMARY KEY (pharmacy_bill_seq_id);

ALTER  TABLE  hosp_id_patterns ADD COLUMN transaction_type character (3);

ALTER  TABLE  hosp_bill_audit_seq_prefs DROP CONSTRAINT  hosp_bill_audit_seq_prefs_pkey;
ALTER TABLE hosp_bill_audit_seq_prefs ADD COLUMN bill_audit_number_seq_id integer;

CREATE SEQUENCE hosp_bill_audit_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;
UPDATE hosp_bill_audit_seq_prefs SET bill_audit_number_seq_id = nextval('hosp_bill_audit_seq_prefs_seq');
ALTER TABLE hosp_bill_audit_seq_prefs ADD CONSTRAINT hosp_bill_audit_seq_prefs_pkey PRIMARY KEY (bill_audit_number_seq_id);

ALTER TABLE hosp_bill_audit_seq_prefs ADD COLUMN is_tpa character(1) default '*';
ALTER TABLE hosp_bill_audit_seq_prefs ADD COLUMN is_credit_note character(1) NOT NULL DEFAULT '*';

ALTER TABLE generic_preferences ADD column patient_name_match_distance integer DEFAULT 0;

CREATE SEQUENCE medicine_dosage_master_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE medicine_dosage_master DROP CONSTRAINT medicine_dosage_master_pkey;
ALTER TABLE medicine_dosage_master ADD COLUMN dosage_id integer DEFAULT nextval('medicine_dosage_master_seq'::regclass) PRIMARY KEY NOT NULL;
CREATE INDEX medicine_dosage_master_dosage_name ON medicine_dosage_master(dosage_name);

-- ALTER TABLE patient_consultation_field_values ADD COLUMN field_name_values character varying;

-- UPDATE patient_consultation_field_values pc SET field_name_values = dhtf.field_name || ':- ' || pcfv.field_value
-- FROM  patient_consultation_field_values pcfv
-- JOIN  doc_hvf_template_fields dhtf ON (dhtf.field_id = pcfv.field_id)
-- WHERE pc.value_id = pcfv.value_id;

-- INSERT INTO patient_consultation_field_values (value_id,doc_id,field_id,field_value)
-- (
--   SELECT DISTINCT nextval('patient_consultation_field_values_seq'), doc_id, -1 ,  
--   string_agg(field_name_values, E'\n') AS field_value FROM patient_consultation_field_values pcfv
--   GROUP BY doc_id HAVING NOT EXISTS (SELECT 1 FROM patient_consultation_field_values WHERE doc_id = pcfv.doc_id AND field_id = -1)
-- );

-- ALTER TABLE patient_consultation_field_values DROP COLUMN field_name_values; 

ALTER TABLE system_generated_sections ADD COLUMN display_name character varying(2000);
UPDATE system_generated_sections SET display_name = split_part(section_name, ' (Sys)', 1);

-- Build 11.12.0-7976

-- Build 11.12.0-7978

-- Build 11.12.0-7981

-- Build 11.12.0-7984

-- Build 11.12.0-7990

-- Build 11.12.0-7992

-- Build 11.12.0-7995

-- Build 11.12.0-7997

-- Build 11.12.0-8001

-- Build 11.12.0-8004

-- Build 11.12.0-8007

-- Build 11.12.0-8011

-- Build 11.12.0-8014

-- Build 11.12.0-8020

-- Build 11.12.0-8026

-- Build 11.12.0-8031

-- Build 11.12.0-8035

-- Build 11.12.0-8036

-- Build 11.12.0-8041

-- Build 11.12.0-8046

-- Build 11.12.0-8049

-- Build 11.12.0-8056

CREATE SEQUENCE hosp_voucher_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

CREATE TABLE hosp_voucher_seq_prefs
(
voucher_seq_id integer NOT NULL default nextval('hosp_voucher_seq_prefs_seq'),
  priority integer NOT NULL,
  center_id integer NOT NULL DEFAULT 0,
  pattern_id character varying NOT NULL,
  constraint hosp_voucher_seq_prefs_pkey primary key(voucher_seq_id)
);

INSERT INTO hosp_voucher_seq_prefs(priority, center_id, pattern_id) VALUES (1000, '0', 'VOUCHER_NUMBER_DEFAULT');

INSERT INTO hosp_id_patterns(
pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type,transaction_type)
VALUES ('VOUCHER_NUMBER_DEFAULT', 'VC', '', '99000000', 'hosp_voucher_seq_prefs_seq', '', '', 'Txn','PVN');

-- Build 11.12.0-8062

-- Build 11.12.0-8067

-- Build 11.12.0-8075

-- Build 11.12.0-8078

-- Build 11.12.0-8081

-- Build 11.12.0-8086

-- Build 11.12.0-8091

-- Build 11.12.0-8093

ALTER  TABLE  hosp_receipt_seq_prefs DROP CONSTRAINT  hosp_receipt_seq_prefs_pkey;
ALTER TABLE hosp_receipt_seq_prefs ADD COLUMN receipt_number_seq_id integer;

CREATE SEQUENCE hosp_receipt_seq_prefs_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;
UPDATE hosp_receipt_seq_prefs SET receipt_number_seq_id = nextval('hosp_receipt_seq_prefs_seq');
ALTER TABLE hosp_receipt_seq_prefs ADD CONSTRAINT hosp_receipt_seq_prefs_pkey PRIMARY KEY (receipt_number_seq_id);
CREATE SEQUENCE bill_charge_claim_tax_seq;

ALTER TABLE bill_charge_claim_tax ADD COLUMN charge_claim_tax_id integer NOT NULL DEFAULT  nextval('bill_charge_claim_tax_seq'), 
								  ADD COLUMN adj_amt character(1) default 'N',
								  ADD COLUMN charge_tax_id integer;

--ALTER TABLE bill_charge_claim_tax ADD COLUMN charge_tax_id integer;

UPDATE bill_charge_claim_tax bcct SET charge_tax_id = bct.charge_tax_id from bill_charge_tax bct where bcct.charge_id = bct.charge_id and bcct.tax_sub_group_id = bct.tax_sub_group_id;

DELETE FROM bill_charge_claim_tax WHERE charge_tax_id IS null;

ALTER TABLE bill_charge_claim_tax ALTER COLUMN charge_tax_id SET NOT NULL;

--CREATE SEQUENCE bill_charge_claim_tax_seq;

--ALTER TABLE bill_charge_claim_tax ADD COLUMN charge_claim_tax_id integer NOT NULL DEFAULT  nextval('bill_charge_claim_tax_seq');
-- Build 11.12.0-8096

-- Build 11.12.0-8100

-- Build 11.12.0-8103

-- Build 11.12.0-8106

DROP TABLE IF EXISTS store_grn_tax_details;
CREATE TABLE store_grn_tax_details (
    medicine_id integer NOT NULL,
    grn_no character varying(20) NOT NULL,
    item_batch_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    tax_rate numeric,
    tax_amt numeric(15,2),
    PRIMARY KEY(medicine_id, grn_no, item_batch_id, item_subgroup_id)
);

-- Build 11.12.0-8110

-- Build 11.12.0-8111

-- Build 11.12.0-8115

-- Build 11.12.0-8119

-- Insert all item groups

INSERT INTO item_groups
SELECT * FROM 
(SELECT nextval('item_groups_seq'), 
	regexp_split_to_table('CGST,SGST,i-GST', E','), 
	regexp_split_to_table('GST,GST,IGST', E','), generate_series(1,3), 'TAX', 'A') AS foo 
WHERE 
NOT EXISTS (SELECT * FROM item_groups LIMIT 1)
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Insert all item_subgroups
INSERT INTO item_sub_groups  
SELECT * FROM 
-- CGST sub-groups
(SELECT nextval('item_sub_groups_seq'), regexp_split_to_table('CGST-0,CGST-5,CGST-12,CGST-18,CGST-28', E','), generate_series(1,5),
	null, item_group_id, 'A' FROM item_groups WHERE item_group_name = 'CGST'
UNION ALL  
-- SGST sub-groups
SELECT nextval('item_sub_groups_seq'), regexp_split_to_table('SGST-0,SGST-5,SGST-12,SGST-18,SGST-28', E','), generate_series(6,10),
	null, item_group_id, 'A' FROM item_groups WHERE item_group_name = 'SGST'
UNION ALL  
-- i-GST sub-groups
SELECT nextval('item_sub_groups_seq'), regexp_split_to_table('iGST-0,iGST-5,iGST-12,iGST-18,iGST-28', E','), generate_series(11,15),
	null, item_group_id, 'A' FROM item_groups WHERE item_group_name = 'i-GST') AS foo
WHERE NOT EXISTS
(SELECT * FROM item_sub_groups LIMIT 1)
AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Insert tax rate for each sub group
INSERT INTO item_sub_groups_tax_details
SELECT * FROM 
(SELECT item_subgroup_id, round((split_part(item_subgroup_name, '-', 2)::numeric / 2.0), 
	coalesce(pref.after_decimal_digits, 2)) AS tax_rate, null::text AS tax_expr, date(now()) AS validity_start_date, 
	null::date AS vaidity_end_date 
FROM item_sub_groups isg JOIN item_groups ig USING (item_group_id) CROSS JOIN generic_preferences pref 
WHERE ig.item_group_name IN ('CGST', 'SGST') 

UNION ALL

SELECT item_subgroup_id, round((split_part(item_subgroup_name, '-', 2)::numeric), 
coalesce(pref.after_decimal_digits, 2)) as tax_rate, 
null::text AS tax_expr, date(now()) AS validity_start_date, null::date as vaidity_end_date FROM 
item_sub_groups isg JOIN item_groups ig USING (item_group_id) CROSS JOIN generic_preferences pref 
WHERE ig.item_group_name IN ('i-GST')) AS foo
WHERE 
NOT EXISTS (SELECT * FROM item_sub_groups_tax_details LIMIT 1) AND 
EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- create tax sub group mapping into the item master 
INSERT INTO store_item_sub_groups
SELECT * FROM 
(SELECT DISTINCT medicine_id, item_subgroup_id FROM store_item_details sid CROSS JOIN 
	(SELECT ig.item_group_id, ig.item_group_name, isg.item_subgroup_id, isgtd.tax_rate FROM 
	item_sub_groups_tax_details isgtd JOIN item_sub_groups isg USING (item_subgroup_id) JOIN item_groups ig USING (item_group_id)) AS tr
CROSS JOIN generic_preferences pref
WHERE abs(round(sid.tax_rate, coalesce(pref.after_decimal_digits, 2)) - round(tr.tax_rate, coalesce(pref.after_decimal_digits, 2))) <= 1.0 
AND tr.item_group_name IN ('i-GST') 

UNION ALL

SELECT DISTINCT medicine_id, item_subgroup_id FROM store_item_details sid CROSS JOIN 
	(SELECT ig.item_group_id, ig.item_group_name, isg.item_subgroup_id, isgtd.tax_rate FROM 
	item_sub_groups_tax_details isgtd JOIN item_sub_groups isg USING (item_subgroup_id) JOIN item_groups ig USING (item_group_id)) AS tr
CROSS JOIN generic_preferences pref
WHERE abs(round(sid.tax_rate, coalesce(pref.after_decimal_digits, 2)) - round((tr.tax_rate * 2), coalesce(pref.after_decimal_digits, 2))) <= 1.0 
AND tr.item_group_name in ('CGST','SGST')) AS foo
WHERE NOT EXISTS (SELECT * FROM store_item_sub_groups LIMIT 1) AND 
EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Build 11.12.0-8120

-- Build 11.12.0-8125

-- Build 11.12.0-8129

-- Build 11.12.0-8132

-- Build 11.12.0-8136

ALTER TABLE scheduler_appointments add column primary_sponsor_id character varying(20); 
ALTER TABLE scheduler_appointments add column primary_sponsor_co character varying(15);
ALTER TABLE scheduler_appointments add column plan_id integer;
ALTER TABLE scheduler_appointments add column plan_type_id integer;
ALTER TABLE scheduler_appointments add column member_id character varying(40);
-- Build 11.12.0-8140

CREATE TABLE dyna_package_item_sub_groups (
    dyna_package_id integer NOT NULL,
    item_subgroup_id integer NOT NULL,
    PRIMARY KEY(dyna_package_id, item_subgroup_id)
);
-- Build 11.12.0-8144

-- Build 11.12.0-8147

-- Build 11.12.0-8151

-- Build 11.12.0-8154

-- Build 11.12.0-8157

-- Build 11.12.0-8160

-- Build 11.12.0-8161

update item_sub_groups set item_subgroup_name = 'CGST-2.5' where item_subgroup_name = 'CGST-5' AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'CGST-6' where item_subgroup_name = 'CGST-12' AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'CGST-9' where item_subgroup_name = 'CGST-18' AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'CGST-14' where item_subgroup_name = 'CGST-28'AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


update item_sub_groups set item_subgroup_name = 'SGST-2.5' where item_subgroup_name = 'SGST-5'AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'SGST-6' where item_subgroup_name = 'SGST-12'AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'SGST-9' where item_subgroup_name = 'SGST-18'AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

update item_sub_groups set item_subgroup_name = 'SGST-14' where item_subgroup_name = 'SGST-28'AND EXISTS
-- default center in India
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Build 11.12.0-8164

--create index idx_charge_id_bill_charge_audit_log on bill_charge_audit_log(charge_id);

-- Build 11.12.0-8167

-- Build 11.12.0-8171

-- Build 11.12.0-8172

-- Build 11.12.0-8174

-- Migration for open GRNs with GST

INSERT INTO store_grn_tax_details (grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt)
SELECT grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT sg.grn_no, sgd.medicine_id, sgd.item_batch_id, ig.group_code, sisg.item_subgroup_id, 
	isgtd.tax_rate, round((sgd.tax * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY sg.grn_no, sgd.medicine_id, 
	sgd.item_batch_id, ig.group_code ORDER BY sg.grn_no, sgd.medicine_id, sgd.item_batch_id)),2) AS tax_amt 
	FROM store_grn_main sg 
	JOIN store_grn_details sgd ON (sg.grn_no = sgd.grn_no) 
	LEFT JOIN store_item_sub_groups sisg ON (sgd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) 
	JOIN store_invoice si ON (si.supplier_invoice_id = sg.supplier_invoice_id) 
	WHERE si.status = 'O' AND upper(si.tax_name) = 'GST' AND ig.group_code = 'GST' AND isgtd.tax_rate > 0) as foo;

-- Migration for open GRNs with IGST
	
INSERT INTO store_grn_tax_details (grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt)
SELECT grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT sg.grn_no, sgd.medicine_id, sgd.item_batch_id, ig.group_code, sisg.item_subgroup_id, 
	isgtd.tax_rate, round((sgd.tax * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY sg.grn_no, sgd.medicine_id, 
	sgd.item_batch_id, ig.group_code ORDER BY sg.grn_no, sgd.medicine_id, sgd.item_batch_id)),2) AS tax_amt 
	FROM store_grn_main sg 
	JOIN store_grn_details sgd ON (sg.grn_no = sgd.grn_no) 
	LEFT JOIN store_item_sub_groups sisg ON (sgd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) 
	JOIN store_invoice si ON (si.supplier_invoice_id = sg.supplier_invoice_id) 
	WHERE si.status = 'O' AND upper(si.tax_name) = 'IGST' AND ig.group_code = 'IGST' AND isgtd.tax_rate > 0) as foo;

-- Build 11.12.0-8177

-- Migration for open Debit Note with GST

INSERT INTO store_grn_tax_details (grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt)
SELECT grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT sg.grn_no, sgd.medicine_id, sgd.item_batch_id, ig.group_code, sisg.item_subgroup_id, 
	isgtd.tax_rate, round((-sgd.tax * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY sg.grn_no, sgd.medicine_id, 
	sgd.item_batch_id, ig.group_code ORDER BY sg.grn_no, sgd.medicine_id, sgd.item_batch_id)),2) AS tax_amt 
	FROM store_debit_note sdn 
	JOIN store_grn_main sg ON (sg.debit_note_no = sdn.debit_note_no)
	JOIN store_grn_details sgd ON (sg.grn_no = sgd.grn_no) 
	LEFT JOIN store_item_sub_groups sisg ON (sgd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
	WHERE sdn.status = 'O' AND upper(sdn.tax_name) = 'GST' AND ig.group_code = 'GST' AND isgtd.tax_rate > 0) as foo;

-- Migration for open Debit Note with IGST

INSERT INTO store_grn_tax_details (grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt)
SELECT grn_no, medicine_id, item_batch_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT sg.grn_no, sgd.medicine_id, sgd.item_batch_id, ig.group_code, sisg.item_subgroup_id, 
	isgtd.tax_rate, round((-sgd.tax * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY sg.grn_no, sgd.medicine_id, 
	sgd.item_batch_id, ig.group_code ORDER BY sg.grn_no, sgd.medicine_id, sgd.item_batch_id)),2) AS tax_amt 
	FROM store_debit_note sdn 
	JOIN store_grn_main sg ON (sg.debit_note_no = sdn.debit_note_no)
	JOIN store_grn_details sgd ON (sg.grn_no = sgd.grn_no) 
	LEFT JOIN store_item_sub_groups sisg ON (sgd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
	WHERE sdn.status = 'O' AND upper(sdn.tax_name) = 'IGST' AND ig.group_code = 'IGST' AND isgtd.tax_rate > 0) as foo;
	
-- Migration for open PO with GST

INSERT INTO store_po_tax_details (po_no, medicine_id, item_subgroup_id, tax_rate, tax_amt)
SELECT po_no, medicine_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT spm.po_no, sisg.item_subgroup_id, spd.medicine_id, ig.group_code,  
	isgtd.tax_rate, round((spd.vat * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY spd.po_no, spd.medicine_id, 
	ig.group_code ORDER BY spd.po_no, spd.medicine_id)),2) AS tax_amt 
	
	FROM store_po_main spm 
	JOIN store_po spd ON (spm.po_no = spd.po_no) 
	LEFT JOIN store_item_sub_groups sisg ON (spd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
	WHERE (spm.status = 'O' OR spm.status = 'AO') AND upper(spm.vat_type) = 'GST' AND ig.group_code = 'GST' AND isgtd.tax_rate > 0) as foo;


-- Migration for open PO with IGST

INSERT INTO store_po_tax_details (po_no, medicine_id, item_subgroup_id, tax_rate, tax_amt)
SELECT po_no, medicine_id, item_subgroup_id, tax_rate, tax_amt 
FROM 
(SELECT spm.po_no, sisg.item_subgroup_id, spd.medicine_id, ig.group_code,  
	isgtd.tax_rate, round((spd.vat * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY spd.po_no, spd.medicine_id, 
	ig.group_code ORDER BY spd.po_no, spd.medicine_id)),2) AS tax_amt 
	
	FROM store_po_main spm 
	JOIN store_po spd ON (spm.po_no = spd.po_no) 
	LEFT JOIN store_item_sub_groups sisg ON (spd.medicine_id = sisg.medicine_id) 
	LEFT JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_sub_groups isg ON (isg.item_subgroup_id = sisg.item_subgroup_id) 
	LEFT JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
	WHERE (spm.status = 'O' OR spm.status = 'AO') AND upper(spm.vat_type) = 'IGST' AND ig.group_code = 'IGST' AND isgtd.tax_rate > 0) as foo;

ALTER TABLE bill_charge_adjustment ADD COLUMN tax_amt numeric(15,2);
ALTER TABLE bill_charge_adjustment ADD COLUMN sponsor_tax_amt numeric(15,2);

--create index idx_bill_no_bill_charge_audit_log on bill_charge_audit_log(bill_no);
--create index idx_bill_no_bill_audit_log on bill_audit_log(bill_no);

-- Build 11.12.0-8179

ALTER TABLE tpa_master ALTER COLUMN claim_amount_includes_tax SET DEFAULT 'Y';
ALTER TABLE tpa_master ALTER COLUMN limit_includes_tax SET DEFAULT 'Y';

UPDATE tpa_master SET claim_amount_includes_tax = 'Y';
UPDATE tpa_master SET limit_includes_tax = 'Y';

-- Build 11.12.0-8181

-- Build 11.12.0-8182

-- Build 11.12.0-8184

-- Build 11.12.0-8186

-- KSA TAX Group Migration 

-- Update exist VAT group TO GCC VAT
UPDATE item_groups SET item_group_name ='GCC VAT' WHERE group_code = 'VAT';

-- Update exist VAT group to KSA
UPDATE item_groups SET item_group_name ='KSA VAT Citizen Exempt', group_code ='KSACEX' WHERE group_code = 'VAT' AND EXISTS
(SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '966'));

-- Insert new group for KSA
INSERT INTO item_groups(item_group_id, item_group_name, group_code, item_group_type_id, status)
SELECT * FROM 
(SELECT nextval('item_groups_seq'), 'KSA VAT Citizen Taxable', 'KSACTA', item_group_type_id, 'A' FROM item_group_type WHERE item_group_type_id ='TAX'
 AND EXISTS
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '966'))) foo;

-- Insert new subgroups for KSA
INSERT INTO item_sub_groups(item_subgroup_id, item_subgroup_name, item_group_id, status)
SELECT * FROM 
(SELECT nextval('item_sub_groups_seq'), regexp_split_to_table('VAT-5,VAT-0,VAT-Exempt', E','), item_group_id, 'A' FROM item_groups WHERE group_code = 'KSACTA'
 AND EXISTS
(SELECT * FROM hospital_center_master WHERE center_id = 0 
AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '966'))) foo;

-- Insert tax rate for each sub group(KSA)
INSERT INTO item_sub_groups_tax_details(item_subgroup_id, tax_rate, validity_start)
SELECT * FROM 
(SELECT item_subgroup_id, 5, date(now()) AS validity_start_date
FROM item_sub_groups isg JOIN item_groups ig USING (item_group_id) CROSS JOIN generic_preferences pref 
WHERE ig.item_group_name = 'KSA VAT Citizen Taxable' AND isg.item_subgroup_name = 'VAT-5' ) foo;

INSERT INTO item_sub_groups_tax_details(item_subgroup_id, tax_rate, validity_start)
SELECT * FROM 
(SELECT item_subgroup_id, 0, date(now()) AS validity_start_date
FROM item_sub_groups isg JOIN item_groups ig USING (item_group_id) CROSS JOIN generic_preferences pref 
WHERE ig.item_group_name = 'KSA VAT Citizen Taxable' AND isg.item_subgroup_name = 'VAT-0' ) foo;

INSERT INTO item_sub_groups_tax_details(item_subgroup_id, validity_start)
SELECT * FROM 
(SELECT item_subgroup_id, date(now()) AS validity_start_date
FROM item_sub_groups isg JOIN item_groups ig USING (item_group_id) CROSS JOIN generic_preferences pref 
WHERE ig.item_group_name = 'KSA VAT Citizen Taxable' AND isg.item_subgroup_name = 'VAT-Exempt' ) foo;

-- KSA - new column to track original tax
ALTER TABLE bill_charge_tax ADD COLUMN original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;
ALTER TABLE bill_charge ADD COLUMN original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00,
						ADD COLUMN return_original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;
ALTER TABLE bill ADD COLUMN total_original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

-- KSA - migration for old bills
update bill_charge_tax SET original_tax_amt = tax_amount ;

--KSA - TPA bills Adjustment flag
--ALTER TABLE bill_charge_claim_tax ADD COLUMN adj_amt character(1) default 'N';

ALTER TABLE store_sales_details ADD COLUMN original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00,
								ADD COLUMN return_original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;
ALTER TABLE store_sales_tax_details ADD COLUMN original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00, 
									ADD COLUMN return_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

-- KSA - migration for old sales bills
update store_sales_tax_details SET original_tax_amt = COALESCE(tax_amt, 0);

-- KSA - migration for old sales bills
update store_sales_details SET original_tax_amt = COALESCE(tax, 0);

--KSA - TPA sales claim Adjustment flag
ALTER TABLE sales_claim_tax_details ADD COLUMN adj_amt character(1) default 'N';

--ALTER TABLE store_sales_details ADD COLUMN return_original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

-- Build 11.12.0-8188

-- Build 11.12.0-8191
ALTER TABLE hms_accounting_info ALTER COLUMN voucher_no type character varying;
-- Build 11.12.0-8192

-- Build 11.12.0-8193

-- Build 11.12.0-8194

-- Build 11.12.0-8197

-- Build 11.12.0-8198

-- Build 11.12.0-8199

-- Build 11.12.0-8200

-- Build 11.12.0-8201

-- Build 11.12.0-8202

-- Build 11.12.0-8203

-- Build 11.12.0-8204

-- Build 11.12.0-8205

-- Build 11.12.0-8206

-- Build 11.12.0-8207

-- Build 11.12.0-8208

-- Build 11.12.0-8209

-- Build 11.12.0-8210

-- Build 11.12.0-8211

-- Build 11.12.0-8212

-- Build 11.12.0-8213

-- Build 11.12.0-8214

-- Build 11.12.0-8215

-- Build 11.12.0-8216

-- Build 11.12.0-8217

-- Build 11.12.0-8218

-- Build 11.12.0-8219

-- Build 11.12.0-8220

-- Build 11.12.0-8221

-- Build 11.12.0-8222

-- Build 11.12.0-8223

-- Build 11.12.0-8224

-- Build 11.12.0-8225

-- Build 11.12.0-8226

-- Build 11.12.0-8227

-- Build 11.12.0-8228

-- Build 11.12.0-8229

insert into insta_integration (integration_name,status,environment) ( SELECT 'loyalty_offers','A','PROD' 
WHERE NOT EXISTS (SELECT 1 FROM insta_integration WHERE integration_name = 'loyalty_offers' AND environment = 'PROD'));

insert into insta_integration (integration_name,status,environment) (SELECT 'loyalty_offers','I','TEST' 
WHERE NOT EXISTS (SELECT 1 FROM insta_integration WHERE integration_name = 'loyalty_offers' AND environment = 'TEST'));

-- Build 11.12.0-8230

-- Build 11.12.1-8233

ALTER TABLE store_invoice ADD COLUMN transportation_charges numeric(15,2) default 0;; 
ALTER TABLE store_po_main ADD COLUMN transportation_charges numeric(15,2) default 0;;

-- Build 11.12.1-8234

-- Build 11.12.1-8235

-- Build 11.12.1-8237

-- Build 11.12.1-8240


-- Build 11.12.1-8241

-- Build 11.12.1-8242

-- Build 11.12.1-8244

--CREATE INDEX tests_prescribed_presc_date_idx ON tests_prescribed (date(pres_date));

-- Build 11.12.1-8248

CREATE INDEX idx_bed_names_bed_type_upper ON bed_names ((UPPER(bed_type)));

-- Build 11.12.1-8251

-- Build 11.12.1-8252

--ALTER TABLE bill_charge ADD COLUMN return_original_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

--ALTER TABLE store_sales_tax_details ADD COLUMN return_tax_amt numeric(15,2) NOT NULL DEFAULT 0.00;

CREATE TABLE mrd_code_claim_groups(
mrd_code_id integer NOT NULL PRIMARY KEY,
code_group character varying(15) NOT NULL DEFAULT 'LG'
);

INSERT INTO mrd_code_claim_groups(SELECT mrd_code_id FROM mrd_codes_master WHERE code_type in('HAAD Drug',
	'HAAD HCPCS','DHA Drug','DHA HCPCS','Drug','HCPCS','CPT','DSL','Service','Service Code',
	'Dental','E&M', 'Drug HAAD', 'Drug DHA', 'HCPCS HAAD', 'HCPCS DHA'));

	
-- Build 11.12.1-8255

ALTER TABLE  store_retail_customers ADD COLUMN nationality_id  character varying(50);
ALTER TABLE  store_retail_customers ADD COLUMN government_identifier character varying(50);
ALTER TABLE  store_retail_customers ADD COLUMN identifier_id integer;

-- Build 11.12.1-8256

-- Build 11.12.1-8257

-- Build 11.12.1-8259

-- Build 11.12.1-8260

-- Build 11.12.1-8261

-- Build 11.12.1-8262

alter table scheduler_appointments add column cond_doc_id varchar(30);

-- Build 11.12.1-8265

-- Build 11.12.1-8266

-- Build 11.12.1-8268

CREATE INDEX ppa_consultation_id_idx ON preauth_prescription_activities USING btree(consultation_id);
CREATE INDEX ppa_visit_id_idx ON preauth_prescription_activities USING btree(visit_id);

-- Sales migration script.

UPDATE store_sales_tax_details sstd SET tax_amt = -1 * sstd.tax_amt, original_tax_amt = -1 * sstd.original_tax_amt 
	FROM store_sales_details ssd JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) 
	JOIN bill b ON (b.bill_no = ssm.bill_no) 
	WHERE sstd.sale_item_id = ssd.sale_item_id AND ssm.type='R' AND sstd.tax_amt > 0 AND b.status IN ('A', 'F', 'C') 
	AND b.open_date >= '2017-04-01' AND b.open_date <= now();

UPDATE bill_charge_tax bct SET tax_amount = -1 * bct.tax_amount, original_tax_amt = -1 * bct.original_tax_amt 
	FROM store_sales_main ssm JOIN bill b ON (b.bill_no = ssm.bill_no) 
	WHERE ssm.charge_id = bct.charge_id AND ssm.type='R' AND bct.tax_amount > 0 AND b.status IN ('A', 'F', 'C') 
	AND b.open_date >= '2017-04-01' AND b.open_date <= now();

UPDATE bill_charge bc SET tax_amt = -1 * bc.tax_amt, original_tax_amt = -1 * bc.original_tax_amt 
	FROM store_sales_main ssm JOIN bill b ON (b.bill_no = ssm.bill_no) 
	WHERE ssm.charge_id = bc.charge_id AND ssm.type='R' AND bc.tax_amt > 0 AND b.status IN ('A', 'F', 'C') 
	AND b.open_date >= '2017-04-01' AND b.open_date <= now();

UPDATE bill b SET total_tax = (SELECT COALESCE(SUM(bc.tax_amt), 0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) 
	WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now();

-- Update sales return tax amount for return.
UPDATE store_sales_details ssd SET return_tax_amt = round((ssd.tax/ssd.quantity)*ssd.return_qty, 4)
FROM store_sales_main ssm JOIN bill b ON (b.bill_no = ssm.bill_no) 
WHERE ssm.sale_id = ssd.sale_id AND b.status IN ('A', 'F', 'C')  
AND ssd.return_qty != 0 AND ssd.return_tax_amt = 0 AND ssd.tax != 0 AND ssd.quantity != 0  AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Insert store_sales_tax_details tax split.
INSERT INTO store_sales_tax_details (sale_item_id, item_subgroup_id, tax_rate, tax_amt, original_tax_amt, return_tax_amt)
	SELECT sale_item_id, item_subgroup_id, tax_rate, tax_amt, tax_amt as original_tax_amt, return_tax_amt
		FROM 
		(SELECT ssd.sale_item_id, sisg.item_subgroup_id, ssd.medicine_id, ig.group_code,  
			isgtd.tax_rate, 
			CASE WHEN ssd.tax != 0 AND isgtd.tax_rate != 0 THEN round((ssd.tax * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY ssd.sale_item_id, 
			ig.group_code ORDER BY ssd.sale_item_id)),4) ELSE 0 END AS tax_amt, 
			CASE WHEN ssd.return_tax_amt != 0 AND isgtd.tax_rate != 0 THEN round((ssd.return_tax_amt * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY ssd.sale_item_id, 
			ig.group_code ORDER BY ssd.sale_item_id)),4) ELSE 0 END AS return_tax_amt
			FROM store_sales_main ssm
			JOIN bill b ON (b.bill_no = ssm.bill_no)
			JOIN (SELECT sssm.sale_id FROM store_sales_main sssm 
					JOIN bill bb ON (bb.bill_no = sssm.bill_no)
					JOIN store_sales_details sssd ON (sssd.sale_id = sssm.sale_id) 
					LEFT JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = sssd.sale_item_id) 
					WHERE bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() group by sssm.sale_id having count(sstd.item_subgroup_id) = 0 order by sssm.sale_id) as tc 
			ON (tc.sale_id = ssm.sale_id)
			JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
			JOIN store_item_sub_groups sisg ON (sisg.medicine_id = ssd.medicine_id) 
			JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
			JOIN item_sub_groups isg ON (isg.item_subgroup_id = isgtd.item_subgroup_id) 
			JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
		WHERE b.status IN ('A', 'F', 'C') AND ig.group_code = 'GST' AND b.open_date >= '2017-04-01' AND b.open_date <= now()
		AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN (SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'))) as foo;

-- Update store sales details original tax amount.
UPDATE store_sales_details ssd SET original_tax_amt = ssd.tax, return_original_tax_amt = ssd.return_tax_amt FROM store_sales_main ssm 
JOIN bill b ON (ssm.bill_no = b.bill_no) 
WHERE ssd.sale_id = ssm.sale_id AND ssd.tax != 0 AND ssd.original_tax_amt = 0 AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND 
EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


-- Update sales claim details tax = sum(tax).
UPDATE sales_claim_details scd SET tax_amt = 
	((scd.insurance_claim_amt - COALESCE(round(((scd.insurance_claim_amt*100)/(100+ssd.tax_rate)), 4), 0)))
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no)
JOIN (SELECT sssm.sale_id FROM store_sales_main sssm 
		JOIN bill bb ON (bb.bill_no = sssm.bill_no)
		JOIN store_sales_details sssd ON (sssd.sale_id = sssm.sale_id)
		JOIN sales_claim_details sccd ON (sccd.sale_item_id = sssd.sale_item_id) 
		LEFT JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = sccd.sale_item_id AND sctd.claim_id = sccd.claim_id) 
		where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() group by sssm.sale_id having count(sctd.item_subgroup_id) = 0 order by sssm.sale_id) as tc 
ON (tc.sale_id = ssm.sale_id)
JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id) 
WHERE ssd.sale_item_id = scd.sale_item_id AND scd.insurance_claim_amt != 0 AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND ssm.type = 'S' AND b.is_tpa = true 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update sales claim details insurance_claim_amount = insurance_claim_amount - tax.
UPDATE sales_claim_details scd SET insurance_claim_amt = scd.insurance_claim_amt - scd.tax_amt
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no) 
JOIN (SELECT sssm.sale_id FROM store_sales_main sssm 
		JOIN bill bb ON (bb.bill_no = sssm.bill_no)
		JOIN store_sales_details sssd ON (sssm.sale_id = sssd.sale_id)
		JOIN sales_claim_details sccd ON (sccd.sale_item_id = sssd.sale_item_id) 
		LEFT JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = sccd.sale_item_id AND sctd.claim_id = sccd.claim_id) 
		where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() group by sssm.sale_id having count(sctd.item_subgroup_id) = 0 order by sssm.sale_id) as tc 
ON (tc.sale_id = ssm.sale_id)
JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
WHERE ssd.sale_item_id = scd.sale_item_id AND scd.insurance_claim_amt != 0 AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND ssm.type = 'S' AND b.is_tpa = true 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update store_sales_details insurance_claim_amount = (Sum of multiple sponsors insurance_claim_amount)
UPDATE store_sales_details ssd SET insurance_claim_amt = COALESCE((SELECT SUM(insurance_claim_amt) FROM sales_claim_details scd WHERE scd.sale_item_id = ssd.sale_item_id), 0)
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no) 
WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND ssd.sale_id = ssm.sale_id AND ssm.type = 'S' AND b.is_tpa = true AND 
EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Insert sales_claim_tax_details tax split.
INSERT INTO sales_claim_tax_details (sale_item_id, claim_id, item_subgroup_id, tax_rate, tax_amt)
	SELECT sale_item_id, claim_id, item_subgroup_id, tax_rate, tax_amt
		FROM 
		(SELECT scd.sale_item_id, scd.claim_id, sisg.item_subgroup_id, ssd.medicine_id, ig.group_code,  
			isgtd.tax_rate, 
			CASE WHEN scd.tax_amt != 0 AND isgtd.tax_rate != 0 THEN round(scd.tax_amt * isgtd.tax_rate / sum(isgtd.tax_rate) OVER (PARTITION BY ssd.sale_item_id, scd.claim_id,
			ig.group_code ORDER BY ssd.sale_item_id),4) ELSE 0 END AS tax_amt 
			FROM store_sales_main ssm
			JOIN bill b ON (b.bill_no = ssm.bill_no)
			JOIN (SELECT sssm.sale_id FROM store_sales_main sssm 
					JOIN bill bb ON (bb.bill_no = sssm.bill_no)
					JOIN store_sales_details sssd ON (sssm.sale_id = sssd.sale_id)
					JOIN sales_claim_details sccd ON (sccd.sale_item_id = sssd.sale_item_id) 
					LEFT JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = sccd.sale_item_id AND sctd.claim_id = sccd.claim_id) 
					where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() group by sssm.sale_id having count(sctd.item_subgroup_id) = 0 order by sssm.sale_id) as tc 
			ON (tc.sale_id = ssm.sale_id)
			JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
			JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id)
			JOIN store_item_sub_groups sisg ON (sisg.medicine_id = ssd.medicine_id) 
			JOIN item_sub_groups_tax_details isgtd ON (isgtd.item_subgroup_id = sisg.item_subgroup_id) 
			JOIN item_sub_groups isg ON (isg.item_subgroup_id = isgtd.item_subgroup_id) 
			JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id)
		WHERE b.status IN ('A', 'F', 'C') AND ig.group_code = 'GST' AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND scd.tax_amt != 0 AND ssm.type = 'S' AND b.is_tpa = true 
		AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
			(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'))) as foo;


-- Insert bill_charge_tax tax split.
INSERT INTO bill_charge_tax (charge_id, tax_sub_group_id, tax_rate, tax_amount, original_tax_amt)
	SELECT charge_id, tax_sub_group_id, tax_rate, tax_amount, tax_amount as original_tax_amt
		FROM 
		(SELECT ssm.charge_id, sstd.item_subgroup_id as tax_sub_group_id, max(sstd.tax_rate) as tax_rate, SUM(sstd.tax_amt) as tax_amount
			FROM store_sales_main ssm
			JOIN bill b ON (b.bill_no = ssm.bill_no)
			JOIN (SELECT sssm.sale_id FROM store_sales_main sssm
					JOIN bill bb ON (bb.bill_no = sssm.bill_no) 
					JOIN bill_charge bcc ON (bcc.charge_id = sssm.charge_id)
					LEFT JOIN bill_charge_tax bct ON (bct.charge_id = bcc.charge_id) 
					where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() group by sssm.sale_id having count(bct.tax_sub_group_id) = 0 order by sssm.sale_id) as tc 
			ON (tc.sale_id = ssm.sale_id)
			JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
			JOIN store_sales_tax_details sstd ON (sstd.sale_item_id = ssd.sale_item_id)
		WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
		AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
			(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91')) 
		group by sstd.item_subgroup_id,ssm.sale_id,ssm.charge_id order by charge_id) as foo;

UPDATE bill_charge bc SET act_rate = bc.act_rate - COALESCE((SELECT SUM(ssd.tax) FROM store_sales_details ssd JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) WHERE ssm.charge_id = bc.charge_id), 0)
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.amount != 0 AND bc.tax_amt = 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update bill_charge tax = sum(tax).
UPDATE bill_charge bc SET tax_amt = COALESCE((SELECT sum(ssd.tax) as tax_amount 
	FROM store_sales_details ssd JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) WHERE ssm.charge_id = bc.charge_id), 0)
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.amount != 0 AND bc.tax_amt = 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update bill_charge amount = amount - tax 
UPDATE bill_charge bc SET amount = 
	COALESCE((SELECT (SUM(ssd.amount-ssd.tax) - ssm.discount + ssm.round_off) FROM store_sales_details ssd WHERE ssd.sale_id = ssm.sale_id), 0)
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no) 
where ssm.charge_id = bc.charge_id AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.tax_amt != 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


-- Update bill_charge return_tax_amt = Sum(return_tax_amt)
UPDATE bill_charge bc SET return_tax_amt = COALESCE((SELECT sum(ssd.return_tax_amt) as return_tax_amt 
	FROM store_sales_details ssd JOIN store_sales_main ssm ON (ssd.sale_id = ssm.sale_id AND ssm.charge_id = bc.charge_id)), 0)
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.return_amt != 0 AND bc.return_tax_amt = 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update bill_charge return_amt = return_amt - return_tax_amt 
UPDATE bill_charge bc SET return_amt = 
	(SELECT SUM(ssd.return_amt-ssd.return_tax_amt) FROM store_sales_details ssd WHERE ssd.sale_id = ssm.sale_id)
FROM store_sales_main ssm JOIN bill b ON (ssm.bill_no = b.bill_no) 
where ssm.charge_id = bc.charge_id AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.return_tax_amt != 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update original tax amount and return Original tax amt
UPDATE bill_charge bc SET original_tax_amt = bc.tax_amt, return_original_tax_amt = bc.return_tax_amt
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.amount != 0 AND bc.original_tax_amt = 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') 
AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
	(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


-- Insert bill_charge_claim_tax tax split.
INSERT INTO bill_charge_claim_tax (charge_id, claim_id, tax_sub_group_id, tax_rate, sponsor_tax_amount, charge_tax_id)
	SELECT charge_id, claim_id, tax_sub_group_id, tax_rate, tax_amount, charge_tax_id 
		FROM 
		(SELECT ssm.charge_id, sctd.claim_id, sctd.item_subgroup_id as tax_sub_group_id, max(sctd.tax_rate) as tax_rate, SUM(sctd.tax_amt) as tax_amount, bcht.charge_tax_id
			FROM store_sales_main ssm
			JOIN bill b ON (b.bill_no = ssm.bill_no)
			JOIN (SELECT sssm.sale_id FROM store_sales_main sssm
					JOIN bill bb ON (bb.bill_no = sssm.bill_no) 
					JOIN bill_charge bct ON (bct.charge_id = sssm.charge_id)
					JOIN bill_charge_claim bcc ON (bcc.charge_id = sssm.charge_id)
					LEFT JOIN bill_charge_claim_tax bcct ON (bcct.charge_id = bcc.charge_id AND bcct.claim_id = bcc.claim_id) 
					where bb.status IN ('A', 'F', 'C') AND bb.open_date >= '2017-04-01' AND bb.open_date <= now() AND sssm.type='S' group by sssm.sale_id having count(bcct.tax_sub_group_id) = 0 order by sssm.sale_id) as tc 
			ON (ssm.sale_id = tc.sale_id)
			JOIN store_sales_details ssd ON (ssd.sale_id = tc.sale_id)
			JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id)
			JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id)
			JOIN bill_charge_tax bcht ON (bcht.charge_id = ssm.charge_id AND bcht.tax_sub_group_id = sctd.item_subgroup_id)
		WHERE b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND ssm.type = 'S' AND b.is_tpa = true 
		AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
			(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'))
		group by sctd.item_subgroup_id,sctd.claim_id,ssm.sale_id,ssm.charge_id,bcht.charge_tax_id order by charge_id) as foo;

-- Update bill_charge_claim tax = sum(tax).
UPDATE bill_charge_claim bcc SET tax_amt = 
	COALESCE((SELECT SUM(scd.tax_amt) FROM sales_claim_details scd JOIN store_sales_details ssd ON (ssd.sale_item_id = scd.sale_item_id) 
		JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) WHERE ssm.charge_id = bcc.charge_id AND scd.claim_id = bcc.claim_id), 0)
	FROM bill_charge bc JOIN bill b ON (b.bill_no = bc.bill_no)
	WHERE bcc.charge_id = bc.charge_id AND bcc.tax_amt = 0 AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') AND b.is_tpa = true 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update bill_charge_claim insurance_claim_amt = insurance_claim_amt - Tax.
UPDATE bill_charge_claim bcc SET insurance_claim_amt = 
		COALESCE((SELECT SUM(scd.insurance_claim_amt) FROM sales_claim_details scd JOIN store_sales_details ssd ON (scd.sale_item_id = ssd.sale_item_id) 
			JOIN store_sales_main sssm ON (sssm.sale_id = ssd.sale_id) WHERE sssm.charge_id = bcc.charge_id AND scd.claim_id = bcc.claim_id), 0)
	FROM store_sales_main ssm JOIN bill_charge bc ON (ssm.charge_id = bc.charge_id) JOIN bill b ON (bc.bill_no = b.bill_no) 
	WHERE ssm.charge_id = bcc.charge_id AND bcc.tax_amt != 0 AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') AND b.is_tpa = true 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


-- Update bill_charge insurance_claim_amt = (Sum of multiple sponsor's)
UPDATE bill_charge bc SET insurance_claim_amount = COALESCE((SELECT SUM(insurance_claim_amt) FROM bill_charge_claim bcc WHERE bcc.charge_id = bc.charge_id), 0)
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.amount != 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') AND b.is_tpa = true 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

-- Update bill_charge sponsor_tax_amt = SUM(tax_amt)
UPDATE bill_charge bc SET sponsor_tax_amt = COALESCE((SELECT SUM(tax_amt) FROM bill_charge_claim bcc WHERE bcc.charge_id = bc.charge_id), 0)
FROM bill b where b.bill_no = bc.bill_no AND b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() AND bc.amount != 0 AND bc.tax_amt != 0 AND bc.charge_head IN ('PHCMED', 'PHMED', 'PHRET', 'PHCRET') AND b.is_tpa = true 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));


-- total_claim, total_amount, total_tax, total_claim_tax, total_original_tax_amt
UPDATE bill b SET total_tax = (SELECT COALESCE(SUM(tax_amt),0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN 
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

UPDATE bill b SET total_original_tax_amt = (SELECT COALESCE(SUM(tax_amt),0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

UPDATE bill b SET total_claim_tax = (SELECT COALESCE(SUM(sponsor_tax_amt),0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

UPDATE bill b SET total_amount = (SELECT COALESCE(SUM(amount),0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

UPDATE bill b SET total_claim = (SELECT COALESCE(SUM(insurance_claim_amount),0) FROM bill_charge bc WHERE bc.bill_no = b.bill_no) where b.status IN ('A', 'F', 'C') AND b.open_date >= '2017-04-01' AND b.open_date <= now() 
	AND EXISTS (SELECT * FROM hospital_center_master WHERE center_id = 0 AND country_id IN
		(SELECT country_id FROM country_master WHERE status = 'A' AND country_code = '91'));

ALTER TABLE insurance_claim RENAME obsolete_submission_id_with_correction to submission_id_with_correction;

UPDATE insurance_claim SET submission_id_with_correction = last_submission_batch_id WHERE resubmission_type='correction';

INSERT INTO patient_consultation_field_values (value_id,doc_id,field_id,field_value)

SELECT nextval('patient_consultation_field_values_seq'), doc_id, -1,field_name_values
FROM ( SELECT string_agg(dhtf.field_name || ':-' || pc.field_value, E'\n') as field_name_values, pc.doc_id
FROM patient_consultation_field_values pc
JOIN doc_hvf_template_fields dhtf on (dhtf.field_id = pc.field_id)
GROUP BY doc_id
) as foo
WHERE NOT EXISTS (SELECT 1 FROM patient_consultation_field_values pcf WHERE pcf.doc_id = foo.doc_id AND field_id = -1);
