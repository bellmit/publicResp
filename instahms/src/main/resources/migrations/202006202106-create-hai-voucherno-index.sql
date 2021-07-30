-- liquibase formatted sql
-- changeset rajendratalekar:create-hai-voucherno-idx failOnError:false

create index hai_voucher_no_idx on hms_accounting_info (voucher_no);
