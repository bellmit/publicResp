-- liquibase formatted sql
-- changeset raj-nt:audit-log-triggers splitStatements:false runAlways:true 
-- validCheckSum: ANY

-- A common trigger for capturing audit logs on any table.

-- The trigger accepts the following four parameters in the order indicated:

-- audit_log_table : Required. Name of the audit log table into which records will be inserted.
--          Typically there is one audit log table for each table that needs to be audited

-- user_name_field : Required. Name of the field, in the base table which records the application user
--          who is creating / modifying the record. Every table that needs auditing should
--          support a field to capture the user. The name of such a field should be supplied as
--          argument to the trigger

-- audit_field_list : Optional. This is a comma separated list of fields, that need to be (or need not be) tracked
--          as part of the audit trail. If this parameter is not supplied, all fields will be included.

-- incl_excl    : Optional. This indicates whether the audit_field_list is an include list or an exclude list.
--          0 indicates exclusion, 1 indicates inclusion. If this parameter is not specified, the
--          audit_field_list will be treated as inclusion list.

--
-- Note: As a convention all audit log trigger names start with z_ to make sure they are the last one to run
-- in the chain of triggers.

CREATE OR REPLACE FUNCTION audit_log_trigger() RETURNS trigger AS $BODY$
DECLARE
  operation varchar;
  username_array varchar[];
  username varchar;
  custom_fields varchar;
  custom_field_values varchar;
  str_value varchar;
  audit_table_name varchar;
  username_field varchar;
  audit_field_list varchar[];
  incl_excl smallint; 
  rec record;
  new_value varchar;
  old_value varchar;
  field_name varchar;
  audit_entries text[];
  loggable_fields text[];
  row_data json;
  old_row_data json;
  new_row_data json;
