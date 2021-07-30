-- liquibase formatted sql
-- changeset satishl2772:allow-zero-tax-vouchers-generic-preference


ALTER TABLE generic_preferences ADD COLUMN fa_allow_zero_tax_voucher character(1) NOT NULL DEFAULT 'N';
COMMENT ON COLUMN generic_preferences.fa_allow_zero_tax_voucher IS 'Values : (Y,N) , Y - allows zero tax voucher posting, N - not allow ';
