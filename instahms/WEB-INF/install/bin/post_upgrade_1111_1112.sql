create index concurrently idx_bill_no_bill_charge_audit_log on bill_charge_audit_log(bill_no);
create index concurrently idx_bill_no_bill_audit_log on bill_audit_log(bill_no);
CREATE INDEX tests_prescribed_presc_date_idx ON tests_prescribed (date(pres_date));
create index concurrently idx_charge_id_bill_charge_audit_log on bill_charge_audit_log(charge_id);
