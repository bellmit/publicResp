-- liquibase formatted sql
-- changeset anandpatel:added-column-permissible_discount_cap-in-u_user-for-user-discount-percentage
ALTER TABLE u_user ADD COLUMN permissible_discount_cap  numeric(15,2) DEFAULT 0.00;

UPDATE u_user u SET permissible_discount_cap = 100.00
FROM action_rights ar
WHERE (u.role_id = ar.role_id AND action ='allow_discount' AND rights='A') OR u.role_id in (1,2);

ALTER TABLE bill_charge ADD COLUMN is_system_discount character varying(1) DEFAULT 'Y';
