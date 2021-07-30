-- liquibase formatted sql
-- changeset rajendratalekar:sso-user-support
ALTER TABLE u_user ADD COLUMN sso_only_user BOOLEAN DEFAULT false; 