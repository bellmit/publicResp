-- liquibase formatted sql
-- changeset yashwantkumar:patient-arrived-diag_states_master
update diag_states_master set display_name = 'Patient Arrived' where value= 'MA';
