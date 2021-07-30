-- liquibase formatted sql
-- changeset goutham005:indices-for-consumable-related-tables

CREATE INDEX store_reagent_usage_main_date_time_idx on store_reagent_usage_main (CAST(date_time AS DATE));
CREATE INDEX store_reagent_usage_details_reagent_usage_seq_idx ON store_reagent_usage_details (reagent_usage_seq);
CREATE INDEX diagnostics_reagents_reagent_id_idx ON diagnostics_reagents (reagent_id);
CREATE INDEX service_consumables_service_id_idx ON service_consumables (service_id);
CREATE INDEX service_consumables_consumable_id_idx ON service_consumables (consumable_id);
CREATE INDEX ot_consumables_operation_id_idx ON ot_consumables(operation_id);
CREATE INDEX ot_consumables_consumable_id_idx ON ot_consumables(consumable_id);
