-- liquibase formatted sql
-- changeset sonam009:ip-form-saved-status-migration

UPDATE patient_registration pr SET ipemr_status = 'P'
WHERE pr.ipemr_status='N' AND (SELECT count(1) 
	FROM patient_section_details psd
	JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id AND psf.form_type='Form_IP')
WHERE pr.visit_type='i' AND pr.patient_id = psd.patient_id) > 0;
