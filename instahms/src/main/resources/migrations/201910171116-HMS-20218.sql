-- liquibase formatted sql
-- changeset sirisharl:<storing_cost_value_for_grn_and_debit_note_migrating-one-year_grns>

ALTER TABLE store_grn_details ADD COLUMN cost_value numeric;

UPDATE store_grn_details g  SET cost_value = (sgd.billed_qty/sid.issue_base_unit*sgd.cost_price-sgd.discount+sgd.tax+sgd.item_ced) 
	FROM store_grn_details sgd
	JOIN store_grn_main sgm ON( sgm.grn_no = sgd.grn_no )
	JOIN store_item_details sid ON(sid.medicine_id = sgd.medicine_id)
WHERE sgd.grn_no = g.grn_no and sgd.item_batch_id = g.item_batch_id;
	