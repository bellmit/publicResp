-- liquibase formatted sql
-- changeset harishm18:index_creation_for_message_log_and_recepient failOnError:false

CREATE INDEX ml_message_type_id_idx ON message_log USING btree (message_type_id);
CREATE INDEX mr_message_log_id_idx ON message_recipient USING btree (message_log_id);