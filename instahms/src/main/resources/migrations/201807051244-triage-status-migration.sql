-- liquibase formatted sql
-- changeset tejakilaru:<Triage-Status-Migration>

update doctor_consultation dc SET triage_done = 'P'
WHERE dc.triage_done='N' AND (select count(*) 
	FROM patient_section_details psd
	JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id
		AND psf.form_type='Form_TRI')
	WHERE psd.section_item_id=dc.consultation_id) > 0;
