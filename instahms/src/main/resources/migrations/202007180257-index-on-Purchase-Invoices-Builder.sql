-- liquibase formatted sql
-- changeset eshwar-chandra:index-on-Purchase-Invoices-Builder failOnError:false

create index idx_store_grn_details_grn_no on store_grn_details(grn_no) where grn_no is not null ;

create index idx_store_grn_main_debit_note_no on store_grn_main(debit_note_no) where debit_note_no is not null ;

