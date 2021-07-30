-- liquibase formatted sql
-- changeset mohamedanees:prec-three-changes-for-package-tables context:precision-3

ALTER TABLE package_content_charges ALTER COLUMN charge TYPE numeric(16, 3);
ALTER TABLE package_content_charges ALTER COLUMN discount TYPE numeric(16, 3);
