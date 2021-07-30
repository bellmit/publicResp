-- liquibase formatted sql
-- changeset sanjana:updating-appointment-source

update appointment_source_master set appointment_source_name='Widget' where appointment_source_name='widget';
update appointment_source_master set appointment_source_name='Kiosk' where appointment_source_name='kiosk';