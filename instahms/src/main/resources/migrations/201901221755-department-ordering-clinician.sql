-- liquibase formatted sql
-- changeset mohamedanees:adding-ordering-clinican-override-dept-master

ALTER TABLE department ADD COLUMN is_referral_doc_as_ordering_clinician character(1) default 'N';