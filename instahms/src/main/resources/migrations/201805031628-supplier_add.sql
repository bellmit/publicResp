-- liquibase formatted sql
-- changeset suryakant.t:Changed_supplier_address_from_200_to_500
ALTER TABLE supplier_master ALTER COLUMN supplier_address TYPE character varying(500);