BEGIN
  audit_table_name := TG_ARGV[0];
  username_field := TG_ARGV[1];
  audit_field_list := TG_ARGV[2];
  incl_excl := TG_ARGV[3];
  IF (TG_OP <> 'INSERT') THEN
    old_row_data := row_to_json(OLD);
  END IF;
  IF (TG_OP = 'DELETE') THEN
    row_data := old_row_data;
  ELSE
    new_row_data := row_to_json(NEW);
    row_data := new_row_data;
    username :=  new_row_data->>username_field;
  END IF;
  IF (username is NULL OR trim(username) = '') THEN
    username := current_setting('application.username');
  END IF;
  operation := TG_OP;
  -- Handle custom operation (bulk inserts and updates)
  -- Split the user name on a colon.
  username_array := string_to_array(username, ':');

  -- If the user name contains an operation hint, separate the two
  -- Strip the operation hint from the user name and set the operation to
  -- the supplied hint.
  IF (array_length(username_array,1) > 1) THEN
    username := trim(username_array[1]);
    operation := CASE trim(username_array[2]) 
      WHEN 'XLS' THEN 'XL IMPORT' WHEN 'CSV' THEN 'CSV IMPORT' WHEN 'GUP' THEN 'GROUP UPDATE' END;
    IF (username_field = 'user_name') THEN
      NEW.user_name = username;
    ELSIF (username_field = 'username') THEN
      NEW.username = username;
    ELSIF (username_field = 'mod_user') THEN
      NEW.mod_user = username;
    ELSIF (username_field = 'mod_username') THEN
      NEW.mod_username = username;
    ELSIF (username_field = 'last_modified_by') THEN
      NEW.last_modified_by = username;
    ELSIF (username_field = 'created_by') THEN
      NEW.created_by = username;
    ELSIF (username_field = 'modified_by') THEN
      NEW.modified_by = username;
    ELSIF (username_field = 'changed_by') THEN
      NEW.changed_by = username;
    ELSIF (username_field = 'mod_user') THEN
      NEW.mod_user = username;
    END IF;
  END IF;
  custom_fields := '';
  custom_field_values := '';
  -- get the fields specific to this audit log table, mostly key fields of the base table apart for the common ones
  FOR rec in 
    select a.attname as column_name, 
      pg_catalog.format_type(a.atttypid, NULL) as data_type 
      from pg_catalog.pg_attribute a, pg_catalog.pg_class c, pg_catalog.pg_namespace n where 
    a.attnum > 0 and a.attisdropped = false and 
    a.attrelid = c.oid and c.relnamespace = n.oid and 
    c.relname = audit_table_name and n.nspname = current_schema() and 
    a.attname not in ('log_id', 'user_name', 'mod_time', 'operation', 'field_name', 'old_value', 'new_value')
  LOOP
    custom_fields := custom_fields || rec.column_name || ',';
    IF rec.data_type IN ('integer','numeric') THEN
      str_value := COALESCE(row_data->>rec.column_name,'NULL');
      custom_field_values := custom_field_values || str_value  || ', ';
    ELSE
      str_value := replace(trim(COALESCE(row_data->>rec.column_name,''),'"'),'''','''''');
      custom_field_values := custom_field_values || '''' || str_value  || ''', ';
    END IF;
  END LOOP;
  IF TG_OP = 'UPDATE' THEN
    IF (incl_excl = 0) THEN
      --- Find all the columns where data has changed for update trigger and discard excluded columns 
      SELECT ARRAY(SELECT o.key AS columname
        FROM json_each_text(old_row_data) AS o 
        JOIN json_each_text(new_row_data) AS n 
          ON o.key = n.key
          AND o.value IS DISTINCT FROM n.value
          AND o.key != ALL(audit_field_list)) into loggable_fields;
    ELSE
      --- Find all the columns where data has changed for update trigger and consider only included columns 
      SELECT ARRAY(SELECT o.key AS columname
        FROM json_each_text(old_row_data) AS o 
        JOIN json_each_text(new_row_data) AS n 
          ON o.key = n.key
          AND o.value IS DISTINCT FROM n.value
          AND o.key = ANY(audit_field_list)) into loggable_fields;
    END IF;
  ELSE
    -- scan all keys for delete and insert cases (applying exclusion or inclusion filter)
    IF (incl_excl = 0) THEN
      SELECT ARRAY(
        SELECT keys from (SELECT json_object_keys(row_data) as keys) _tmp_fields
           WHERE keys != ALL(audit_field_list)
      ) into loggable_fields;
    ELSE
      SELECT ARRAY(
        SELECT keys from (SELECT json_object_keys(row_data) as keys) _tmp_fields
           WHERE keys = ANY(audit_field_list)
      ) into loggable_fields;
    END IF;
  END IF;
  FOREACH field_name in ARRAY loggable_fields LOOP
    old_value := CASE TG_OP 
      WHEN 'INSERT' THEN '' ELSE replace(trim(COALESCE(old_row_data->>field_name,''),'"'),'''','''''') END;
    new_value := CASE TG_OP 
      WHEN 'DELETE' THEN 'DELETED' ELSE replace(trim(COALESCE(new_row_data->>field_name,''),'"'),'''','''''') END;
    audit_entries := array_append(audit_entries, 
      '(' || custom_field_values || '''' || username || ''',''' || operation || ''',''' || field_name || ''','''
       || old_value || ''',''' || new_value || ''')');
  END LOOP;
  IF (trim(array_to_string(audit_entries, ',')) != '') THEN
    EXECUTE ' INSERT INTO ' || audit_table_name || ' (' || custom_fields || 
    'user_name,operation,field_name,old_value,new_value) ' || ' VALUES ' || array_to_string(audit_entries, ',');
  END IF;
  IF (TG_OP = 'DELETE') THEN
    return OLD;
  ELSE
    return NEW;
  END IF;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;

-- Audit log trigger for Billing tables : bill, bill_charge, bill_activity_charge
DROP TRIGGER IF EXISTS z_bill_audit_trigger ON bill;
CREATE TRIGGER z_bill_audit_trigger
  BEFORE INSERT OR UPDATE ON bill FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'bill_audit_log',
    'username', 
    '{"username","app_modified","visit_type","opened_by","mod_time","restriction_type","account_group","total_amount","total_discount","total_claim","total_receipts","no_of_receipts","last_receipt_no","last_sponsor_receipt_no"}',
    0);

