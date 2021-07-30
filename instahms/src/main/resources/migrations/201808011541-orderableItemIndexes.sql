-- liquibase formatted sql
-- changeset utkarshjindal:creating-orderable-item-indexes

create index orderable_item_status_idx on orderable_item (status);
create index orderable_item_visit_type_idx on orderable_item (visit_type);
create index orderable_item_orderable_idx on orderable_item (orderable);
create index mapping_tpa_id_tpa_id_idx on mapping_tpa_id (tpa_id);
create index mapping_tpa_id_status_idx on mapping_tpa_id (status);
create index mapping_center_id_status_idx on mapping_center_id (status);
create index mapping_center_id_center_id_idx on mapping_center_id (center_id);
create index mapping_org_id_status_idx on mapping_org_id (status);
create index mapping_org_id_org_id_idx on mapping_org_id (org_id);
