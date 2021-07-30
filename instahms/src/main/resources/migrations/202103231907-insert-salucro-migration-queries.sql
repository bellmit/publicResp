-- liquibase formatted sql
-- changeset pallavia08:insert-queries-for-salucro-integration
-- validCheckSum: ANY
INSERT into modules_activated values('mod_salucro','N');
INSERT into payment_mode_master(mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required,card_auth_required,card_holder_required,card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) values(-10,'Salucro' , 'N', 'N','N','N','I', (select max(displayorder)+1 from payment_mode_master), 'Salucro','N', 'N','N','N','N', 'N', 'N',0.00,'A');
INSERT INTO saved_searches values('Role Mapping',  nextval('saved_searches_seq'), 'System', 'Active Users', true,
'status=A', 'InstaAdmin', 'InstaAdmin', now(), now());

INSERT INTO saved_searches values('Role Mapping',  nextval('saved_searches_seq'), 'System', 'Inactive Users', false,
'status=I', 'InstaAdmin', 'InstaAdmin', now(), now());


INSERT INTO saved_searches values('Location Mapping',  nextval('saved_searches_seq'), 'System', 'Active Counters', true,
'status=A', 'InstaAdmin', 'InstaAdmin', now(), now());

INSERT INTO saved_searches values('Location Mapping',  nextval('saved_searches_seq'), 'System', 'Inactive Counters', false,
'status=I', 'InstaAdmin', 'InstaAdmin', now(), now());