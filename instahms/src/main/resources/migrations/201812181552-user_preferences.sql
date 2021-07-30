-- liquibase formatted sql
-- changeset akshaysuman:user-prefs

CREATE SEQUENCE user_preferences_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE user_preferences(
	preference_id INTEGER PRIMARY KEY DEFAULT(nextval('user_preferences_seq')),
	username VARCHAR(30),
	module_id VARCHAR(40),
	applicable_preferences TEXT,
	mod_time timestamp without time zone,
	created_time timestamp without time zone
);

COMMENT ON COLUMN user_preferences.module_id IS 'group for which the preference will be recorded viz scheduler,patient etc';
COMMENT ON COLUMN user_preferences.username IS 'user for which the data will get recorded';
COMMENT ON COLUMN user_preferences.applicable_preferences IS 'the values will be stored in form of strigified JSON';