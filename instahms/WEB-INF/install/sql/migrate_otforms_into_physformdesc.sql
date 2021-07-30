--
-- Migrate OT fomrs into Physician forms from a spreadsheet for initial master data.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_otforms_phys_forms_desc;
CREATE TABLE tmp_otforms_phys_forms_desc (
	form_name text NOT NULL,
	allow_all_normal text not null,
	linked_to text not null
);

COPY tmp_otforms_phys_forms_desc FROM '/tmp/masters/otforms_physforms_desc.csv' csv header;

update tmp_otforms_phys_forms_desc set
	form_name=trim(form_name),
	allow_all_normal=clean_yn(allow_all_normal, 'Y'),
	linked_to=trim(lower(linked_to));


INSERT INTO physician_form_desc (form_id, form_title, allow_all_normal, linked_to, status)
SELECT nextval('physician_form_desc_seq'), form_name, clean_yn(allow_all_normal, 'Y'), linked_to, 'A'
FROM tmp_otforms_phys_forms_desc;

drop table if exists tmp_otforms_phys_forms_desc;