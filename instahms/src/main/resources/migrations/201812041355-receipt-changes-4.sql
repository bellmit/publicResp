-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes-4

ALTER TABLE receipts ALTER COLUMN paid_by TYPE VARCHAR(100);