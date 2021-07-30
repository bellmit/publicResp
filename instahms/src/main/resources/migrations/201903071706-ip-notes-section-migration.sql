-- liquibase formatted sql
-- changeset janakivg:ipemr-notes-sections-migration splitStatements:false

CREATE TABLE temp_note_stn_dtls(display_order integer, patient_id character varying(30));
INSERT INTO temp_note_stn_dtls (SELECT max(display_order)+1 as display_order, patient_id 
FROM patient_section_details psd JOIN patient_notes USING (patient_id) JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id and psf.form_type='Form_IP') 
GROUP BY patient_id );

CREATE FUNCTION migrate_notes() RETURNS void as $$
DECLARE
notesection RECORD;
stn_detail_id int;
BEGIN
    FOR notesection IN 
      SELECT tn.*, pr.mr_no FROM temp_note_stn_dtls tn JOIN patient_registration pr USING(patient_id)
    LOOP
      SELECT nextval('patient_section_details_seq') INTO stn_detail_id;

      INSERT INTO patient_section_details(section_detail_id, mr_no, patient_id, section_item_id, 
        generic_form_id, item_type, section_id, section_status, finalized, finalized_user, 
        user_name, mod_time) 
      VALUES (stn_detail_id, notesection.mr_no, notesection.patient_id, 
        0, 0, '', '-18', 'A', 'N', null, 'InstaAdmin', 
        localtimestamp(0));

      INSERT INTO patient_section_forms(section_detail_id, form_id, form_type, display_order) 
      VALUES (stn_detail_id, '-1', 'Form_IP', notesection.display_order);

    END LOOP;
END;
$$
LANGUAGE plpgsql;

SELECT migrate_notes();
DROP FUNCTION migrate_notes();

CREATE FUNCTION create_notes_section() RETURNS void as $$
DECLARE
notesection RECORD;
stn_detail_id int;
BEGIN
    FOR notesection IN 
      SELECT foo.patient_id,pr.mr_no from (select distinct patient_id from patient_notes) as foo JOIN patient_registration pr USING(patient_id) where foo.patient_id NOT IN (select patient_id from temp_note_stn_dtls)
    LOOP
      SELECT nextval('patient_section_details_seq') INTO stn_detail_id;

      INSERT INTO patient_section_details(section_detail_id, mr_no, patient_id, section_item_id, 
        generic_form_id, item_type, section_id, section_status, finalized, finalized_user, 
        user_name, mod_time) 
      VALUES (stn_detail_id, notesection.mr_no, notesection.patient_id, 
        0, 0, '', '-18', 'A', 'N', null, 'InstaAdmin', 
        localtimestamp(0));

      INSERT INTO patient_section_forms(section_detail_id, form_id, form_type, display_order) 
      VALUES (stn_detail_id, '-1', 'Form_IP', 1);

    END LOOP;
END;
$$
LANGUAGE plpgsql;

SELECT create_notes_section();
DROP FUNCTION create_notes_section();
DROP TABLE temp_note_stn_dtls;
