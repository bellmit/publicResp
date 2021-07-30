-- liquibase formatted sql
-- changeset manasaparam:event-push-subscriber-details

ALTER TABLE api_clients ADD COLUMN retry_duration_min INTEGER;
ALTER TABLE api_clients ADD COLUMN retry_count INTEGER;
ALTER TABLE api_clients ADD COLUMN custom_header VARCHAR(200);

CREATE TABLE event_push_subscribers_http (
	id INTEGER NOT NULL PRIMARY KEY,
	client_id INTEGER NOT NULL,
	event VARCHAR(50),
	include_filter VARCHAR(200),
	exclude_filter VARCHAR(200),
	FOREIGN KEY (client_id) REFERENCES api_clients(client_id)
);

COMMENT ON table event_push_subscribers_http is '{ "type": "Master", "comment": "Subscriber-Event details" }';