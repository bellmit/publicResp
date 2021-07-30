
----------------Based on rpt_diagnostics_view--------------------------
drop view if exists diag_lab_test_dim CASCADE;

create view diag_lab_test_dim as 
select distinct test_name as test_name 
from  rpt_diagnostics_view;


drop view if exists diag_center_dim CASCADE;

create view diag_center_dim as
select distinct center_name as center_name, 
	dept_category as dept_category,
	ddept_name as ddept_name
from  rpt_diagnostics_view;


drop view if exists diag_conduct_test_status_dim CASCADE;

create view diag_conduct_test_status_dim as
select distinct conducted as conducted
from  rpt_diagnostics_view;


drop view if exists diag_rpt_handedover_dim CASCADE;

create view diag_rpt_handedover_dim as
select distinct handed_over as handed_over
from  rpt_diagnostics_view;

 
drop view if exists diag_patient_type_dim CASCADE;

create view diag_patient_type_dim as
select distinct patient_type as patient_type
from  rpt_diagnostics_view;


drop view if exists diagnostics_view_fact_CD CASCADE;

create view diagnostics_view_fact_CD as
select * 
from rpt_diagnostics_view;



-------------------Based on rpt_diagnostics_revenue_view-----------------------
drop view if exists diagrev_lab_test_dim CASCADE;

create view diagrev_lab_test_dim as 
select distinct test_name  as test_name 
from  rpt_diagnostics_view;



drop view if exists diagrev_center_dim CASCADE;

create view diagrev_center_dim as
select distinct center_name as center_name, 
	admitted_dept_name as admitted_dept_name,
	diag_dept_type as diag_dept_type, 
	treating_dept as treating_dept
from  rpt_diagnostics_revenue_view;



drop view if exists diagrev_act_description_dim CASCADE;

create view diagrev_act_description_dim as
select distinct act_description as act_description
from rpt_diagnostics_revenue_view;



drop view if exists diagrev_pres_doc_dim CASCADE;

create view diagrev_pres_doc_dim as
select distinct pres_doc_name as prescribing_doctor_name
from  rpt_diagnostics_revenue_view;



drop view if exists diagrev_patient_type_dim CASCADE;

create view diagrev_patient_type_dim as
select distinct patient_type_name as patient_type_name
from  rpt_diagnostics_revenue_view;	



drop view if exists diagrev_insurance_dim CASCADE;

create view diagrev_insurance_dim as
select distinct insurance_co_name as insurance_co_name
from  rpt_diagnostics_revenue_view ;	


--- for monthly lab  wise revenue and count and monthly radiology
----test wise reports and diagnostic revenue trend report item ( rpt 7, 8, 9) wise there is  not test anem 
---- or item description in rpt_diagnostics_revenue_view table


drop view if exists diagnostics_revenue_view_fact_OD CASCADE;
	
create view diagnostics_revenue_view_fact_OD as
select * 
from rpt_diagnostics_revenue_view;


drop view if exists diagnostics_revenue_view_fact_FD CASCADE;

create view diagnostics_revenue_view_fact_FD as
select * 
from rpt_diagnostics_revenue_view;


---------------------------- Based on Inventory View------------

drop view if exists STORE_NAME_DIM CASCADE;

CREATE VIEW STORE_NAME_DIM AS
SELECT DISTINCT CENTER_NAME as CENTER_NAME,
	DEPT_NAME as STORE_NAME
FROM rpt_detailed_stock_report_view;

 
drop view if exists CATEGORY_DIM CASCADE;
   
CREATE VIEW CATEGORY_DIM AS
SELECT DISTINCT category_name as category_name
FROM rpt_detailed_stock_report_view;


drop view if exists MEDICINE_CLASS_DIM CASCADE;

CREATE VIEW MEDICINE_CLASS_DIM AS
SELECT DISTINCT classification_name as classification_name,
		sub_classification_name as sub_classification_name
FROM rpt_detailed_stock_report_view;


drop view if exists ITEM_DIM CASCADE;

CREATE VIEW ITEM_DIM AS
SELECT DISTINCT item_name as item_name
FROM rpt_detailed_stock_report_view;


drop view if exists CLAIMABLE_DIM CASCADE;

