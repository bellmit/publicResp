--
-- Upload custom sections from csv file.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

begin transaction;

DROP TABLE IF EXISTS section_temp;
create table section_temp (
	section_title character varying,
	allow_all_normal character varying,
	linked_to character varying,
	status_s character varying,
	section_mandatory boolean,
	allow_duplicate boolean,
	display_order_f integer,
	field_name character varying,
	field_type character varying,
	allow_others character varying,
	allow_normal character varying,
	normal_text character varying,
	no_of_lines integer,
	status_f character varying,
	is_mandatory boolean,
	use_in_presenting_complaint character varying,
	default_to_current_datetime character varying,
	display_order_o integer,
	option_value character varying,
	status_o character varying,
	value_code character varying);

\COPY section_temp FROM '/tmp/masters/custom_sections.csv' CSV HEADER;

DELETE FROM section_temp WHERE section_title IN (select section_title from section_master);

INSERT INTO section_master (SELECT nextval('section_master_seq'), foo.* FROM (SELECT section_title,allow_all_normal,linked_to,status_s,section_mandatory,allow_duplicate FROM section_temp GROUP BY section_title,allow_all_normal,linked_to,status_s,section_mandatory,allow_duplicate) AS foo);

INSERT INTO section_field_desc 
(field_id, section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text, no_of_lines, status, is_mandatory, use_in_presenting_complaint, default_to_current_datetime) 
(select nextval('section_field_desc_seq'), foo.* from 
(select sm.section_id, temp.display_order_f as display_order, temp.field_name, temp.field_type,
temp.allow_others, temp.allow_normal, COALESCE(temp.normal_text, ''), temp.no_of_lines,
temp.status_f as status, temp.is_mandatory, temp.use_in_presenting_complaint,
temp.default_to_current_datetime 
from section_temp temp
join section_master sm USING (section_title)
group by sm.section_id, display_order, field_name, field_type, allow_others, allow_normal, normal_text,
no_of_lines, status_f, is_mandatory, use_in_presenting_complaint, default_to_current_datetime) as foo);

INSERT INTO section_field_options (option_id, field_id, display_order, option_value, status, value_code) 
(select nextval('section_field_options_seq'), foo.* from (select sfd.field_id,
temp.display_order_o as display_order, option_value, temp.status_o as status, temp.value_code 
from section_temp temp
join section_master sm USING (section_title)
join section_field_desc sfd ON (sfd.section_id=sm.section_id AND sfd.field_name=temp.field_name)
group by sfd.field_id, temp.display_order_o, option_value, temp.status_o, value_code
) as foo);

drop table if exists section_temp;

commit;

