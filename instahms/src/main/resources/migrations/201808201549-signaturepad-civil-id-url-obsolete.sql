-- liquibase formatted sql

-- changeset qwewrty1:signaturepad-civil-id-url-obsolete

ALTER TABLE generic_preferences RENAME COLUMN signature_device_ip TO signature_device_ip_obsolete;

ALTER TABLE center_preferences RENAME COLUMN government_id_provider TO government_id_provider_obsolete;

ALTER TABLE center_preferences RENAME COLUMN government_id_provider_url TO government_id_provider_url_obsolete;
