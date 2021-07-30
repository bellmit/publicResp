-- liquibase formatted sql
-- changeset abhishekv31:index-creation-for-performance-in-bill-screen-on-save failOnError:false
CREATE INDEX pd_mr_no_idx ON patient_deposits(mr_no);
CREATE INDEX bill_ip_deposit_set_off_idx ON bill (ip_deposit_set_off);
