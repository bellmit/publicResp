-- liquibase formatted sql
-- changeset sanjana.goyal:drop-specific-message-mod

delete from modules_activated where module_id in ('mod_general_messages','mod_scheduler_messages');

alter table message_types RENAME COLUMN message_group TO obsolete_message_group; 