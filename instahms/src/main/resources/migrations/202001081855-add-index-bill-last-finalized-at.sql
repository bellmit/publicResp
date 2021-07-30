-- liquibase formatted sql
-- changeset rajendratalekar:add-index-accounting-billno-jobtrans-salesbill-no

CREATE INDEX bill_last_finalized_at_idx on bill(last_finalized_at);