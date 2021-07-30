-- liquibase formatted sql
-- changeset kartikag01:add-col-child_dup_memb_id_validity_days

alter table tpa_master ADD COLUMN child_dup_memb_id_validity_days integer;