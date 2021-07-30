-- liquibase formatted sql
-- changeset rajendratalekar:comments-for-user-preferences

COMMENT ON table user_preferences is '{ "type": "Master", "comment": "User Preferences" }';
COMMENT ON sequence user_preferences_seq is '{ "type": "Master", "comment": "" }';
