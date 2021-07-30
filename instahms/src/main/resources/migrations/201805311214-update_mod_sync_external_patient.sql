-- liquibase formatted sql
-- changeset kchetan:update_mod_sync_external_patient

UPDATE modules_activated set activation_status = 'N' where module_id= 'mod_sync_external_patient';
