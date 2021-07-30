--
-- Transaction data clean-up - this script should be run to clean up all transaction data.
-- List of tables which contains transaction data
--
delete from admission;
delete from baby_details;
delete from bed_equipment;
delete from bed_operation_schedule;
delete from bed_shifting;
delete from bill;
delete from bill_activity_charge;
delete from bill_approvals;
delete from bill_charge;
delete from bill_receipts;
delete from dis_detail;
delete from dis_header;
delete from doctor_appointments;
delete from doctor_consultation;
delete from doctor_ot_prescription;
delete from doctor_prescription;
delete from doctor_timing;
delete from doctor_timings;
delete from ip_bed_details;
delete from medical_fileupload;
delete from op_detail;
delete from op_header;
delete from op_prescription_details;
delete from operation_report;
delete from operation_schedule;
delete from ot_activities;
delete from other_charges;
delete from other_services_prescribed;
delete from patient_bed_eqipmentcharges;
delete from patient_details;
delete from patient_insurance_details;
delete from patient_mlc;
delete from patient_package_details;
delete from patient_registration;
delete from patient_visits;

delete from payments;
delete from payments_details;

delete from sample_collection;
delete from services_prescribed;
delete from test_details;
delete from tests_prescribed;
delete from test_visit_reports;
delete from tests_conducted;


delete from audit_logs;
delete from follow_up_details;
delete from incoming_sample_registration_details;
delete from incoming_sample_registration;
delete from outhouse_sample_details;
delete from discharge_format_detail;
delete from insurance_case;
delete from inventory_invoice;
delete from vital_reading;
delete from visit_vitals;

delete from mrd_complaint ;
delete from mrd_diagnosis ;
delete from mrd_treatment ;
delete from mrd_icdcodes ;
delete from patient_general_docs ;
delete from patient_general_images ;
delete from patient_documents ;
delete from service_reports ;
delete from test_report_files ;
delete from test_report_images ;

delete from equipment_prescribed ;
delete from patient_deposits ;
delete from patient_pdf_form_doc_values ;
delete from discharge_fileupload ;

delete from patient_registration_cards;
delete from patient_service_prescriptions;
delete from patient_test_prescriptions;
delete from patient_medicine_prescriptions;

delete from insurance_claim_docs ;
delete from insurance_estimate ;
delete from insurance_preauth;
delete from insurance_preauth_values ;

delete from consultation_token ;

delete from insurance_transaction_attachments ;
delete from insurance_transaction ;
delete from insurance_tpa_docs ;
delete from mrd_casefile_attributes ;
delete from mrd_casefile_issuelog ;

delete from deposit_setoff_total ;
delete from estimate_header ;


delete from asset_maintenance_schedule ;
delete from deposit_setoff_total ;
delete from estimate_header ;

delete from bill_audit_log ;
delete from bill_charge_audit_log ;
delete from bill_activity_charge_audit_log ;
delete from patient_details_audit_log ;
delete from patient_registration_audit_log ;
delete from payments_audit_log ;
delete from payments_details_audit_log ;
delete from sample_collection_audit_log ;
delete from store_stock_details_audit_log ;
delete from test_details_audit_log ;
delete from test_visit_reports_audit_log ;
delete from tests_conducted_audit_log ;
delete from tests_prescribed_audit_log ;

delete from estimate_bill ;
delete from estimate_charge ;
delete from patient_consultation_field_values ;
delete from scheduler_appointments ;

delete from package_prescribed ;
--
-- Reset transaction Sequences
--

