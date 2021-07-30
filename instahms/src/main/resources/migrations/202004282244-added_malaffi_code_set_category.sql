-- liquibase formatted sql
-- changeset javalkarvinay:added_malaffi_code_set_category

UPDATE code_system_categories SET label='country_master(nationality)' where id=6;
INSERT INTO code_system_categories VALUES (8,'country_master');