DROP TRIGGER IF EXISTS z_bill_charge_audit_trigger ON bill_charge;
CREATE TRIGGER z_bill_charge_audit_trigger
  BEFORE INSERT OR UPDATE ON bill_charge FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'bill_charge_audit_log',
    'username',
    '{"username","charge_group","charge_head","act_department_id","act_description","orig_rate","charge_ref","paid_amount","mod_time","package_unit","act_description_id","activity_conducted","account_group","order_number","allow_discount","conducted_datetime","doctor_amount","referal_amount","out_house_amount","doc_payment_id","ref_payment_id","oh_payment_id","hasactivity","code_type","service_sub_group_id","conducting_doc_mandatory","consultation_type_id"}',
    0);

-- Audit log triggers for registration tables : patient_registration, patient_details

DROP TRIGGER IF EXISTS z_patient_registration_audit_trigger ON patient_registration;
CREATE TRIGGER z_patient_registration_audit_trigger
  BEFORE INSERT OR UPDATE ON patient_registration FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_registration_audit_log',
    'user_name', 
    '{"user_name","cflag","doc_id","ward_name","discharge_format","discharge_doc_id","insurance_id","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_details_audit_trigger ON patient_details;
CREATE TRIGGER z_patient_details_audit_trigger
  BEFORE INSERT OR UPDATE ON patient_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_details_audit_log',
    'user_name',
    '{"user_name,patient_photo,cflag"}',
    0);

-- Audit log triggers for store stock tables: store_stock_details, store_item_batch_details

DROP TRIGGER IF EXISTS z_store_stock_details_audit_trigger ON store_stock_details;
CREATE TRIGGER z_store_stock_details_audit_trigger
  BEFORE INSERT OR UPDATE ON store_stock_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'store_stock_details_audit_log',
    'username',
    '{"exp_dt","mrp","package_sp","tax_rate","package_cp","bin","tax_type","asset_approved","item_supplier_name","item_supplier_code","stock_pkg_size","username"}',
    1);

DROP TRIGGER IF EXISTS z_store_item_batch_details_audit_trigger ON store_item_batch_details;
CREATE TRIGGER z_store_item_batch_details_audit_trigger
  BEFORE INSERT OR UPDATE ON store_item_batch_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'store_item_batch_details_audit_log',
    'username',
    '{"exp_dt","mrp","username"}',
    1);

-- Audit log triggers for diagnostic test tables: test_details, tests_prescribed, tests_conducted, test_visit_reports, sample_collection

DROP TRIGGER IF EXISTS z_tests_prescribed_audit_trigger ON tests_prescribed;
CREATE TRIGGER z_tests_prescribed_audit_trigger
  BEFORE INSERT OR UPDATE ON tests_prescribed FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'tests_prescribed_audit_log',
    'user_name',
    '{"user_name"}',
    0);

DROP TRIGGER IF EXISTS z_test_details_audit_trigger ON test_details;
CREATE TRIGGER z_test_details_audit_trigger
  BEFORE INSERT OR UPDATE ON test_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'test_details_audit_log',
    'user_name',
    '{"filecontent","user_name","patient_report_file"}',
    0);

DROP TRIGGER IF EXISTS z_tests_conducted_audit_trigger ON tests_conducted;
CREATE TRIGGER z_tests_conducted_audit_trigger
  BEFORE INSERT OR UPDATE ON tests_conducted FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'tests_conducted_audit_log',
    'user_name',
    '{"user_name"}',
    0);

DROP TRIGGER IF EXISTS z_test_visit_reports_audit_trigger ON test_visit_reports;
CREATE TRIGGER z_test_visit_reports_audit_trigger
  BEFORE INSERT OR UPDATE ON test_visit_reports FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'test_visit_reports_audit_log',
    'user_name',
    '{"report_data","user_name"}',
    0);

DROP TRIGGER IF EXISTS z_sample_collection_audit_trigger ON sample_collection;
CREATE TRIGGER z_sample_collection_audit_trigger
  BEFORE INSERT OR UPDATE ON sample_collection FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'sample_collection_audit_log',
    'user_name',
    '{"user_name"}',
    0);

DROP TRIGGER IF EXISTS z_payments_audit_trigger ON payments;
CREATE TRIGGER z_payments_audit_trigger
  BEFORE INSERT OR UPDATE ON payments FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'payments_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_payments_details_audit_trigger ON payments_details;
CREATE TRIGGER z_payments_details_audit_trigger
  BEFORE INSERT OR UPDATE ON payments_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'payments_details_audit_log',
    'username',
    '{"username"}',
    0);

