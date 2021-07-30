-- liquibase formatted sql
-- changeset rajendratalekar:edc-pos-code-increase-to-20-chars

ALTER TABLE edc_machine_master ALTER COLUMN merchant_pos_code TYPE character varying(20);
