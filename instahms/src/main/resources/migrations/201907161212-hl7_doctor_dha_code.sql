-- liquibase formatted sql
-- changeset yashwantkumar:doctor_license_number

insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_04', 'hl7_is_doctor_license_number', 'Y', 'HL7 will have either doctor_id / doctor_license_number');
insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_08', 'hl7_is_doctor_license_number', 'Y', 'HL7 will have either doctor_id / doctor_license_number');
insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_18', 'hl7_is_doctor_license_number', 'Y', 'HL7 will have either doctor_id / doctor_license_number');