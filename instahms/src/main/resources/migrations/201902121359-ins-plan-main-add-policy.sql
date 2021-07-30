-- liquibase formatted sql
-- changeset mohamedanees:changes-to-add-policy
ALTER TABLE insurance_plan_main add column plan_code character varying(30);