-- liquibase formatted sql
-- changeset sanjana.goyal:delivery-type

alter table patient_details add column delivery_type character varying(1) ;