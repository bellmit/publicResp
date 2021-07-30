-- liquibase formatted sql
-- changeset sreenivasayashwanth:rename-encounter-types-visit-name-to-TeleConsultation



update encounter_types_visits set encounter_types_visit_name = 'TeleConsultation' where encounter_types_visit_name = 'Telemedicine';
