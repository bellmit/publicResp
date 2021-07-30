-- liquibase formatted sql
-- changeset AdeshAtole:sms-localization


-- preferred languages to show in dropdown for sms contact preferences
CREATE TABLE preferred_languages (
    lang_code character varying(10) PRIMARY KEY
);

INSERT INTO preferred_languages VALUES('en');
INSERT INTO preferred_languages VALUES('kn');
INSERT INTO preferred_languages VALUES('ta');
INSERT INTO preferred_languages VALUES('hi');
INSERT INTO preferred_languages VALUES('ar');
INSERT INTO preferred_languages VALUES('ml');


-- schema level contact preference
ALTER TABLE generic_preferences add column contact_pref_lang_code character varying(10) default 'en'  REFERENCES preferred_languages(lang_code);

-- patient level contact preference
CREATE SEQUENCE contact_preferences_seq;
CREATE TABLE contact_preferences (
    pref_id integer default nextval('contact_preferences_seq') PRIMARY KEY,
    mr_no character varying(15) unique,
    receive_communication character varying(1) default 'B' not null,
    lang_code character varying(10) not null,
    FOREIGN KEY(mr_no) REFERENCES patient_details(mr_no),
    FOREIGN KEY(lang_code) REFERENCES preferred_languages(lang_code)
);


-- add preferred language field in patient header
INSERT INTO patient_header_preferences VALUES (
'contact_pref_lang_code', 'Y', 'P', 'Preferred Language', 'b', 'Both', 50, 'Text' );


-- message and event type for deposit sms
INSERT INTO message_events VALUES('deposit_paid','Deposit paid', 'Event used for triggering a SMS to the payer and patient when deposit is paid');

INSERT INTO message_types VALUES('sms_deposit_paid','Deposit Information Message','Message is automatically sent to patient and payer whenever patient pays deposit.',null,null,null,'Patient, ${patient_name}, has made a deposit of ${currency_symbol} ${deposit_amount}  at ${center_name}.',null,null,'deposit_paid','SMS','I',NULL,'general', 'A');
---- making deposit SMS editable by only InstaAdmin if practo sms is on
UPDATE message_types SET editability = 'I' WHERE message_type_id = 'sms_deposit_paid' AND exists(SELECT * FROM modules_activated WHERE module_id = 'mod_practo_sms' AND activation_status= 'Y');

-- message and event type for sms to doctor for op oatient admission
INSERT INTO message_events VALUES('op_patient_admitted','OP Patient Admitted', 'Event used for triggering OP patient admitted message');

INSERT INTO message_types VALUES('sms_op_patient_admitted','OP Patient Admission to Doctor','Message is sent automatically to the consulting and referral doctor on OP admission of a patient. This is to inform the doctor about patient''s admission.',null,null,null,'Dear doctor, ${patient_name} has been admitted at ${center_name}.Please call hospital for any query.',null,null,'op_patient_admitted','SMS','I',NULL,'general', 'A');
