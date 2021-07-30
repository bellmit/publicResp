-- liquibase formatted sql
-- changeset SirishaRL:fixedHMS-20182 failOnError:false
ALTER TABLE hl7_order_items ADD COLUMN doctor_mobile character varying(16),ADD COLUMN referal_mobileno character varying(15);
ALTER TABLE hl7_order_items_main ADD COLUMN package_category_id integer,ADD COLUMN tpa_id character varying(15);

ALTER TABLE hl7_order_items ADD COLUMN presc_doctor_mobile character varying(16);
ALTER TABLE hl7_order_items_main ADD COLUMN sample_type_id integer;