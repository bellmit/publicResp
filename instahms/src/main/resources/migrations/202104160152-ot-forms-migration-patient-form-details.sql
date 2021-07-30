-- liquibase formatted sql
-- changeset pranays:migrating-existing-surgery-forms-to-patient-form-details

INSERT INTO patient_form_details (form_detail_id, mr_no, patient_id, form_type, created_date, mod_time, user_name, form_master_id, form_status, created_by)
SELECT op.operation_proc_id AS form_detail_id, od.mr_no, od.patient_id, psf.form_type, MIN(psd.mod_time) AS created_date,
MAX(psd.mod_time) AS mod_time, psd.user_name,psf.form_id, 'P' AS status, psd.user_name
FROM operation_details od
   JOIN patient_registration pr on (od.patient_id=pr.patient_id and discharge_flag <> 'D')
   JOIN operation_procedures op using (operation_details_id)
   JOIN patient_section_details psd on (op.operation_proc_id=psd.section_item_id)
   JOIN patient_section_forms psf using (section_detail_id)
   where psf.form_type='Form_OT' and section_status='A'
GROUP BY operation_proc_id, od.patient_id, od.mr_no, psf.form_type, psf.form_id, psd.user_name
ORDER BY od.patient_id;
