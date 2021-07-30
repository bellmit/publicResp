-- liquibase formatted sql
-- changeset raeshmika:<package-contents-changes-for-creating-order-sets>

ALTER TABLE package_contents ADD COLUMN conduction_gap INTEGER, ADD COLUMN conduction_gap_unit varchar(20);
ALTER TABLE package_contents ADD COLUMN parent_pack_ob_id INTEGER REFERENCES package_contents(package_content_id);