DROP TRIGGER IF EXISTS z_paymentsrule_audit_trigger ON payment_rules;
CREATE TRIGGER z_paymentsrule_audit_trigger
  BEFORE INSERT OR UPDATE OR DELETE ON payment_rules FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'payment_rules_audit_log',
    'username',
    '{"doctor_category","referrer_category","rate_plan","activity_id","charge_head","prescribed_category","username"}',
    0);

DROP TRIGGER IF EXISTS z_operation_master_audit_trigger ON operation_master;
CREATE TRIGGER z_operation_master_audit_trigger
  BEFORE INSERT OR UPDATE ON operation_master FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'operation_master_audit_log',
    'username',
    '{"op_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_operation_charges_audit_trigger ON operation_charges;
CREATE TRIGGER z_operation_charges_audit_trigger
  BEFORE INSERT OR UPDATE ON operation_charges FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'operation_charges_audit_log',
    'username',
    '{"op_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_services_master_audit_trigger ON services;
CREATE TRIGGER z_services_master_audit_trigger
  BEFORE INSERT OR UPDATE ON services FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'services_audit_log',
    'username',
    '{"service_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_services_charges_audit_trigger ON service_master_charges;
CREATE TRIGGER z_services_charges_audit_trigger
  BEFORE INSERT OR UPDATE ON service_master_charges FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'service_master_charges_audit_log',
    'username',
    '{"service_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_diagnostictest_master_audit_trigger ON diagnostics;
CREATE TRIGGER z_diagnostictest_master_audit_trigger
  BEFORE INSERT OR UPDATE ON diagnostics FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'diagnostics_audit_log',
    'username',
    '{"test_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_diagnostictest_charges_audit_trigger ON diagnostic_charges;
CREATE TRIGGER z_diagnostictest_charges_audit_trigger
  BEFORE INSERT OR UPDATE ON diagnostic_charges FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'diagnostic_charges_audit_log',
    'username',
    '{"test_id","username"}',
    0);

-- Trigger: dyna_package_master_audit_trigger on dyna_packages

DROP TRIGGER IF EXISTS z_dyna_package_master_audit_trigger ON dyna_packages;
CREATE TRIGGER z_dyna_package_master_audit_trigger
  BEFORE INSERT OR UPDATE ON dyna_packages FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dyna_package_audit_log',
    'username',
    '{"dyna_package_id","username"}',
    0);

-- Trigger: dyna_package_charges_audit_trigger on dyna_package_master_charges --

DROP TRIGGER IF EXISTS z_dyna_package_charges_audit_trigger ON dyna_package_charges;
CREATE TRIGGER z_dyna_package_charges_audit_trigger
  BEFORE INSERT OR UPDATE ON dyna_package_charges FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dyna_package_charges_audit_log',
    'username',
    '{"dyna_package_id","username"}',
    0);

-- Trigger: dyna_package_category_limits_audit_trigger on dyna_package_category_limits --

DROP TRIGGER IF EXISTS z_dyna_package_category_limits_audit_trigger ON dyna_package_category_limits;
CREATE TRIGGER z_dyna_package_category_limits_audit_trigger
  BEFORE INSERT OR UPDATE ON dyna_package_category_limits FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dyna_package_category_limits_audit_log',
    'username',
    '{"dyna_package_id","dyna_pkg_cat_id","username"}',
    0);

-- Trigger: scheduler_appointments_audit_trigger on scheduler_appointments

DROP TRIGGER IF EXISTS z_scheduler_appointments_audit_trigger ON scheduler_appointments;
CREATE TRIGGER z_scheduler_appointments_audit_trigger
  BEFORE INSERT OR UPDATE ON scheduler_appointments FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'scheduler_appointments_audit_log',
    'changed_by',
    '{"appointment_id","res_sch_id","res_sch_name","visit_id","changed_by"}',
    0);

-- Trigger: scheduler_appointments_audit_trigger on scheduler_appointments

DROP TRIGGER IF EXISTS z_scheduler_appointment_items_audit_trigger ON scheduler_appointment_items;
CREATE TRIGGER z_scheduler_appointment_items_audit_trigger
  BEFORE INSERT OR UPDATE ON scheduler_appointment_items FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'scheduler_appointment_items_audit_log',
    'user_name',
    '{"resource_id","user_name"}',
    1);

  -- Trigger: bill_receipts_audit_trigger on bill_receipts

