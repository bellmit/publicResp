-- liquibase formatted sql
-- changeset mancini2802:created-index-tax-related-new-sub-groups-tables-items

CREATE INDEX item_sub_groups_item_group_id_idx ON item_sub_groups USING btree (item_group_id);
CREATE INDEX item_groups_item_group_id_idx ON item_groups USING btree (item_group_id);
CREATE INDEX service_item_sub_groups_item_subgroup_id_idx ON service_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX anesthesia_item_sub_groups_item_subgroup_id_idx ON anesthesia_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX theatre_item_sub_groups_item_subgroup_id_idx ON theatre_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX equipment_item_sub_groups_item_subgroup_id_idx ON equipment_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX operation_item_sub_groups_item_subgroup_id_idx ON operation_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX diagnostics_item_sub_groups_item_subgroup_id_idx ON diagnostics_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX bed_item_sub_groups_item_subgroup_id_idx ON bed_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX consultation_item_sub_groups_item_subgroup_id_idx ON consultation_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX common_item_sub_groups_item_subgroup_id_idx ON common_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX package_item_sub_groups_item_subgroup_id_idx ON package_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX dietary_item_sub_groups_item_subgroup_id_idx ON dietary_item_sub_groups USING btree (item_subgroup_id);
CREATE INDEX drg_code_item_sub_groups_item_subgroup_id_idx ON drg_code_item_sub_groups USING btree (item_subgroup_id);