CREATE VIEW CLAIMABLE_DIM AS
SELECT DISTINCT claimable as claimable
FROM rpt_detailed_stock_report_view;


drop view if exists BILLABLE_DIM CASCADE;

CREATE VIEW BILLABLE_DIM AS
SELECT DISTINCT billable as billable
FROM rpt_detailed_stock_report_view;


drop view if exists CONTROL_TYPE_DIM CASCADE;

CREATE VIEW CONTROL_TYPE_DIM AS
SELECT DISTINCT control_type_name as control_type_name
FROM rpt_detailed_stock_report_view;


drop view if exists IDENTIFICATION_TYPE_DIM CASCADE;

CREATE VIEW IDENTIFICATION_TYPE_DIM AS
SELECT DISTINCT IDENTIFICATION_TYPE as IDENTIFICATION_TYPE
FROM rpt_detailed_stock_report_view;

---- Can reorder level > 0 and stock quantity > 0 be filtered in the tool or create dim
drop view if exists STOCK_REPORT_VIEW_FACT_ED CASCADE;

CREATE VIEW STOCK_REPORT_VIEW_FACT_ED AS
SELECT *
FROM rpt_detailed_stock_report_view;


drop view if exists STOCK_REPORT_VIEW_FACT_SD CASCADE;

CREATE VIEW STOCK_REPORT_VIEW_FACT_SD AS
SELECT *
FROM rpt_detailed_stock_report_view;


--------------------------Based on Revenue View---------------------



drop view if exists  CENTER_DIM CASCADE;

CREATE VIEW CENTER_DIM AS
SELECT DISTINCT NULL as center_name, dept_name as dept_name
FROM rpt_bill_charge_view 
;


drop view if exists CONDUCTING_DOCTOR_DIM CASCADE;

CREATE VIEW CONDUCTING_DOCTOR_DIM AS
SELECT DISTINCT conducting_doctor as conducting_doctor
FROM rpt_bill_charge_view 
;



drop view if exists ADMITTING_DOCTOR_DIM CASCADE;

CREATE VIEW ADMITTING_DOCTOR_DIM AS
SELECT DISTINCT doctor_name  as doctor_name 
FROM rpt_bill_charge_view 
;



drop view if exists REVENUE_DIM CASCADE;

CREATE VIEW REVENUE_DIM AS
SELECT DISTINCT chargegroup_name as chargegroup_name , chargehead_name as chargehead_name
FROM rpt_bill_charge_view 
;


drop view if exists VISIT_TYPE_DIM CASCADE;
 
CREATE VIEW VISIT_TYPE_DIM AS
SELECT DISTINCT Visit_type_name as Visit_type_name
FROM rpt_bill_charge_view 
;


drop view if exists ROOM_DIM CASCADE;

CREATE VIEW ROOM_DIM AS
SELECT DISTINCT bed_type as bed_type
FROM rpt_bill_charge_view
;


drop view if exists SERVICE_GROUP_DIM CASCADE;

CREATE VIEW  SERVICE_GROUP_DIM AS
SELECT DISTINCT service_group_name as service_group_name, service_sub_group_name as service_sub_group_name 
FROM rpt_bill_charge_view 
;



drop view if exists CLAIM_STATUS_DIM CASCADE;
CREATE VIEW  CLAIM_STATUS_DIM AS
SELECT DISTINCT CLAIM_STATUS as CLAIM_STATUS
FROM rpt_bill_charge_view
;



drop view if exists INSURANCE_DIM CASCADE;

CREATE VIEW INSURANCE_DIM AS
SELECT  DISTINCT bill_tpa as bill_tpa, plan_name as plan_name, plan_Type as plan_Type, INSURANCE_CO_NAME as INSURANCE_CO_NAME
FROM rpt_bill_charge_view 
;



drop view if exists PATIENT_DIM CASCADE;

CREATE VIEW PATIENT_DIM AS
SELECT DISTINCT
mr_no,
CASE
	WHEN previous_visit_id != cur_visit_id THEN 'YES'
	ELSE  'NO' end as repeat_visit_flag,
