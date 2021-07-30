-- liquibase formatted sql
-- changeset rajendratalekar:increase-message-type-subject-300-chars

alter table message_types alter column message_subject type varchar(300);