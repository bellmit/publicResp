-- liquibase formatted sql
-- changeset rajendratalekar:index-accounting-info-voucher-idx failOnError:false

create index hai_voucher_type_idx on hms_accounting_info (voucher_type);
create index hai_mod_time_idx on hms_accounting_info (mod_time);
