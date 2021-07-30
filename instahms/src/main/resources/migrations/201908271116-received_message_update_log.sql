-- liquibase formatted sql
-- changeset vinaykumarjavalkar:stores appointment id which got updated by smsinbound api

CREATE SEQUENCE received_message_status_update_log_seq START 1;
COMMENT ON SEQUENCE received_message_status_update_log_seq IS '{ "type": "Txn", "comment": "Holds sequence for received message log id" }';

CREATE TABLE received_message_status_update_log (
	id BIGINT DEFAULT nextval('received_message_status_update_log_seq'),
	received_message_id BIGINT REFERENCES received_message,
	updated_status_to CHARACTER VARYING(15),
	appointment_id INTEGER REFERENCES scheduler_appointments
);
COMMENT ON TABLE received_message_status_update_log IS '{ "type": "Txn", "comment": "Holds received messages appointment status log" }';