DROP TRIGGER IF EXISTS z_bill_receipts_audit_trigger ON bill_receipts;
CREATE TRIGGER z_bill_receipts_audit_trigger
  BEFORE INSERT OR UPDATE ON bill_receipts FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'bill_receipts_audit_log',
    'username',
    '{"receipt_no","username"}',
    0);
  
-- Trigger: receipts_audit_trigger on receipts

DROP TRIGGER IF EXISTS z_receipts_audit_trigger ON receipts;
CREATE TRIGGER z_receipts_audit_trigger
  BEFORE INSERT OR UPDATE ON receipts FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'receipts_audit_log',
    'modified_by',
    '{"modified_by","receipt_id"}',
    0);

-- Trigger: z_dialysis_prescriptions_audit_trigger on dialysis_prescriptions

DROP TRIGGER IF EXISTS z_dialysis_prescriptions_audit_trigger ON dialysis_prescriptions;
CREATE TRIGGER z_dialysis_prescriptions_audit_trigger
  BEFORE INSERT OR UPDATE ON dialysis_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dialysis_prescriptions_audit_log',
    'username',
    '{"username"}',
    0);

DROP TRIGGER IF EXISTS z_temporary_access_types_audit_trigger ON temporary_access_types;
CREATE TRIGGER z_temporary_access_types_audit_trigger
  BEFORE INSERT OR UPDATE ON temporary_access_types FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dialysis_prescriptions_audit_log',
    'username',
    '{"mr_no","dialysis_presc_id","temporary_access_type_id","username"}',
    0);

DROP TRIGGER IF EXISTS z_permanent_access_types_audit_trigger ON permanent_access_types;
CREATE TRIGGER z_permanent_access_types_audit_trigger
  BEFORE INSERT OR UPDATE ON permanent_access_types FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'dialysis_prescriptions_audit_log',
    'username',
    '{"mr_no","dialysis_presc_id","permanent_access_type_id","username"}',
    0);

-- Trigger: patient_general_docs_audit_log_trigger on patient_general_docs_audit_log

DROP TRIGGER IF EXISTS z_patient_general_docs_audit_log_trigger ON patient_general_docs;
CREATE TRIGGER z_patient_general_docs_audit_log_trigger
  BEFORE INSERT OR UPDATE ON patient_general_docs FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_general_docs_audit_log',
    'username',
    '{"username"}',
    0);

DROP TRIGGER IF EXISTS z_patient_pdf_form_doc_values_audit_log_trigger ON patient_pdf_form_doc_values;
CREATE TRIGGER z_patient_pdf_form_doc_values_audit_log_trigger
  BEFORE INSERT OR UPDATE ON patient_pdf_form_doc_values FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_pdf_form_doc_values_audit_log',
    'username',
    '{"username"}',
    0);

DROP TRIGGER IF EXISTS z_patient_activities_audit_trigger ON patient_activities;
CREATE TRIGGER z_patient_activities_audit_trigger
  BEFORE INSERT OR UPDATE ON patient_activities FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_activities_audit_log',
    'username',
    '{"username"}',
    0);


DROP TRIGGER IF EXISTS z_doctor_notes_trigger ON ip_doctor_notes;
CREATE TRIGGER z_doctor_notes_trigger
  BEFORE INSERT OR UPDATE ON ip_doctor_notes FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'doctor_notes_audit_log',
    'mod_user',
    '{"patient_id","note_num","creation_datetime","notes","billable_consultation","consultation_type_id","doctor_id","finalized","highlighted","mod_user"}',
    1);


DROP TRIGGER IF EXISTS z_nurse_notes_trigger ON ip_nurse_notes;
CREATE TRIGGER z_nurse_notes_trigger
  BEFORE INSERT OR UPDATE ON ip_nurse_notes FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'nurse_notes_audit_log',
    'mod_user',
    '{"patient_id","note_num","creation_datetime","notes","finalized","note_type","mod_user"}',
    1);


DROP TRIGGER IF EXISTS z_secondary_complaints_audit_trigger ON secondary_complaints;
CREATE TRIGGER z_secondary_complaints_audit_trigger
  BEFORE INSERT OR UPDATE ON secondary_complaints FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'secondary_complaints_audit_log',
    'username',
    '{"username","visit_id","row_id","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_mrd_diagnosis_audit_trigger ON mrd_diagnosis;
