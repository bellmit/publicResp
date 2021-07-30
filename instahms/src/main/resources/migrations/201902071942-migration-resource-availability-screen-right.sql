-- liquibase formatted sql
-- changeset raeshmika:<migration-resource-availability-screen-right>

  INSERT INTO action_rights (role_id, action, rights) select role_id, 'block_unblock_calendar', 'A' from screen_rights where screen_id = 'res_availability' AND rights = 'A';