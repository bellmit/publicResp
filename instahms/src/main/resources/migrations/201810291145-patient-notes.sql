-- liquibase formatted sql
-- changeset janakivg:patient-notes-table

CREATE SEQUENCE patient_notes_seq
      START WITH 1
      INCREMENT BY 1
      NO MAXVALUE
      NO MINVALUE
      CACHE 1;

CREATE TABLE patient_notes (
  note_id integer not null default nextval('patient_notes_seq') PRIMARY KEY,
  patient_id  character varying(25),
  note_type_id integer,
  note_content text,
  billable_consultation character(1) default 'Y',
  consultation_type_id integer,
  charge_id  character varying(15) ,
  save_status character(1) default 'F',
  original_note_id integer,
  new_note_id integer,
  on_behalf_doctor_id character varying(30),
  on_behalf_user  character varying(100),
  created_by  character varying(100),
  created_time timestamp without time zone DEFAULT now(),
  mod_user character varying(100),
  mod_time timestamp without time zone DEFAULT now() NOT NULL,
  old_note_num integer
);
INSERT INTO hospital_roles_master VALUES(-1, 'Doctor', 'A');
INSERT INTO hospital_roles_master VALUES(-2, 'Nurse', 'A');
INSERT INTO hospital_roles_master VALUES(-3, 'Physiotherapist', 'A');
INSERT INTO hospital_roles_master VALUES(-4, 'Dietitian', 'A');

INSERT INTO user_hosp_role_master (hosp_role_user_id,hosp_role_id,u_user) 
SELECT nextval('user_hosp_role_master_seq'),-1,u.emp_username FROM u_user u 
JOIN url_action_rights uar ON (uar.role_id=u.role_id) 
WHERE action_id ='doctors_note' AND rights='A';

INSERT INTO user_hosp_role_master (hosp_role_user_id,hosp_role_id,u_user) 
SELECT nextval('user_hosp_role_master_seq'),-2,u.emp_username FROM u_user u 
JOIN url_action_rights uar ON (uar.role_id=u.role_id) 
WHERE action_id ='nurse_note' AND rights='A';

INSERT INTO note_type_master (SELECT nextval('note_type_master_seq'),'Old Doctor Notes',-1,'O','I','Y', null,'InstaAdmin',now(),'InstaAdmin',now());
INSERT INTO note_type_master (SELECT nextval('note_type_master_seq'),'Old Nurse Notes',-2,'O','I','N', null,'InstaAdmin',now(),'InstaAdmin',now());
INSERT INTO note_type_master (SELECT nextval('note_type_master_seq'),'Old Nurse Handover Notes',-2,'O','I','N', null,'InstaAdmin',now(),'InstaAdmin',now());
INSERT INTO note_type_master (SELECT nextval('note_type_master_seq'),'Old Nurse Takeover Notes',-2,'O','I','N', null,'InstaAdmin',now(),'InstaAdmin',now());

INSERT INTO patient_notes(note_id, patient_id, note_type_id, note_content, billable_consultation,
consultation_type_id,charge_id,save_status,original_note_id,on_behalf_doctor_id,on_behalf_user,created_by, created_time,
mod_user, mod_time, old_note_num)
SELECT nextval('patient_notes_seq'), patient_id,1,notes,billable_consultation,consultation_type_id,
charge_id, 'F', null, doctor_id,d.doctor_name, mod_user,creation_datetime, mod_user, mod_time, note_num 
FROM ip_doctor_notes idn left join doctors d USING (doctor_id) ;


INSERT INTO patient_notes(note_id, patient_id, note_type_id, note_content, save_status,created_by, created_time,
mod_user, mod_time, old_note_num)
SELECT nextval('patient_notes_seq'), patient_id,2,notes,'F', mod_user,creation_datetime, mod_user, mod_time, note_num 
FROM ip_nurse_notes where note_type is NULL OR note_type='';

INSERT INTO patient_notes(note_id, patient_id, note_type_id, note_content, save_status,created_by, created_time,
mod_user, mod_time, old_note_num)
SELECT nextval('patient_notes_seq'), patient_id,3,notes,'F', mod_user,creation_datetime, mod_user, mod_time, note_num 
FROM ip_nurse_notes where note_type ='H';

INSERT INTO patient_notes(note_id, patient_id, note_type_id, note_content, save_status,created_by, created_time,
mod_user, mod_time, old_note_num)
SELECT nextval('patient_notes_seq'), patient_id,4,notes,'F', mod_user,creation_datetime, mod_user, mod_time, note_num 
FROM ip_nurse_notes where note_type='T';

