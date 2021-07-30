-- liquibase formatted sql
-- changeset junaidahmed:deprecates-selfpay-sponsor-types-table

ALTER TABLE sponsor_type ADD COLUMN is_selfpay_sponsor BOOLEAN DEFAULT FALSE;
update sponsor_type set is_selfpay_sponsor = true where sponsor_type_id in (select sponsor_type_id from selfpay_sponsor_types);
DROP TABLE selfpay_sponsor_types;