-- liquibase formatted sql
-- changeset raeshmika:<acl-for-allow-overbook-appt-with-status>

UPDATE action_rights SET rights = 'A' WHERE action='allow_appt_overbooking' and rights='Y';