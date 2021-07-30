-- liquibase formatted sql
-- changeset mohamedanees:add-indexes-to-package-migrated-data

ALTER TABLE package_content_charges ADD CONSTRAINT pack_cont_charge_id_pkey PRIMARY KEY (content_charge_id);
CREATE INDEX package_content_charges_id_bt_idx ON package_content_charges(package_content_id,org_id,bed_type);
ALTER TABLE package_content_charges
	ADD CONSTRAINT package_content_charges_unique_constraint UNIQUE (package_content_id, org_id, bed_type);
