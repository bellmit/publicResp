-- liquibase formatted sql
-- changeset yashwantkumar:message_types_for_hl7_adt_18

insert into message_events(event_id, event_name, event_description) 
values('ADT_18', 'ADT 18 message event', 'It will used for sending ADT 18 message');

insert into message_types(event_id,message_type_id, message_type_name, message_type_description, status, confidential, message_mode, message_group, editability) 
values('ADT_18', 'ADT_18', 'HL7 ADT 18 event type', 'hl7 adt-x version support', 'I', 'N', 'HL7_SOCKET_ADT', 'general', 'Y');

insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_18', 'receiving_facility', 'ADT', 'Identifies the receiving application among multiple identical instances of the application');
insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_18', 'receiving_application', 'ADT_18', 'Uniquely identifies the receiving application among all other applications');