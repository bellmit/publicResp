-- liquibase formatted sql
-- changeset rajendratalekar:change-sms-defaults-to-failing-http

update message_dispatcher_config set protocol='http', host_name='127.0.0.1', custom_param_1=null, http_url='http://127.0.0.1/_sms_not_configured', country_code_prefix=null, max_attachment_kb=0, port_no=0, http_success_response='_sms_not_configured' where message_mode='SMS' and host_name in ('hub.instahealthsolutions.com', '10.11.0.1') and protocol='smtp' and custom_param_1='.instahms@smscountry.net';
update generic_preferences set sms_username='',username=username where sms_username='.instahms@smscountry.net';