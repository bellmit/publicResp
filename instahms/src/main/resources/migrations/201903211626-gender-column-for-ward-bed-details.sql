-- liquibase formatted sql
-- changeset raeshmika:<gender-column-for-ward-bed-details>

ALTER TABLE ward_names ADD COLUMN allowed_gender character varying(3) not null default 'ALL';