--
-- Transaction data clean-up - this script should be run to clean up all transaction data.;
-- List of tables which contains transaction data in pharmacy;

delete from store_reorder_levels;
delete from store_grn_details;
delete from store_grn_main;
delete from store_invoice;
delete from store_po;
delete from store_po_main;
delete from store_sales_details;
delete from store_sales_main;
delete from store_adj_details;
delete from store_adj_main;
delete from store_transfer_details;
delete from store_transfer_main;
delete from store_stock_details;
delete from store_issue_returns_details;
delete from store_issue_returns_main;
delete from stock_issue_details;
delete from stock_issue_main;
delete from store_checkpoint_details;
delete from store_checkpoint_main;
delete from store_indent_details;
delete from store_indent_main;
delete from store_supplier_returns;
delete from store_supplier_returns_main;
delete from store_debit_note;
delete from store_consignment_invoice;

delete from store_kit_details;
delete from store_kit_stock;
delete from store_estimate_details;
delete from store_estimate_main;
delete from store_reagent_usage_details;
delete from store_reagent_usage_main;
delete from store_retail_customers;
delete from store_retail_sponsors;



--
-- Reset transaction Sequences;
--

SELECT pg_catalog.setval('stockissue_sequence', 1, false); ;
SELECT pg_catalog.setval('stockadjust_sequence', 1, false); ;
SELECT pg_catalog.setval('grn_id_seq', 1, false); ;
SELECT pg_catalog.setval('pharmacy_medicine_sales_main_seq', 1, false); ;
SELECT pg_catalog.setval('pharmacy_medicine_sales_seq', 1, false); ;
SELECT pg_catalog.setval('retail_customer_id_sequence', 1, false); ;
SELECT pg_catalog.setval('supplier_return_sequence', 1, false); ;
SELECT pg_catalog.setval('pharmacy_medicine_category_sequence', 1, false); ;
;
;

alter sequence store_debit_note_seq restart 1 ;
alter sequence store_estimate_details_seq restart 1 ;
alter sequence store_estimate_main_seq restart 1 ;
alter sequence store_grn_debit_note_seq restart 1 ;
alter sequence store_grn_id_seq restart 1 ;
alter sequence store_indent_details_seq restart 1 ;
alter sequence store_indent_seq restart 1 ;
alter sequence store_issue_returns_sequence restart 1 ;
alter sequence store_issue_sequence restart 1 ;
alter sequence store_reagent_usage_main_seq restart 1 ;
alter sequence store_sales_details_seq restart 1 ;
alter sequence store_seq restart 1 ;
alter sequence store_user_issue_seq restart 1 ;
alter sequence po_id_seq restart 1 ;
alter sequence stock_transfer_seq restart 1 ;




