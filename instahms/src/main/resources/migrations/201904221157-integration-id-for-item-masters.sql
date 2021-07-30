-- liquibase formatted sql
-- changeset anandpatel:integration-id-for-item-masters

ALTER TABLE strength_units ADD COLUMN integration_strength_unit_id character varying(15) UNIQUE;
ALTER TABLE medicine_route ADD COLUMN integration_medicine_route_id character varying(15) UNIQUE;
