-- liquibase formatted sql
-- changeset satishl2772:adding-tax-deduction-label-for-payment-voucher-genericpreference

ALTER TABLE generic_preferences ADD COLUMN tax_deduction_label_for_payment_voucher character varying(5) NOT NULL DEFAULT 'TDS';