CREATE TRIGGER z_mrd_diagnosis_audit_trigger
  BEFORE INSERT OR UPDATE ON mrd_diagnosis FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'mrd_diagnosis_audit_log',
    'username',
    '{"username","mod_time","visit_id","sent_for_approval"}', 
    0);

DROP TRIGGER IF EXISTS z_mrd_diagnosis_adm_request_trigger ON patient_admission_request;
CREATE TRIGGER z_mrd_diagnosis_adm_request_trigger
  BEFORE INSERT OR UPDATE ON patient_admission_request FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_admission_request_audit_log',
    'user_name',
    '{"user_name","mod_time","adm_request_id"}', 
    0);

DROP TRIGGER IF EXISTS z_visit_vitals_audit_trigger ON visit_vitals;
CREATE TRIGGER z_visit_vitals_audit_trigger
  BEFORE INSERT OR UPDATE ON visit_vitals FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'visit_vitals_audit_log',
    'user_name',
    '{"user_name","vital_reading_id","vital_status"}',
    0);

DROP TRIGGER IF EXISTS z_vital_reading_audit_trigger ON vital_reading;
CREATE TRIGGER z_vital_reading_audit_trigger
  BEFORE INSERT OR UPDATE ON vital_reading FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'vital_reading_audit_log',
    'username',
    '{"username","param_id","mod_time"}',
    0);

-- Trigger: per_diem_codes_master_audit_trigger on per_diem_codes_master

DROP TRIGGER IF EXISTS z_per_diem_codes_master_audit_trigger ON per_diem_codes_master;
CREATE TRIGGER z_per_diem_codes_master_audit_trigger
  BEFORE INSERT OR UPDATE ON per_diem_codes_master FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'per_diem_codes_audit_log',
    'username',
    '{"per_diem_code","username"}',
    0);

-- Trigger: per_diem_codes_charges_audit_trigger on per_diem_codes_charges --

DROP TRIGGER IF EXISTS z_per_diem_codes_charges_audit_trigger ON per_diem_codes_charges;
CREATE TRIGGER z_per_diem_codes_charges_audit_trigger
  BEFORE INSERT OR UPDATE ON per_diem_codes_charges FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'per_diem_codes_charges_audit_log',
    'username',
    '{"per_diem_code","username"}', 
    0);

DROP TRIGGER IF EXISTS z_patient_allergies_trigger ON patient_allergies;
CREATE TRIGGER z_patient_allergies_trigger
  BEFORE INSERT OR UPDATE ON patient_allergies FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_allergies_audit_log',
    'username',
    '{"username","mod_time","section_detail_id"}',
    0);


DROP TRIGGER IF EXISTS z_insurance_plan_main_trigger ON insurance_plan_main;
CREATE TRIGGER z_insurance_plan_main_trigger
  BEFORE INSERT OR UPDATE ON insurance_plan_main FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'insurance_plan_main_audit_log',
    'username',
    '{"plan_id","username","mod_time"}',
    0);


DROP TRIGGER IF EXISTS z_insurance_plan_details_trigger ON insurance_plan_details;
CREATE TRIGGER z_insurance_plan_details_trigger
  BEFORE INSERT OR UPDATE ON insurance_plan_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'insurance_plan_details_audit_log',
    'username',
    '{"plan_id","username"}',
    0);
  
  
-- Audit log trigger for Store PO tables : store_po_main
DROP TRIGGER IF EXISTS z_store_po_main_audit_trigger ON store_po_main;
CREATE TRIGGER z_store_po_main_audit_trigger
  BEFORE INSERT OR UPDATE OR DELETE ON store_po_main FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'store_po_main_audit_log',
    'last_modified_by', 
    '{"po_no","po_date","qut_no","qut_date","supplier_id","reference","vat_rate","po_total","status","supplier_terms","hospital_terms","actual_po_date","user_id","vat_type","mrp_type","store_id","approved_by","approved_time","approver_remarks","closure_reasons","credit_period","delivery_date","round_off","discount_type","discount_per","discount","po_qty_unit","remarks","dept_id","delivery_instructions","enq_no","enq_date","validated_by","validated_time","validator_remarks","po_alloted_to","amended_reason","amendment_time","amendment_validated_time","amendment_approved_time","amended_by","amendment_validated_by","amendment_approved_by","amendment_validator_remarks","amendment_approver_remarks","cancelled_by","quotation_file_name","transportation_charges","last_modified_by"}',
    1);


