-- liquibase formatted sql
-- changeset prashantbaisla:fix-doctor_consultation_tokens-comments

SELECT comment_on_table_or_sequence_if_exists('doctor_consultation_tokens', true, 'Master', 'Doctor wise Consultation Tokens for the day');

