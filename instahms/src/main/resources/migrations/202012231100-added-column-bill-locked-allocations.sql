-- liquibase formatted sql
-- changeset allabakash:added-column-bill-allocation-lock

ALTER TABLE bill ADD COLUMN locked boolean default false;
 COMMENT ON column bill.locked IS ' LOCK THE BILL, ONCE THE ALLOCATION JOB STARTED. ';