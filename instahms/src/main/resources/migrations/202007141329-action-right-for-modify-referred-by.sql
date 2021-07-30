-- liquibase formatted sql
-- changeset raeshmika:<action-right-for-modify-referred-by-details>

insert into action_rights(action,role_id,rights) select 'modify_referred_by_details', role_id,'A' from u_role;