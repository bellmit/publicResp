-- liquibase formatted sql
-- changeset manjular:add-columns-gender-applicability-and-min-max-age

ALTER TABLE orderable_item
ADD COLUMN gender_applicability  character(1),
ADD COLUMN min_age  integer,
ADD COLUMN max_age integer,
ADD COLUMN age_unit character(1);
