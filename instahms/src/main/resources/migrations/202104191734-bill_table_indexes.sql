-- liquibase formatted sql
-- changeset shilpanr:adding-indexes-on-few-columns-of-bill-table-to-improve-performance

DROP INDEX IF EXISTS mrd_diagnosis_code_type_idx;
CREATE INDEX mrd_diagnosis_code_type_idx ON mrd_diagnosis(code_type);

DROP INDEX IF EXISTS mrd_diagnosis_diagnosis_datetime_idx;
CREATE INDEX mrd_diagnosis_diagnosis_datetime_idx ON mrd_diagnosis(diagnosis_datetime);

DROP INDEX IF EXISTS bill_dyna_package_id_idx;
CREATE INDEX bill_dyna_package_id_idx ON bill(dyna_package_id);

DROP INDEX IF EXISTS bill_discount_category_id_idx;
CREATE INDEX bill_discount_category_id_idx ON bill(discount_category_id);

DROP INDEX IF EXISTS bill_bill_label_id_idx;
CREATE INDEX bill_bill_label_id_idx ON bill(bill_label_id);

DROP INDEX IF EXISTS bill_username_idx;
CREATE INDEX bill_username_idx ON bill(username);

DROP INDEX IF EXISTS bill_closed_by_idx;
CREATE INDEX bill_closed_by_idx ON bill(closed_by);

DROP INDEX IF EXISTS bill_opened_by_idx;
CREATE INDEX bill_opened_by_idx ON bill(opened_by);

DROP INDEX IF EXISTS bill_finalized_by_idx;
CREATE INDEX bill_finalized_by_idx ON bill(finalized_by);

DROP INDEX IF EXISTS bill_procedure_no_idx;
CREATE INDEX bill_procedure_no_idx ON bill(procedure_no);

DROP INDEX IF EXISTS bill_discount_auth_idx;
CREATE INDEX bill_discount_auth_idx ON bill(discount_auth);

DROP INDEX IF EXISTS sponsor_procedure_limit_procedure_no_idx;
CREATE INDEX sponsor_procedure_limit_procedure_no_idx ON sponsor_procedure_limit(procedure_no);

