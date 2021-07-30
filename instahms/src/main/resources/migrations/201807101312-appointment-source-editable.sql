-- liquibase formatted sql
-- changeset sanjana:appointment-source-editable

alter table appointment_source_master add column editable varchar(1) default 'Y';

update appointment_source_master set editable = 'N' where appointment_source_name in ('Kiosk','Widget','Practo');