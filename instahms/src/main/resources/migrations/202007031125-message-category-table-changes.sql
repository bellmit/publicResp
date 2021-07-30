-- liquibase formatted sql
-- changeset raeshmika:<adding-columns-transactional-system-defined-dropping-ui-master>

ALTER TABLE message_category ADD COLUMN system_defined character varying(1) default 'N';

ALTER TABLE message_category ADD COLUMN is_transactional character varying(1) default 'Y';

UPDATE message_category set is_transactional = 'N' where message_category_name='Custom Promotional';

UPDATE message_category set system_defined='Y' where message_category_name in('Test', 'Practo Share', 'Clinical', 'Registration', 'Billing', 'Appointments', 'Diagnostics', 'Discharge', 'Inventory', 'Vaccination', 'Ward', 'Doctor', 'Management', 'Promotional','HL7','General');

DELETE FROM screen_rights  where screen_id ='mas_message_category';

DELETE FROM url_action_rights where action_id ='mas_message_category';

