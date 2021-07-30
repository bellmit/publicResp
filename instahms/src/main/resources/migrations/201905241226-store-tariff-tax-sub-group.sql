-- liquibase formatted sql
-- changeset anandpatel:adding-tax-subgroups-in-store-tariff-level

CREATE TABLE store_tariff_item_sub_groups (
  item_id integer NOT NULL,
  item_subgroup_id integer NOT NULL,
  store_rate_plan_id integer NOT NULL,
  PRIMARY KEY (item_id, item_subgroup_id, store_rate_plan_id)
);

COMMENT ON TABLE store_tariff_item_sub_groups IS 
	'{ "type": "Master", "comment": "Inventory Items tariff level - Tax group mapping " }';
	
INSERT INTO store_tariff_item_sub_groups (item_id, item_subgroup_id, store_rate_plan_id)
SELECT medicine_id,
       item_subgroup_id,
       store_rate_plan_id
FROM store_item_sub_groups
INNER JOIN store_rate_plans ON TRUE;
