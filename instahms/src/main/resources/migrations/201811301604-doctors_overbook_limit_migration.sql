-- liquibase formatted sql
-- changeset sanjana:doctors-overbook-limit

update doctors set overbook_limit =25 where overbook_limit is null;