-- liquibase formatted sql
-- changeset sanjana:doctor-confirmation-eventId-change

INSERT into message_events values ('doc_appt_confirmed','Doctor Appointment Confirmend','Event used for triggering SMS to doctors for confirmed appointments');

UPDATE message_types set event_id='doc_appt_confirmed' where message_type_id='sms_appointment_confirmation_for_doctor';