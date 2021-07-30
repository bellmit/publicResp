-- liquibase formatted sql
-- changeset pallavia08:alter-queries-for-payment-transactions-tables-for-salucro-integration
ALTER TABLE payment_transactions ADD COLUMN response text;
ALTER TABLE payment_transactions ADD constraint payment_transaction_id_pkey PRIMARY KEY(payment_transaction_id);
ALTER TABLE payment_transactions ADD column payment_mode character varying(100);