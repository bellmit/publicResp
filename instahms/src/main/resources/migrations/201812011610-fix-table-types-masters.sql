-- liquibase formatted sql
-- changeset raj-nt:fix-table-types-masters.sql 

COMMENT ON sequence diag_outsource_master_seq is '{ "type": "Master", "comment": "" }';
COMMENT ON sequence health_authority_master_seq is '{ "type": "Master", "comment": "" }';
COMMENT ON table phrase_suggestions_category_master is '{ "type": "Master", "comment": "Phrase Suggestion Category" }';
