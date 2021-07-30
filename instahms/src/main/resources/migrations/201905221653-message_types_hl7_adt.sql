-- liquibase formatted sql
-- changeset yashwantkumar:message_types_for_adt_hl7
insert into message_events(event_id, event_name, event_description) 
values('ADT_04', 'ADT 04 message event', 'It will used for sending ADT message');

insert into message_types(event_id,message_type_id, message_type_name, message_type_description, status, confidential, message_mode, message_group, editability) 
values('ADT_04', 'ADT_04', 'HL7 ADT 04 event type', 'hl7 adt-x version support', 'I', 'N', 'HL7_SOCKET_ADT', 'general', 'Y');


insert into message_dispatcher_config(message_mode, display_name, protocol, host_name, port_no)
values('HL7_SOCKET_ADT', 'HL7 Socket based dispatcher', 'tcp', '127.0.0.1', '6000');

insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_04', 'receiving_facility', 'ADT', 'Identifies the receiving application among multiple identical instances of the application');
insert into message_config(message_configuration_id, message_type_id, param_name, param_value, param_description)
values(nextval('message_config_seq'), 'ADT_04', 'receiving_application', 'ADT_04', 'Uniquely identifies the receiving application among all other applications');