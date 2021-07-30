-- liquibase formatted sql
-- changeset shilpanr:index_to_improve_purchase_invoice_report_view

CREATE INDEX store_grn_main_store_id_idx ON store_grn_main(store_id);
CREATE INDEX store_grn_main_grn_date_idx ON store_grn_main(grn_date);
CREATE INDEX store_invoice_po_no_idx ON store_invoice(po_no);
