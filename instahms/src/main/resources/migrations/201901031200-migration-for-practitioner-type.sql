-- liquibase formatted sql
-- changeset raeshmika:<set-practitioner-type-to-empty-when-not-mapped-to-any-type>

UPDATE doctors SET practitioner_id=null WHERE practitioner_id=0;