-- liquibase formatted sql
--changeset akshaySuman:complaint_type_master_seq_rename

ALTER SEQUENCE complaint_type_master_sequence RENAME TO complaint_type_master_seq;