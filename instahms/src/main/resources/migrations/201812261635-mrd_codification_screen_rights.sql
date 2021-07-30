-- liquibase formatted sql
-- changeset shilpanr:insert-new-screen-rights-mrd-codifcation-in-url-action-rights-table

INSERT INTO url_action_rights (SELECT role_id, 'mrd_codification', rights from url_action_rights where action_id = 'update_mrd');
