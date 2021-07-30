-- liquibase formatted sql
-- changeset manasaparam:changing-print-type-to-Insurance

update hosp_print_master set print_type ='Insurance' where print_type='Insurence';
