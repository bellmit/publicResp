-- liquibase formatted sql
-- changeset amolb2101:default-po-sequence-no-pref-record
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:null select pattern_id from hosp_po_seq_prefs where pattern_id = 'PO_DEFAULT' and priority=1000

INSERT INTO hosp_po_seq_prefs (po_seq_id, priority, store_id, pattern_id) VALUES (nextval('hosp_po_seq_prefs_seq'), 1000, '*', 'PO_DEFAULT');
