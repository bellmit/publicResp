-- liquibase formatted sql
-- changeset utkarshjindal:deleting-view_general_rates
DELETE FROM action_rights where action = 'view_general_rates';
