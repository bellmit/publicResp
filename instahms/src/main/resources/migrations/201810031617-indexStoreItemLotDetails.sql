-- liquibase formatted sql
-- changeset utkarsh-jindal:creating-index-store_item_lot_details

CREATE INDEX silt_item_batch_id_idx ON store_item_lot_details(item_batch_id);
