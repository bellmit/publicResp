-- liquibase formatted sql
-- changeset harishm18:adding-new-action-rights-for-apply-strict-po-controls

INSERT INTO action_rights (select role_id, 'apply_strict_po_controls', 'N' from action_rights where action = 'allow_backdate');
