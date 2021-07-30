-- liquibase formatted sql
-- changeset asif:force-password-change

ALTER TABLE u_user ADD COLUMN force_password_change boolean DEFAULT false;
UPDATE u_user SET force_password_change = false;
