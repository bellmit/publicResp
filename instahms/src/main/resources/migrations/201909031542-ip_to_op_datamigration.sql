-- liquibase formatted sql
-- changeset tejakilaru:ip_to_op_datamigration

SELECT DISTINCT section_item_id, patient_id INTO temp_patient_section_details 
FROM patient_section_details 
WHERE section_item_id != 0 AND item_type='CONS' AND patient_id ilike '%IP%';

SELECT *  INTO temp_op_ip_converted
FROM (SELECT consultation_id, dc.patient_id, tpsd.patient_id AS ip_patient_id 
	FROM temp_patient_section_details tpsd 
	JOIN doctor_consultation dc on (dc.consultation_id=tpsd.section_item_id 
		AND dc.patient_id!=tpsd.patient_id)) as foo;

UPDATE patient_section_details psd set patient_id=toi.patient_id 
FROM temp_op_ip_converted toi 
WHERE toi.consultation_id = psd.section_item_id AND psd.item_type='CONS';

SELECT * INTO temp_diag_ids
FROM (SELECT id, visit_id, ip_patient_id
	FROM mrd_diagnosis_audit_log
	JOIN temp_op_ip_converted toi ON (toi.patient_id=visit_id)
	WHERE field_name='id' AND operation='INSERT') AS foo;

INSERT INTO mrd_diagnosis
	SELECT visit_id, nextval('mrd_diagnosis_seq'::regclass) AS id,
	description, icd_code, code_type,
	diag_type, username, mod_time, diagnosis_status_id, 
	remarks, doctor_id, diagnosis_datetime, adm_request_id,
	sent_for_approval, present_on_admission, year_of_onset
	FROM mrd_diagnosis
	WHERE id IN (SELECT id FROM temp_diag_ids);

UPDATE mrd_diagnosis as md SET visit_id=tdi.visit_id 
FROM temp_diag_ids tdi 
WHERE md.id=tdi.id;

SELECT * INTO temp_secondary_complaints_count
FROM (SELECT count(*) as count, scal.visit_id, ip_patient_id
		FROM secondary_complaints_audit_log scal
		JOIN temp_op_ip_converted toi ON (toi.patient_id=scal.visit_id)
		WHERE scal.operation = 'INSERT' GROUP BY visit_id, ip_patient_id) as foo;

SELECT * INTO temp_secondary_complaints_id
	FROM (SELECT foo.row_id, tscc.visit_id
	FROM temp_secondary_complaints_count tscc
	JOIN LATERAL (SELECT * 
		FROM secondary_complaints sc
		WHERE sc.visit_id=tscc.ip_patient_id
		ORDER BY sc.row_id
		LIMIT tscc.count) as foo ON true) as fooo;

INSERT INTO secondary_complaints
	(SELECT nextval('secondary_complaints_seq'::regclass) AS row_id, visit_id, complaint, username, mod_time
	FROM secondary_complaints
	WHERE row_id IN (SELECT row_id FROM temp_secondary_complaints_id));

UPDATE secondary_complaints sc SET visit_id=tsci.visit_id
	FROM temp_secondary_complaints_id tsci
	WHERE tsci.row_id=sc.row_id;

DROP TABLE temp_patient_section_details;
DROP TABLE temp_op_ip_converted;
DROP TABLE temp_diag_ids;
DROP TABLE temp_secondary_complaints_count;
DROP TABLE temp_secondary_complaints_id;


