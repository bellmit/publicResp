-- liquibase formatted sql
-- changeset prashantbaisla:add-malaffi-columns-to-u_user.sql

ALTER TABLE u_user
  ADD COLUMN user_first_name character varying(100),
  ADD COLUMN user_middle_name character varying(100),
  ADD COLUMN user_last_name character varying(100),
  ADD COLUMN user_gender character(1),
  ADD COLUMN employee_id character varying(25),
  ADD COLUMN profession character varying(100),
  ADD COLUMN employee_category character varying(100),
  ADD COLUMN employee_major character varying(100)
;

COMMENT ON COLUMN u_user.user_gender IS 'M - Male, F - Female, O - Other, NULL - Not set/Unset';
