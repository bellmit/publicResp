-- liquibase formatted sql
-- changeset ritolia1:adding-new-action-rights-for-finalizing-bill-and-bill-order-activities

INSERT INTO action_rights (select role_id, 'modify_bill_finalized_date', rights from action_rights where action = 'allow_backdate');
INSERT INTO action_rights (select role_id, 'allow_back_date_bill_activities', rights from action_rights where action = 'allow_backdate');