previous_visit_id,
cur_visit_id,
full_name,
patient_status,
patient_gender,
age,
age_in,
first_visit_date,
patient_category_name,
patient_address,
patient_area,
city_name,
state_name,
country_name,
patient_phone,
patient_phone2,
religion,
occupation,
bloodgroup,
vip_status,
family_id,
patient_remarks,
email_id,
oldmrno,
casefile_no,
'NA' as patient_source_category,
passport_no,
passport_validity,
passport_issue_country,
visa_validity,
government_identifier
FROM rpt_bill_charge_view 
;


drop view if exists BILL_CHARGES_FACT CASCADE;

create view BILL_CHARGES_FACT AS
select * , 
(select count(distinct mr_no) from  rpt_bill_charge_view) as total_patients,
(select count(distinct cur_visit_id) from  rpt_bill_charge_view) as total_no_of_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Lab Tests')  as total_no_of_lab_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Radiology Tests') as total_no_of_radiology_visits,
( select count(distinct cur_visit_id || act_description) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Surgeon Fees') as total_no_of_surgeries
from
rpt_bill_charge_view 
;







drop view if exists CENTER_DIM CASCADE;

CREATE VIEW CENTER_DIM AS
SELECT DISTINCT NULL as center_name, dept_name as dept_name
FROM rpt_bill_charge_view 
;



drop view if exists CONDUCTING_DOCTOR_DIM CASCADE;

CREATE VIEW CONDUCTING_DOCTOR_DIM AS
SELECT DISTINCT conducting_doctor as conducting_doctor
FROM rpt_bill_charge_view 
;



drop view if exists ADMITTING_DOCTOR_DIM CASCADE;
 
CREATE VIEW ADMITTING_DOCTOR_DIM AS
SELECT DISTINCT doctor_name as  doctor_name 
FROM rpt_bill_charge_view 
;


drop view if exists PRESCRIBING_DOCTOR_DIM CASCADE;

CREATE VIEW PRESCRIBING_DOCTOR_DIM AS
SELECT DISTINCT pres_doc_name as  PRESCRIBING_DOCTOR
FROM rpt_bill_charge_view 
;




drop view if exists ACT_DESCRIPTION_DIM CASCADE;

CREATE VIEW ACT_DESCRIPTION_DIM AS
SELECT DISTINCT ACT_DESCRIPTION as  ACT_DESCRIPTION
FROM rpt_bill_charge_view 
;


-------------------------------------Based on Revenue Patient View----------


drop view if exists REVENUE_DIM CASCADE;

CREATE VIEW REVENUE_DIM AS
SELECT DISTINCT chargegroup_name as chargegroup_name , chargehead_name as chargehead_name
FROM rpt_bill_charge_view 
;

drop view if exists VISIT_TYPE_DIM CASCADE;

CREATE VIEW VISIT_TYPE_DIM AS
SELECT DISTINCT Visit_type_name as Visit_type_name
FROM rpt_bill_charge_view 
;

drop view if exists ROOM_DIM CASCADE;

CREATE VIEW ROOM_DIM AS
SELECT DISTINCT bed_type as bed_type
FROM rpt_bill_charge_view
;

drop view if exists SERVICE_GROUP_DIM CASCADE;

CREATE VIEW  SERVICE_GROUP_DIM AS
SELECT DISTINCT service_group_name as service_group_name, service_sub_group_name as service_sub_group_name 
FROM rpt_bill_charge_view 
;

drop view if exists CLAIM_STATUS_DIM CASCADE;

CREATE VIEW  CLAIM_STATUS_DIM AS
SELECT DISTINCT CLAIM_STATUS as  CLAIM_STATUS
FROM rpt_bill_charge_view
;

drop view if exists INSURANCE_DIM CASCADE;

CREATE VIEW INSURANCE_DIM AS
SELECT  DISTINCT bill_tpa as bill_tpa, plan_name as plan_name, plan_Type as plan_Type, INSURANCE_CO_NAME as INSURANCE_CO_NAME
FROM rpt_bill_charge_view 
;

drop view if exists Policy_holder_DIM CASCADE;

CREATE VIEW Policy_holder_DIM AS
SELECT  DISTINCT bill_tpa as bill_tpa, plan_name as plan_name, plan_Type as plan_Type, INSURANCE_CO_NAME as INSURANCE_CO_NAME,
policy_holder_name
FROM rpt_bill_charge_view 
;


drop view if exists PATIENT_CATEGORY_DIM CASCADE;

CREATE VIEW PATIENT_CATEGORY_DIM AS
SELECT DISTINCT PATIENT_CATEGORY_NAME as PATIENT_CATEGORY_NAME
FROM rpt_patient_details_view;


drop view if exists RATE_PLAN_DIM CASCADE;

CREATE VIEW RATE_PLAN_DIM AS
SELECT DISTINCT RATE_PLAN as RATE_PLAN
FROM rpt_bill_charge_view;

 
drop view if exists PATIENT_AGE_DIM CASCADE;

CREATE VIEW PATIENT_AGE_DIM AS
SELECT DISTINCT AGE as AGE, AGE_IN as AGE_IN
FROM rpt_patient_details_view;



drop view if exists REPEAT_VISIT_FLAG_DIM CASCADE;

CREATE VIEW REPEAT_VISIT_FLAG_DIM AS
SELECT DISTINCT 
CASE
	WHEN previous_visit_id != cur_visit_id THEN 'YES'
	ELSE  'NO' end  as repeat_visit_flag
FROM rpt_patient_details_view;



drop view if exists PATIENT_AREA_DIM CASCADE;
CREATE VIEW PATIENT_AREA_DIM AS
SELECT DISTINCT COUNTRY_NAME as COUNTRY_NAME,
	STATE_NAME as STATE_NAME,
	CITY_NAME as CITY_NAME,
	PATIENT_AREA as PATIENT_AREA
FROM rpt_patient_details_view;



drop view if exists PATIENT_PP_ISS_COUNTRY CASCADE;
CREATE VIEW PATIENT_PP_ISS_COUNTRY AS
SELECT PASSPORT_ISSUE_COUNTRY
FROM rpt_patient_details_view;




drop view if exists PATIENT_DIM CASCADE;

CREATE VIEW PATIENT_DIM AS
SELECT *
FROM rpt_patient_details_view;



drop view if exists BILL_CHARGE_VIEW_FACT_OD CASCADE;

create view BILL_CHARGE_VIEW_FACT_OD AS
select * , 
CASE
	WHEN previous_visit_id != cur_visit_id THEN 'YES'
	ELSE  'NO' end  as repeat_visit_flag,
(select count(distinct mr_no) from  rpt_bill_charge_view) as total_patients,
(select count(distinct cur_visit_id) from  rpt_bill_charge_view) as total_no_of_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Lab Tests')  as total_no_of_lab_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Radiology Tests') as total_no_of_radiology_visits,
( select count(distinct cur_visit_id || act_description) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Surgeon Fees') as total_no_of_surgeries
from rpt_bill_charge_view 
;


drop view if exists BILL_CHARGE_VIEW_FACT_FD CASCADE;

create view BILL_CHARGE_VIEW_FACT_FD AS
select * , 
CASE
	WHEN previous_visit_id != cur_visit_id THEN 'YES'
	ELSE  'NO' end  as repeat_visit_flag,
(select count(distinct mr_no) from  rpt_bill_charge_view) as total_patients,
(select count(distinct cur_visit_id) from  rpt_bill_charge_view) as total_no_of_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Lab Tests')  as total_no_of_lab_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Radiology Tests') as total_no_of_radiology_visits,
( select count(distinct cur_visit_id || act_description) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Surgeon Fees') as total_no_of_surgeries
from rpt_bill_charge_view 
;


drop view if exists BILL_CHARGE_VIEW_FACT_CD CASCADE;

create view BILL_CHARGE_VIEW_FACT_CD AS
select * , 
CASE
	WHEN previous_visit_id != cur_visit_id THEN 'YES'
	ELSE  'NO' end  as repeat_visit_flag,
(select count(distinct mr_no) from  rpt_bill_charge_view) as total_patients,
(select count(distinct cur_visit_id) from  rpt_bill_charge_view) as total_no_of_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Lab Tests')  as total_no_of_lab_visits,
(select count(distinct cur_visit_id) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Radiology Tests') as total_no_of_radiology_visits,
( select count(distinct cur_visit_id || act_description) 
 from  rpt_bill_charge_view 
 where amount > 0 and chargehead_name = 'Surgeon Fees') as total_no_of_surgeries
from rpt_bill_charge_view 
;
