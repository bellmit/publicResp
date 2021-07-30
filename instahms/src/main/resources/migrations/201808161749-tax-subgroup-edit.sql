-- liquibase formatted sql
-- changeset anandpatel:action-right-for-editing-tax-subgroup-at-item-level-in-bill-and-sale-screen 
INSERT INTO action_rights (select role_id, 'allow_tax_subgroup_edit', 'A' from u_role);
