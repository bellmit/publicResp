-- liquibase formatted sql
-- changeset tejakilaru:physician_order_migration splitStatements:false

CREATE TABLE temp_physician_order_stn_dtls(display_order integer, patient_id character varying(30));
INSERT INTO temp_physician_order_stn_dtls (SELECT max(display_order)+1 as display_order, patient_id 
FROM patient_section_details psd JOIN ip_prescription USING (patient_id) JOIN patient_section_forms psf ON  (psd.section_detail_id=psf.section_detail_id and psf.form_type='Form_IP') 
GROUP BY patient_id );

CREATE FUNCTION migrate_physician_order() RETURNS void as $$
DECLARE
physician_order RECORD;
stn_detail_id int;
BEGIN
    FOR physician_order IN 
      SELECT tn.*, pr.mr_no FROM temp_physician_order_stn_dtls tn JOIN patient_registration pr USING(patient_id)
    LOOP
      SELECT nextval('patient_section_details_seq') INTO stn_detail_id;

      INSERT INTO patient_section_details(section_detail_id, mr_no, patient_id, section_item_id, 
        generic_form_id, item_type, section_id, section_status, finalized, finalized_user, 
        user_name, mod_time) 
      VALUES (stn_detail_id, physician_order.mr_no, physician_order.patient_id, 
        0, 0, '', '-7', 'A', 'N', null, 'InstaAdmin', 
        localtimestamp(0));

      INSERT INTO patient_section_forms(section_detail_id, form_id, form_type, display_order) 
      VALUES (stn_detail_id, '-1', 'Form_IP', physician_order.display_order);

    END LOOP;
    FOR physician_order IN
      select foo.patient_id,pr.mr_no from (select distinct patient_id from ip_prescription) as foo JOIN patient_registration pr USING(patient_id) where foo.patient_id NOT IN (select patient_id from temp_physician_order_stn_dtls)
    LOOP
      SELECT nextval('patient_section_details_seq') INTO stn_detail_id;

      INSERT INTO patient_section_details(section_detail_id, mr_no, patient_id, section_item_id, 
        generic_form_id, item_type, section_id, section_status, finalized, finalized_user, 
        user_name, mod_time) 
      VALUES (stn_detail_id, physician_order.mr_no, physician_order.patient_id, 
        0, 0, '', '-7', 'A', 'N', null, 'InstaAdmin', 
        localtimestamp(0));

      INSERT INTO patient_section_forms(section_detail_id, form_id, form_type, display_order) 
      VALUES (stn_detail_id, '-1', 'Form_IP', 1);

      END LOOP;
END;
$$
LANGUAGE plpgsql;

SELECT migrate_physician_order();
DROP FUNCTION migrate_physician_order();
DROP TABLE temp_physician_order_stn_dtls;


CREATE TABLE ip_migrated_sequences (
  old_id integer,
  new_id integer
);

COMMENT ON table ip_migrated_sequences is '{ "type": "Txn", "comment": "ip_prescription to patient_prescription migration backup" }';

INSERT INTO ip_migrated_sequences
SELECT prescription_id as old_id, nextval('patient_prescription_seq') as new_id
FROM ip_prescription;

INSERT INTO patient_prescription(patient_presc_id, visit_id, doctor_id, prescribed_date,
presc_type, prior_med, freq_type, recurrence_daily_id, repeat_interval, start_datetime,
end_datetime, no_of_occurrences, end_on_discontinue, discontinued,
repeat_interval_units, adm_request_id, username)
SELECT new_id as patient_presc_id, patient_id as visit_id, doctor_id, prescription_date as prescribed_date, 
CASE WHEN presc_type = 'O' THEN 'NonBillable' WHEN presc_type = 'M' THEN 'Medicine' 
WHEN presc_type = 'I' THEN 'Inv.' WHEN presc_type = 'C' THEN 'Doctor' 
WHEN presc_type = 'S' THEN 'Service' END presc_type,
prior_med, freq_type, recurrence_daily_id, repeat_interval, start_datetime,
end_datetime, no_of_occurrences, end_on_discontinue, discontinued,
repeat_interval_units, adm_request_id, username 
from ip_prescription 
join ip_migrated_sequences on (old_id=prescription_id);


INSERT INTO patient_medicine_prescriptions(mod_time, medicine_id, strength, route_of_admin,
item_form_id, item_strength, generic_code, medicine_remarks, item_strength_units, admin_strength,
op_medicine_pres_id, username, medicine_quantity)
Select coalesce(mod_time, entered_datetime) as mod_time, item_id::int as medicine_id, med_dosage AS strength, med_route as route_of_admin, med_form_id AS item_form_id, med_strength AS item_strength, generic_code, remarks as medicine_remarks, med_strength_units AS item_strength_units, admin_strength, new_id AS op_medicine_pres_id, username, 1 AS medicine_quantity
FROM ip_prescription 
JOIN ip_migrated_sequences on (old_id=prescription_id)
WHERE presc_type = 'M';

INSERT INTO patient_test_prescriptions(test_remarks, mod_time, test_id, username, op_test_pres_id)
Select remarks as test_remarks, coalesce(mod_time, entered_datetime) as mod_time,
item_id as test_id, username, new_id AS op_test_pres_id
from ip_prescription 
join ip_migrated_sequences on (old_id=prescription_id)
where presc_type = 'I';

INSERT INTO patient_service_prescriptions(service_remarks, mod_time, service_id, username,
op_service_pres_id, qty)
Select remarks as service_remarks, coalesce(mod_time, entered_datetime) as mod_time,
item_id as service_id, username, new_id AS op_service_pres_id, 1 as qty
from ip_prescription 
join ip_migrated_sequences on (old_id=prescription_id)
where presc_type = 'S';

INSERT INTO patient_consultation_prescriptions(cons_remarks, username, mod_time, doctor_id, prescription_id)
Select remarks AS cons_remarks, username, coalesce(mod_time, entered_datetime) as mod_time,
item_id AS doctor_id, new_id AS prescription_id
from ip_prescription 
join ip_migrated_sequences on (old_id=prescription_id)
where presc_type = 'C';

INSERT INTO patient_other_prescriptions(item_name, item_remarks, mod_time, strength, item_form_id,
item_strength, item_strength_units, prescription_id, admin_strength, username)
Select item_name, remarks as item_remarks, coalesce(mod_time, entered_datetime) as mod_time,
med_dosage AS strength, med_form_id AS item_form_id, med_strength AS item_strength,
med_strength_units AS item_strength_units, new_id AS prescription_id, admin_strength, username
from ip_prescription 
join ip_migrated_sequences on (old_id=prescription_id)
where presc_type = 'O';

DROP INDEX IF EXISTS patient_activities_presc_id_idx;

UPDATE patient_activities SET prescription_id=new_id
FROM ip_migrated_sequences
WHERE old_id=prescription_id;

CREATE INDEX patient_activities_presc_id_idx ON patient_activities USING btree (prescription_id);

ALTER TABLE ip_prescription RENAME TO ip_prescription_backup;
