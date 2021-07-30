-- liquibase formatted sql
-- changeset pranays:<generic-forms-migration-patient-form-details>

CREATE TABLE patient_form_details (
	form_detail_id integer,
	mr_no character varying(15),
	patient_id character varying(15),
	form_type character varying,
	created_date timestamp without time zone DEFAULT ('now'::text)::timestamp(0) without time zone,
	mod_time timestamp without time zone,
	user_name character varying(30),
	form_master_id integer,
	form_status character varying,
	revision_number numeric,
	reopen_remarks character varying(2000),
	created_by character varying(50)
);

COMMENT ON TABLE patient_form_details IS '{ "type": "Txn", "comment": "saves form metadata"}';

COMMENT ON COLUMN patient_form_details.form_status IS 'Status of Form: C -> Saved & Finalized, P -> Partial' ;
COMMENT ON COLUMN patient_form_details.form_detail_id IS 'Reference key to form item (generic_form_id for Generic Forms, section_item_id for Consultation';

INSERT INTO patient_form_details (form_detail_id, mr_no, patient_id, form_type, created_date, mod_time, user_name, form_master_id, form_status, created_by)
SELECT form_detail_id, mr_no, patient_id, form_type, created_date, mod_time, user_name, form_id,'P' AS status, user_name 
	FROM (
		SELECT psd.generic_form_id AS form_detail_id, psd.mr_no, psd.patient_id, psf.form_type, MIN(psd.mod_time) AS created_date, 
			MAX(psd.mod_time) AS mod_time, psd.user_name, fc.id AS form_id
      	FROM patient_section_details psd 
         JOIN patient_section_forms psf ON (psd.section_detail_id = psf.section_detail_id) 
         JOIN patient_registration pr ON (pr.patient_id = psd.patient_id) 
         JOIN form_components fc ON (fc.id = psf.form_id) 
      WHERE psf.form_type = 'Form_Gen' AND coalesce(section_item_id, 0) = 0  AND psd.item_type='GEN'
      GROUP BY psd.generic_form_id, psd.mr_no, psd.patient_id, psf.form_type, psd.user_name, fc.id 
      ORDER BY psd.generic_form_id) AS foo;

CREATE INDEX pat_id_form_type_idx ON patient_form_details(patient_id, form_type);

CREATE INDEX form_detail_id_form_type_idx ON patient_form_details(form_detail_id, form_type);