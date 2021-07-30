-- liquibase formatted sql
-- changeset sindur:update-stock-take-id-generation-pattern
UPDATE unique_number SET pattern = '000000' WHERE type_number = 'physical_stock_take';

