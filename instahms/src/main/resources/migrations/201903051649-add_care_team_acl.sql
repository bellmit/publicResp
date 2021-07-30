-- liquibase formatted sql
-- changeset sonam009:add-remove-care-team-doctor-acl

INSERT INTO action_rights ( SELECT role_id, 'modify_care_team', 'A' FROM screen_rights
 WHERE screen_id IN('define_care_team') AND rights ='A');