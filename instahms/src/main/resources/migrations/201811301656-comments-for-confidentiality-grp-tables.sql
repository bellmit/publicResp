-- liquibase formatted sql
-- changeset adityabhatia02:comments-for-confidentiality-grp-tables

COMMENT ON table confidentiality_grp_master is '{ "type": "Master", "comment": "Master for confidentiality groups" }';
COMMENT ON table user_confidentiality_association is '{ "type": "Master", "comment": "Association table between user and confidentiality groups" }';