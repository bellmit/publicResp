-- liquibase formatted sql
-- changeset raeshmika:<set-registration-on-arrival-to-obsolete>

ALTER TABLE generic_preferences rename column registration_on_arrival to obsolete_registration_on_arrival;
