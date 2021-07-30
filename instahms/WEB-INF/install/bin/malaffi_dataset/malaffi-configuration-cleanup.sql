DELETE FROM message_mapping_hl7;
DELETE FROM interface_details_hl7;
DELETE FROM interface_hl7;
ALTER SEQUENCE interface_details_hl7_seq RESTART WITH 1;
ALTER SEQUENCE interface_hl7_seq RESTART WITH 1;