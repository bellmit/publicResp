-- liquibase formatted sql
-- changeset rajendratalekar:rename-table-ceasarean-indication-master

ALTER TABLE ceasarean_indication_master RENAME TO indication_for_caesarean_section;

ALTER SEQUENCE ceasarean_indication_master_id_seq RENAME TO indication_for_caesarean_section_seq;