-- liquibase formatted sql
-- changeset sindur:stock-take-id-generation
INSERT INTO unique_number(type_number, start_number, prefix, pattern)
VALUES ('physical_stock_take', 1, 'ST', '00000');
