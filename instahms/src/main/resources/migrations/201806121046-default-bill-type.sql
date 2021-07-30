-- liquibase formatted sql
-- changeset kartikag01:defaut-bill-type

update center_preferences set pref_default_ip_bill_type = 'C' where pref_default_ip_bill_type is null OR pref_default_ip_bill_type = ' ' OR pref_default_ip_bill_type = '';
update center_preferences set pref_default_op_bill_type = 'P' where pref_default_op_bill_type is null OR pref_default_op_bill_type = ' ' OR pref_default_op_bill_type = '';
ALTER TABLE center_preferences ALTER COLUMN pref_default_ip_bill_type SET DEFAULT 'C';
ALTER TABLE center_preferences ALTER COLUMN pref_default_ip_bill_type SET NOT NULL;
ALTER TABLE center_preferences ALTER COLUMN pref_default_op_bill_type SET DEFAULT 'P';
ALTER TABLE center_preferences ALTER COLUMN pref_default_op_bill_type SET NOT NULL;
