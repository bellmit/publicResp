-- liquibase formatted sql
-- changeset sanjana:updated-mobile-access-email-body

update message_types set message_body ='Hi ${receipient_name},<br>
Welcome to ${center_name}.<br><br>

We have setup an account for you to access the Mobile App.<br>
The Login Credentials are as below:<br>
Username: ${username}<br>
Password: ${password}<br><br>
 
Please change your password after you login the first time. <br>
You should keep a copy of the mail for your future reference.<br><br>
 
Regards,<br>
${center_name}' where message_type_id='email_enable_mobile_access';