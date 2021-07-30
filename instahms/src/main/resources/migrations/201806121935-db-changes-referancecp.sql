-- liquibase formatted sql

-- changeset irshad.m:feature-1114-issuestax-19519
ALTER TABLE store_item_lot_details ADD COLUMN reference_package_cp numeric(15, 2) default 0;