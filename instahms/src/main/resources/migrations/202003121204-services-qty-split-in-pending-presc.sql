-- liquibase formatted sql
-- changeset krishnasameerachanta:services-qty-split-in-pending-presc


ALTER TABLE services ADD COLUMN qty_split_in_pending_presc character varying(1) NOT NULL DEFAULT 'N';
