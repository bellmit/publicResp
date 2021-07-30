-- liquibase formatted sql
-- changeset rajendratalekar:deprecate-dbaj-sms-gateway

DELETE FROM modules_activated where module_id='mod_dbajsms_customapi';
DELETE FROM insta_integration where integration_name='DBAJ_SMS_gateway';

