--
-- Script to upload stock details from a spreadsheet
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_store_stock_details;
CREATE TABLE tmp_store_stock_details (
	dept_name text NOT NULL,
	medicine_name text NOT NULL,
	batch_no text NOT NULL,
	exp_dt date,
	mrp numeric,
	package_cp numeric NOT NULL, 
	qty numeric NOT NULL,
	consignment_stock text
);

--
-- Load the sheet
--
COPY tmp_store_stock_details FROM '/tmp/masters/stockdetails.csv' csv header;

UPDATE tmp_store_stock_details SET
	dept_name = trim(dept_name),
	medicine_name = trim(medicine_name),
	batch_no = trim(batch_no),
	consignment_stock = clean_yn(consignment_stock, 'N')
;

SELECT 'IGNORING Missing Store', t.dept_name, count(*) || ' record(s)'
FROM tmp_store_stock_details t
LEFT JOIN stores s ON (t.dept_name = s.dept_name)
WHERE s.dept_name IS NULL
GROUP BY t.dept_name;

SELECT 'IGNORING Missing Item', t.medicine_name, count(*) || ' record(s)'
FROM tmp_store_stock_details t
LEFT JOIN store_item_details im ON (t.medicine_name = im.medicine_name)
WHERE im.medicine_name IS NULL
GROUP BY t.medicine_name;

DELETE FROM store_item_lot_details;
DELETE FROM store_item_batch_details;
DELETE FROM store_stock_details;

ALTER SEQUENCE store_item_batch_details_seq RESTART 1;
ALTER SEQUENCE store_item_lot_details_seq RESTART 1;
ALTER SEQUENCE store_stock_details_seq RESTART 1;

ALTER TABLE tmp_store_stock_details ADD COLUMN medicine_id integer;
ALTER TABLE tmp_store_stock_details ADD COLUMN item_batch_id integer;
ALTER TABLE tmp_store_stock_details ADD COLUMN item_lot_id integer;

--
-- Update the medicine_id based on the name
--
UPDATE tmp_store_stock_details t SET medicine_id = sid.medicine_id
FROM store_item_details sid
WHERE sid.medicine_name = t.medicine_name;

--
-- Insert one row per unique item-batch into item-batch table.
-- Then update the item_batch_id based on medicine_id, batch_no
--
INSERT INTO store_item_batch_details (medicine_id, batch_no, mrp, exp_dt)
SELECT DISTINCT ON (medicine_id, batch_no) 
	medicine_id, batch_no, mrp, date_trunc('MONTH', exp_dt)
FROM tmp_store_stock_details
WHERE medicine_id IS NOT NULL;

UPDATE tmp_store_stock_details t SET item_batch_id = ibd.item_batch_id
FROM store_item_batch_details ibd
WHERE ibd.medicine_id = t.medicine_id AND ibd.batch_no = t.batch_no;

--
-- Insert one row per record into item-lot-details for every unique batch-package_cp
-- Then update the lot id based on item_batch_id + package_cp
--
INSERT INTO store_item_lot_details (item_batch_id, package_cp, grn_no, lot_source, purchase_type)
SELECT DISTINCT ON (item_batch_id, package_cp)
	item_batch_id, package_cp, 'INITIAL', 'S', 'S'
FROM tmp_store_stock_details
WHERE medicine_id IS NOT NULL;

UPDATE tmp_store_stock_details t SET item_lot_id = ild.item_lot_id
FROM store_item_lot_details ild
WHERE ild.item_batch_id = t.item_batch_id AND ild.package_cp = t.package_cp;

--
-- Insert one row per record into store_stock_details table
--
INSERT INTO store_stock_details
	(dept_id, medicine_id, batch_no, item_batch_id, item_lot_id, qty,
	consignment_stock, package_cp, package_uom, stock_pkg_size, username)
SELECT s.dept_id, t.medicine_id, t.batch_no, t.item_batch_id, t.item_lot_id, t.qty,
	(t.consignment_stock = 'Y'), t.package_cp, im.package_uom, im.issue_base_unit, 'admin'
FROM tmp_store_stock_details t
	JOIN stores s ON (s.dept_name = t.dept_name)
	JOIN store_item_details im ON (im.medicine_id = t.medicine_id)
WHERE t.medicine_id IS NOT NULL
;

