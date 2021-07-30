-- liquibase formatted sql
-- changeset javalkarvinay:added_message_queue_table

CREATE SEQUENCE export_message_queue_seq START 1;
COMMENT ON SEQUENCE export_message_queue_seq IS '{ "type": "Txn", "comment": "Holds sequence for message queue seq" }';

CREATE TABLE export_message_queue (
     msg_id BIGINT DEFAULT nextval('export_message_queue_seq'),
     interface_id INTEGER,
     status VARCHAR(10),
     created_at TIMESTAMP default now(),
     modified_at TIMESTAMP default now(),
     count INTEGER,
     job_data TEXT,
     acknowledge_msg TEXT,
     acknowledge_status VARCHAR(20),
     PRIMARY KEY (msg_id)
 );
COMMENT ON table export_message_queue is '{ "type": "Txn", "comment": "Message queue" }';

INSERT INTO cron_job (job_group,job_name,job_time,job_status,job_description, job_allow_disable, job_params) VALUES ('ClearInterfaceMessageQueue','ClearInterfaceMessageQueue','0 45 12 * * ?','A','Clear Interface queue using the status specified in job params (Status can be specified using comma separated values)','Y','SENT');