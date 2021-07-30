-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-common-5

CREATE INDEX receipts_mr_no_idx on receipts(mr_no);