-- liquibase formatted sql
-- changeset janakivg:start-date-end-date-changes-in-prescriptions

UPDATE patient_prescription pp SET start_datetime=start_date, end_datetime=end_date FROM (SELECT start_date, end_date, op_medicine_pres_id from patient_medicine_prescriptions where start_date is not null) as pmp WHERE pp.patient_presc_id = pmp.op_medicine_pres_id;

UPDATE patient_prescription pp SET start_datetime=start_date, end_datetime=end_date FROM (SELECT start_date, end_date, prescription_id from patient_other_medicine_prescriptions where start_date is not null) as pomp WHERE pp.patient_presc_id = pomp.prescription_id;

UPDATE patient_prescription pp SET start_datetime=start_date, end_datetime=end_date FROM (SELECT start_date, end_date, prescription_id from patient_other_prescriptions where start_date is not null) as pop WHERE pp.patient_presc_id = pop.prescription_id;

ALTER TABLE patient_medicine_prescriptions RENAME column start_date to obsolete_start_date;
ALTER TABLE patient_medicine_prescriptions RENAME column end_date to obsolete_end_date;
ALTER TABLE patient_other_medicine_prescriptions RENAME column start_date to obsolete_start_date;
ALTER TABLE patient_other_medicine_prescriptions RENAME column end_date to obsolete_end_date;
ALTER TABLE patient_other_prescriptions RENAME column start_date to obsolete_start_date;
ALTER TABLE patient_other_prescriptions RENAME column end_date to obsolete_end_date;
