-- liquibase formatted sql
-- changeset manasaparam:inc-tele-url-and-inc-filter-size

ALTER TABLE scheduler_appointments ALTER COLUMN teleconsult_url TYPE varchar(500);

ALTER TABLE event_push_subscribers_http ALTER COLUMN include_filter TYPE varchar(10000);

ALTER TABLE event_push_subscribers_http ALTER COLUMN exclude_filter TYPE varchar(10000);