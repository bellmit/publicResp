-- liquibase formatted sql
-- changeset yaminipagaria:adding-admission-request-note-type-in-note_type_master

INSERT INTO note_type_master (SELECT nextval('note_type_master_seq'),'Admission Request Notes',-1,'O','A','N', null,'InstaAdmin',now(),'InstaAdmin',now() WHERE NOT EXISTS ( SELECT 1 FROM note_type_master WHERE note_type_name='Admission Request Notes'));
