-- liquibase formatted sql
-- changeset yashwantkumar:<commit-message-describing-this-database-change>

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, param_description) 
VALUES (nextval('message_config_seq'), 'email_diag_report', 'pdf_encryption', 'N', 'Applying encryption for PDF');