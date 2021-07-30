-- liquibase formatted sql
-- changeset manika09:add-valid-to-and-valid-from-columns

ALTER TABLE orderable_item
ADD COLUMN valid_from_date date,
ADD COLUMN valid_to_date date;

