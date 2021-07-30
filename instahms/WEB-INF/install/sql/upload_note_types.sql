--
-- Upload note types from csv file.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

begin transaction;


create table temp_note_type_templates (
	note_type_id integer,
	note_type_name character varying(150),
  	assoc_hosp_role_id integer,
  	editable_by character(1) default 'O',
  	status character(1) default 'A',
  	billing_option character(1) default 'N',
  	transcribing_role_id integer,
  	created_by  character varying(100),
  	created_time timestamp without time zone DEFAULT now(),
  	mod_user character varying(100),
  	mod_time timestamp without time zone DEFAULT now() NOT NULL,
  	template_id integer,
	template_name character varying(150), 
	template_content text);

\COPY temp_note_type_templates FROM '/tmp/masters/note_types.csv' CSV HEADER;

DELETE FROM temp_note_type_templates WHERE note_type_name IN (select note_type_name from note_type_master);

INSERT INTO note_type_master (note_type_id,note_type_name,assoc_hosp_role_id,editable_by,
	status,billing_option,transcribing_role_id,created_by,mod_user) 
SELECT nextval('note_type_master_seq'),note_type_name, assoc_hosp_role_id,editable_by,
	status,billing_option,transcribing_role_id, created_by,mod_user
FROM temp_note_type_templates  
GROUP BY note_type_name,assoc_hosp_role_id, editable_by,status,billing_option,transcribing_role_id, created_by,mod_user;

INSERT INTO note_type_template_master (template_id, note_type_id, template_name,template_content) 
SELECT nextval('note_type_template_master_seq'), ntm.note_type_id,
template_name, template_content 
FROM temp_note_type_templates temp
JOIN note_type_master ntm USING (note_type_name);

DROP TABLE temp_note_type_templates;

commit;