SELECT pg_catalog.setval('ip_admission_sequence', 1, false); 
SELECT pg_catalog.setval('ip_operation_sequence', 1, false); 
SELECT pg_catalog.setval('chargeid_sequence', 1, false); 
SELECT pg_catalog.setval('refundno_sequence', 1, false); 
SELECT pg_catalog.setval('approval_id_sequence', 1, false);
SELECT pg_catalog.setval('voucher_sequence', 1, false); 
SELECT pg_catalog.setval('payments_sequence', 1, false); 
SELECT pg_catalog.SETVAL('ip_medicine_sequence', 1, false); 
SELECT pg_catalog.setval('generic_sequence', 1, false); 
SELECT pg_catalog.setval('sampleno_sequence', 1, false); 
SELECT pg_catalog.setval('inhouse_visit_id', 1, false); 
SELECT pg_catalog.setval('HOSPITAL_ID', 1, false); 
SELECT pg_catalog.setval('stockissue_sequence', 1, false); 
SELECT pg_catalog.setval('stockadjust_sequence', 1, false); 
SELECT pg_catalog.setval('grn_id_seq', 1, false); 
SELECT pg_catalog.setval('pharmacy_medicine_sales_main_seq', 1, false); 
SELECT pg_catalog.setval('pharmacy_medicine_sales_seq', 1, false); 
SELECT pg_catalog.setval('retail_customer_id_sequence', 1, false); 
SELECT pg_catalog.setval('ot_activities_sequence', 1, false); 
SELECT pg_catalog.setval('ip_other_services_sequence', 1, false); 
SELECT pg_catalog.setval('bed_operation_sequence', 1, false); 
SELECT pg_catalog.setval('service_prescribed', 1, false); 
SELECT pg_catalog.setval('test_prescribed', 1, false); 
SELECT pg_catalog.setval('bed_prescribed_id_sequence', 1, false);
SELECT pg_catalog.setval('doctor_consultation_sequence', 1, false); 
SELECT pg_catalog.setval('op_header_seq', 1, false); 
SELECT pg_catalog.setval('op_detail_seq', 1, false); 
SELECT pg_catalog.setval('op_prescription_details_seq', 1, false); 
SELECT pg_catalog.setval('dis_header_seq', 1, false);
SELECT pg_catalog.setval('dis_detail_seq', 1, false);
SELECT pg_catalog.setval('insurance_case_seq', 1, false); 
SELECT pg_catalog.setval('insurance_preauth_seq', 1, false); 
SELECT pg_catalog.setval('insurance_transaction_seq', 1, false); 
SELECT pg_catalog.setval('estimate_id_sequence', 1, false); 
SELECT pg_catalog.setval('estimate_chargeid_sequence', 1, false); 
SELECT pg_catalog.setval('insurance_payment_seq', 1,false);
SELECT pg_catalog.setval('stage_status_tracking_seq', 1,false); 
SELECT pg_catalog.setval('supplier_return_sequence', 1, false); 
SELECT pg_catalog.setval('po_id_seq',1,false); 

ALTER SEQUENCE patient_documents_seq RESTART 1 ;
ALTER SEQUENCE patient_general_images_seq RESTART 1 ;
ALTER SEQUENCE patient_general_docs_seq RESTART 1 ;
ALTER SEQUENCE patient_category_master_seq RESTART 1 ;
ALTER SEQUENCE patient_hvf_doc_values_seq RESTART 1 ;
ALTER SEQUENCE patient_medicine_prescriptions_seq RESTART 1 ;
ALTER SEQUENCE patient_service_prescriptions_seq RESTART 1 ;
ALTER SEQUENCE patient_test_prescriptions_seq RESTART 1 ;
ALTER SEQUENCE test_report_files_seq RESTART 1 ;
ALTER SEQUENCE test_report_sequence RESTART 1 ;

ALTER SEQUENCE deposit_collect_sequence RESTART 1 ;
ALTER SEQUENCE deposit_refund_sequence RESTART 1 ;
ALTER SEQUENCE equipment_prescribed_seq RESTART 1 ;
ALTER SEQUENCE discharge_fileupload_seq RESTART 1 ;

ALTER SEQUENCE insurance_tpa_docs_seq RESTART 1 ;
ALTER SEQUENCE insurance_transaction_attachments_seq RESTART 1 ;
ALTER SEQUENCE insurance_transaction_seq RESTART 1 ;

ALTER SEQUENCE mrd_casefile_issue_id_seq RESTART 1 ;

alter SEQUENCE pharmacy_indent_seq RESTART 1 ;
alter SEQUENCE pharmacy_debit_note_seq RESTART 1 ;

alter SEQUENCE asset_maintenance_schedule_seq RESTART 1 ;

alter SEQUENCE appointment_id_sequence RESTART 1 ;
alter SEQUENCE audit_logid_sequence RESTART 1 ;
alter SEQUENCE estimate_bill_sequence RESTART 1 ;
alter SEQUENCE estimate_charge_seq RESTART 1 ;
alter SEQUENCE patient_consultation_field_values_seq RESTART 1 ;
alter SEQUENCE reversal_voucher_sequence RESTART 1 ;
alter SEQUENCE scheduler_appointment_items_seq RESTART 1 ;
alter SEQUENCE visit_vitals_seq RESTART 1 ;

alter SEQUENCE package_prescribed_sequence RESTART 1 ;
-- soft-coded sequences are in hosp_id_patterns, reset all of them
SELECT pg_catalog.setval(sequence_name, 1, false) from hosp_id_patterns;

--- Reset data where needed.
UPDATE bed_names SET occupancy = 'N' ;


---7.1 tables --
DELETE FROM service_documents ;
DELETE FROM operation_documents ;
DELETE FROM fixed_asset_uploads ;
DELETE FROM store_gatepass ;

ALTER SEQUENCE fixed_asset_uploads_seq RESTART 1 ;
ALTER SEQUENCE store_gatepass_seq RESTART 1 ;

