-- liquibase formatted sql
-- changeset yashwantkumar:category_for_hl7

INSERT into message_category VALUES(nextval('message_category_seq'), 'HL7', 'A');

UPDATE message_types set category_id = (SELECT message_category_id FROM message_category
WHERE message_category_name = 'HL7') WHERE message_type_id in ('ADT_04', 'ADT_08', 'ADT_18');