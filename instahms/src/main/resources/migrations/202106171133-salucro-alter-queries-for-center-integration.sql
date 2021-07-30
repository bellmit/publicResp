-- liquibase formatted sql
-- changeset pallavia08:salucro-alter-queries-for-center-integration failOnError:false
-- validCheckSum: ANY
ALTER TABLE center_integration_details ADD COLUMN http_header text;
ALTER TABLE center_integration_details ADD COLUMN http_request_type character varying(20);