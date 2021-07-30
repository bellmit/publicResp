-- liquibase formatted sql
-- changeset anupamamr:adding-foreign-key-constrainsts-to-the-stock-take-tables

ALTER TABLE physical_stock_take
ADD CONSTRAINT pst_store_fk
FOREIGN KEY (store_id) REFERENCES stores;

ALTER TABLE physical_stock_take_detail 
ADD CONSTRAINT pstd_physical_stock_take_fk 
FOREIGN KEY (stock_take_id) REFERENCES physical_stock_take;

ALTER TABLE physical_stock_take_detail
ADD CONSTRAINT pstd_store_item_batch_details_fk
FOREIGN KEY (item_batch_id) REFERENCES store_item_batch_details;

