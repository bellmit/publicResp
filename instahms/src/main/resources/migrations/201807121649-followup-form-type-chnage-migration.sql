-- liquibase formatted sql
-- changeset sonam009:<followup-form-type-change-migration>

SELECT * INTO patient_section_detail_id_temp FROM (
   SELECT distinct psf.section_detail_id
   FROM doctor_consultation dc
   JOIN patient_registration pr ON(pr.patient_id = dc.patient_id)
   JOIN patient_section_details psd ON(psd.section_item_id = dc.consultation_id)
   JOIN patient_section_forms psf ON(psd.section_detail_id = psf.section_detail_id)
   WHERE dc.status != 'A' AND pr.op_type='F' AND psf.form_type = 'Form_CONS'
)AS foo;

SELECT * INTO patient_section_forms_temp FROM
(SELECT psf.section_detail_id, psf.form_id, psf.display_order,
CASE WHEN psf.section_detail_id = temp.section_detail_id then 'Form_OP_FOLLOW_UP_CONS' ELSE psf.form_type END AS form_type
FROM patient_section_forms psf
LEFT JOIN patient_section_detail_id_temp temp USING(section_detail_id)
) AS foo;

DROP TABLE patient_section_forms CASCADE;
DROP TABLE patient_section_detail_id_temp;

ALTER TABLE patient_section_forms_temp RENAME TO patient_section_forms;  

CREATE INDEX patient_section_forms_form_type_idx ON patient_section_forms USING btree (form_type);

CREATE INDEX psf_section_detail_id_idx ON patient_section_forms USING btree (section_detail_id);