-- liquibase formatted sql
-- changeset sanjana:doctor-overbook-migration-maximum-limit

update doctors set overbook_limit = 99 where overbook_limit > 99;