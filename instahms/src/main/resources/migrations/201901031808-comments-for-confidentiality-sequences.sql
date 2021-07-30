-- liquibase formatted sql
-- changeset adityabhatia02:comments-for-confidentiality-sequences

COMMENT ON sequence confidentiality_grp_master_seq is '{ "type": "Master", "comment": "Sequence for confidentiality groups" }';
COMMENT ON sequence user_confidentiality_association_seq is '{ "type": "Master", "comment": "Sequence for association table between user and confidentiality groups" }';