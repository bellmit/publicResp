-- liquibase formatted sql
-- changeset vishwas07:migrating-appointment-to-contacts

DROP TABLE IF EXISTS contact_details CASCADE;

DROP SEQUENCE IF EXISTS contact_details_seq;

CREATE SEQUENCE contact_details_seq
     START WITH 1
     INCREMENT BY 1
     NO MINVALUE
     NO MAXVALUE
     CACHE 1;

create table contact_details (
contact_id integer DEFAULT nextval('contact_details_seq'::regclass) PRIMARY KEY NOT NULL,
salutation_name character varying(50),
patient_name character varying(100) NOT NULL,
middle_name character varying(200),
last_name character varying(50),
patient_contact character varying(16) NOT NULL,
patient_contact_country_code character varying(5),
patient_dob date,
patient_age integer,
patient_age_units character varying(1),
patient_gender character varying(1),
patient_email_id character varying(250),
vip_status char(1) DEFAULT 'N',
send_sms char(1) DEFAULT 'N',
send_email char(1) DEFAULT 'N',
preferred_language char(2) DEFAULT 'en'
);

COMMENT ON sequence contact_details_seq is '{ "type": "Txn", "comment": "contact details sequence" }';

COMMENT ON table contact_details is '{ "type": "Txn", "comment": "Contacts information for non mr no patients" }';

insert into contact_details (patient_name, patient_contact) 
select distinct upper(patient_name), patient_contact from scheduler_appointments where not exists (select * from contact_details cd, 
scheduler_appointments sa 
where (upper(cd.patient_name) = upper(sa.patient_name)) and cd.patient_contact = sa.patient_contact) and (mr_no is null or mr_no = '')
group by upper(patient_name), patient_contact;

UPDATE scheduler_appointments sa
SET contact_id = cd.contact_id
FROM contact_details cd
WHERE (sa.mr_no is null or sa.mr_no = '') 
AND (upper(sa.patient_name) = upper(cd.patient_name)) AND
sa.patient_contact = cd.patient_contact;

UPDATE contact_details cd 
SET vip_status = sa.vip_status,
patient_contact_country_code = sa.patient_contact_country_code
FROM scheduler_appointments sa
WHERE sa.vip_status IS NOT NULL AND sa.vip_status != '' 
AND sa.contact_id = cd.contact_id;
