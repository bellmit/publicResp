-- liquibase formatted sql
-- changeset amolb2101:default-grn-no-sequence-pref-record
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:null select pattern_id from hosp_grn_seq_prefs where pattern_id = 'GRN_DEFAULT' and priority=1000

INSERT INTO hosp_grn_seq_prefs (grn_number_seq_id, priority, store_id, pattern_id) VALUES (nextval('hosp_grn_seq_prefs_seq'), 1000, '*', 'GRN_DEFAULT');
