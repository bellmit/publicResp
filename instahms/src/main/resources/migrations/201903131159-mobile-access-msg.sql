-- liquibase formatted sql
-- changeset sanjana:mobile-access-msg

insert into message_events values ('enable_mobile_access','Enable Mobile Access','Event is triggered when mobile access is enabled for the patient');

insert into message_types (message_type_id,message_type_name,message_type_description,message_body,event_id,message_mode,status,message_group,editability) values ('sms_enable_mobile_access', 'Enable Mobile Access Message', 'Message to send login credentials to patient for mobile access.','Hi ${receipient_name},
Welcome to ${center_name}.

We have setup an account for you to access the Mobile App.
The Login Credentials are as below:
Username: ${username}
Password: ${password}
 
Please change your password after you login the first time. 
 
Regards,
${center_name}','enable_mobile_access','SMS','A','general', 'A');

insert into message_types (message_type_id,message_type_name,message_type_description,message_body,event_id,message_mode,status,message_group,editability,message_subject) values ('email_enable_mobile_access', 'Enable Mobile Access Email', 'Message to send login credentials to patient for mobile access.','Hi ${receipient_name},
Welcome to ${center_name}.

We have setup an account for you to access the Mobile App.
The Login Credentials are as below:
Username: ${username}
Password: ${password}
 
Please change your password after you login the first time. 
You should keep a copy of the mail for your future reference.
 
Regards,
${center_name}','enable_mobile_access','EMAIL','A','general', 'A', 'Mobile Access Credentials');