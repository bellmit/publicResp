-- liquibase formatted sql
-- changeset adityabhatia02:add-sequences-primary-key-user-confidentiality

CREATE SEQUENCE confidentiality_grp_master_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE user_confidentiality_association_seq START WITH 1 INCREMENT BY 1;
