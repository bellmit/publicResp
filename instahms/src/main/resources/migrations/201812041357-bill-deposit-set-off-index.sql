-- liquibase formatted sql
-- changeset allabakash:Bill-deposit-set-off-index
-- validCheckSum: ANY

create index idx_bill_deposit_set_off on bill(deposit_set_off) where deposit_set_off>0;