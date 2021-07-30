-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes-common-2

ALTER TABLE receipt_usage DROP CONSTRAINT  receipt_usage_pkey;
ALTER TABLE receipt_usage ADD CONSTRAINT receipt_usage_pkey PRIMARY KEY 
      (receipt_id,entity_type,entity_id);