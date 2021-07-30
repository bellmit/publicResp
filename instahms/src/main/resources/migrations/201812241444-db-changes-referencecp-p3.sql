-- liquibase formatted sql
-- changeset adeshatole:feature-1114-issuestax-19519-precision-3 context:precision-3

ALTER TABLE store_item_lot_details ALTER COLUMN reference_package_cp TYPE numeric(16, 3);