-- liquibase formatted sql
-- changeset vinaykumarjavalkar:insta sms which stores the sms received

CREATE SEQUENCE received_message_seq START 1;
COMMENT ON SEQUENCE received_message_seq IS '{ "type": "Txn", "comment": "Holds sequence for received message id" }';


CREATE TABLE received_message (
   id BIGINT DEFAULT nextval('received_message_seq'),
   from_phone_no CHARACTER VARYING(16) NOT NULL,
   message CHARACTER VARYING(160),
   message_type CHARACTER VARYING(10),
   received_date_time TIMESTAMP default now() NOT NULL,
   job_id CHARACTER VARYING(100),
   job_status CHARACTER VARYING(15),
   modified_at TIMESTAMP default now(),
   received_from_ip CHARACTER VARYING(40),
   PRIMARY KEY(id)
);
COMMENT ON TABLE received_message IS '{ "type": "Txn", "comment": "Holds received messages" }';

ALTER TABLE public_api_ip_whitelist ADD COLUMN used_for CHARACTER VARYING(20);
UPDATE public_api_ip_whitelist SET used_for = 'sync_ext';
ALTER TABLE generic_preferences ADD COLUMN received_sms_appointment_confirm CHARACTER VARYING(15);
ALTER TABLE generic_preferences ADD COLUMN received_sms_appointment_cancel CHARACTER VARYING(15);