DROP TRIGGER IF EXISTS z_vital_reading_audit_delete_trigger ON vital_reading;
CREATE TRIGGER z_vital_reading_audit_delete_trigger
  BEFORE DELETE ON vital_reading FOR EACH ROW EXECUTE PROCEDURE vital_reading_delete_trigger_func();

  
DROP TRIGGER IF EXISTS z_system_preferences_audit_trigger ON generic_preferences;  
CREATE TRIGGER z_system_preferences_audit_trigger 
  BEFORE UPDATE ON generic_preferences FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'system_preferences_audit_log',
    'mod_username',
    '{"mod_username"}',
    0);

DROP TRIGGER IF EXISTS z_patient_section_details_trigger ON patient_section_details;
CREATE TRIGGER z_patient_section_details_trigger
    
  BEFORE INSERT OR UPDATE ON patient_section_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_section_details_audit_log',
    'user_name', 
    '{"section_item_id","item_type","mr_no","patient_id","section_status","generic_form_id","mod_time","user_name","finalized_user"}',
    0);

DROP TRIGGER IF EXISTS z_patient_form_details_trigger ON patient_form_details;
CREATE TRIGGER z_patient_form_details_trigger
  BEFORE INSERT OR UPDATE ON patient_form_details FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_form_details_audit_log',
    'user_name',
    '{"reopen_remarks","user_name", "mod_time", "form_status"}',
    1);

DROP TRIGGER IF EXISTS z_patient_section_fields_trigger ON patient_section_fields;
CREATE TRIGGER z_patient_section_fields_trigger
  BEFORE INSERT OR UPDATE ON patient_section_fields FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_section_fields_audit_log',
    'user_name',
    '{"mod_time","field_detail_id","user_name"}',
    0);

DROP TRIGGER IF EXISTS z_patient_section_options_trigger ON patient_section_options;
CREATE TRIGGER z_patient_section_options_trigger
  BEFORE INSERT OR UPDATE ON patient_section_options FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_section_options_audit_log',
    'user_name',
    '{"option_id","option_detail_id","field_detail_id","mod_time","user_name"}',
    0);


DROP TRIGGER IF EXISTS z_patient_allergies_delete_trigger ON patient_allergies;
CREATE TRIGGER z_patient_allergies_delete_trigger
  BEFORE DELETE ON patient_allergies FOR EACH ROW EXECUTE PROCEDURE patient_allergies_delete_trigger_func();

DROP TRIGGER IF EXISTS z_mrd_diagnosis_audit_delete_trigger ON mrd_diagnosis;
CREATE TRIGGER z_mrd_diagnosis_audit_delete_trigger
  BEFORE DELETE ON mrd_diagnosis FOR EACH ROW EXECUTE PROCEDURE mrd_diagnosis_delete_trigger_func();

DROP TRIGGER IF EXISTS z_secondary_complaints_audit_delete_trigger ON secondary_complaints;
CREATE TRIGGER z_secondary_complaints_audit_delete_trigger
  BEFORE DELETE ON secondary_complaints FOR EACH ROW EXECUTE PROCEDURE secondary_complaints_delete_trigger_func();

DROP TRIGGER IF EXISTS z_patient_prescription_trigger ON patient_prescription;
CREATE TRIGGER z_patient_prescription_trigger
  BEFORE INSERT OR UPDATE ON patient_prescription FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_prescription_audit_log',
    'username',
    '{"username","mr_no","presc_type","patient_presc_id"}',
    0);

DROP TRIGGER IF EXISTS z_patient_prescription_delete_trigger ON patient_prescription;
CREATE TRIGGER z_patient_prescription_delete_trigger
  BEFORE DELETE ON patient_prescription FOR EACH ROW EXECUTE PROCEDURE patient_prescription_delete_trigger_func();

