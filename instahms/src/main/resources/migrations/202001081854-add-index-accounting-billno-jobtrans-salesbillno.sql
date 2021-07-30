-- liquibase formatted sql
-- changeset rajendratalekar:add-index-accounting-billno-jobtrans-salesbill-no

CREATE INDEX hai_bill_no_idx on hms_accounting_info(bill_no);
CREATE INDEX hai_salebillno_idx ON hms_accounting_info (sale_bill_no);
CREATE INDEX hai_job_transaction_idx ON hms_accounting_info (job_transaction);