DROP TRIGGER IF EXISTS z_patient_test_prescriptions_trigger ON patient_test_prescriptions;
CREATE TRIGGER z_patient_test_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_test_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_test_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_service_prescriptions_trigger ON patient_service_prescriptions;
CREATE TRIGGER z_patient_service_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_service_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_service_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_consultation_prescriptions_trigger ON patient_consultation_prescriptions;
CREATE TRIGGER z_patient_consultation_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_consultation_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_consultation_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_operation_prescriptions_trigger ON patient_operation_prescriptions;
CREATE TRIGGER z_patient_operation_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_operation_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_operation_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_other_prescriptions_trigger ON patient_other_prescriptions;
CREATE TRIGGER z_patient_other_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_other_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_other_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_other_medicine_prescriptions_trigger ON patient_other_medicine_prescriptions;
CREATE TRIGGER z_patient_other_medicine_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_other_medicine_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_other_medicine_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_patient_medicine_prescriptions_trigger ON patient_medicine_prescriptions;
CREATE TRIGGER z_patient_medicine_prescriptions_trigger
  BEFORE INSERT OR UPDATE ON patient_medicine_prescriptions FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_medicine_prescriptions_audit_log',
    'username',
    '{"username","mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_doctor_consultation_trigger ON doctor_consultation;
CREATE TRIGGER z_doctor_consultation_trigger
  BEFORE INSERT OR UPDATE ON doctor_consultation FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'doctor_consultation_audit_log',
    'username',
    '{"reopen_remarks","status","consultation_complete_time","username"}',
    1);

DROP TRIGGER IF EXISTS z_clinical_preferences_trigger ON clinical_preferences;
CREATE TRIGGER z_clinical_preferences_trigger
  BEFORE UPDATE ON clinical_preferences FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'clinical_preferences_audit_log',
    'username',
    '{"username"}',
    0);
   
DROP TRIGGER IF EXISTS z_patient_notes_trigger ON patient_notes;
CREATE TRIGGER z_patient_notes_trigger
  BEFORE INSERT OR UPDATE ON patient_notes FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'patient_notes_audit_log',
    'mod_user',
    '{"mod_user","note_content"}',
    0);

DROP TRIGGER IF EXISTS z_doctor_consultation_triage_trigger ON doctor_consultation;
CREATE TRIGGER z_doctor_consultation_triage_trigger
  BEFORE INSERT OR UPDATE ON doctor_consultation FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'doctor_consultation_triage_audit_log',
    'username',
    '{"reopen_remarks_triage","triage_done","triage_complete_time","username"}',
    1);

DROP TRIGGER IF EXISTS z_physical_stock_take_trigger ON physical_stock_take;
CREATE TRIGGER z_physical_stock_take_trigger
  BEFORE INSERT OR UPDATE ON physical_stock_take FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'physical_stock_take_audit_log',
    'user_name',
    '{"user_name", "mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_physical_stock_take_detail_trigger ON physical_stock_take_detail;
CREATE TRIGGER z_physical_stock_take_detail_trigger
  BEFORE INSERT OR UPDATE ON physical_stock_take_detail FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'physical_stock_take_detail_audit_log',
    'user_name',
    '{"user_name", "mod_time"}',
    0);

DROP TRIGGER IF EXISTS z_doctor_consultation_ia_trigger ON doctor_consultation;
CREATE TRIGGER z_doctor_consultation_ia_trigger
  BEFORE INSERT OR UPDATE ON doctor_consultation FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'doctor_consultation_ia_audit_log',
    'username',
    '{"reopen_remarks_ia","initial_assessment_status","ia_complete_time","username"}',
    1);

DROP TRIGGER IF EXISTS z_salucro_location_mapping_trigger ON salucro_location_mapping;
CREATE TRIGGER z_salucro_location_mapping_trigger
  BEFORE INSERT OR UPDATE ON salucro_location_mapping FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'salucro_location_mapping_audit_log',
    'name',
    '{"name","status","counter_id","center_id"}',
    1);

DROP TRIGGER IF EXISTS z_salucro_role_mapping_trigger ON salucro_role_mapping;
CREATE TRIGGER z_salucro_role_mapping_trigger
  BEFORE INSERT OR UPDATE ON salucro_role_mapping FOR EACH ROW EXECUTE PROCEDURE audit_log_trigger(
    'salucro_role_mapping_audit_log',
    'emp_username',
    '{"emp_username","role","status","center_id"}',
    1);
