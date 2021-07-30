--
-- Upload ConsultationFormComponents from a spreadsheet for initial master data.
-- this spreadsheet contains tha data of, which dept consists of which physician forms.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_consult_form_comp;
CREATE TABLE tmp_consult_form_comp (
	dept_name text NOT NULL,
	visit_type text not null,
	allergies text not null,
	triage_summary text not null,
	consultation_notes text not null,
	vitals text not null,
	forms text
);

COPY tmp_consult_form_comp FROM '/tmp/masters/consult_form_comp.csv' csv header;

delete from consultation_form_components;
update tmp_consult_form_comp set
	dept_name = trim(dept_name),
	allergies = clean_yn(allergies, 'Y'),
	triage_summary = clean_yn(triage_summary, 'Y'),
	consultation_notes = clean_yn(consultation_notes, 'Y'),
	vitals = clean_yn(vitals, 'Y'),
	visit_type = trim(lower(visit_type));
alter table tmp_consult_form_comp add column dept_id text;

-- update the dept_id column with the deptid using filled department name.
update tmp_consult_form_comp set dept_id=d.dept_id from department d
where upper(tmp_consult_form_comp.dept_name)=upper(trim(d.dept_name));

update tmp_consult_form_comp SET dept_id = -1 where dept_name = '(All)';

INSERT INTO tmp_consult_form_comp
SELECT '(All)', 'o', 'N', 'Y', 'Y', 'Y', '', '-1'
WHERE NOT EXISTS (SELECT * FROM tmp_consult_form_comp WHERE dept_id = '-1' and visit_type='o');

INSERT INTO tmp_consult_form_comp
SELECT '(All)', 'i', 'N', 'Y', 'Y', 'Y', '', '-1'
WHERE NOT EXISTS (SELECT * FROM tmp_consult_form_comp WHERE dept_id = '-1' and visit_type='i');

-- update the forms column with the formid's, replacing the formnames.
-- form name should not contain the comma(,). because we are using comma as a delimeter for the separation of form ids.
update tmp_consult_form_comp set forms=form_id from (select textcat_commacat(form_id||'') as form_id, dept_id, visit_type from physician_form_desc pfd
join (select regexp_split_to_table(forms, E',') as form, dept_id, visit_type from tmp_consult_form_comp) as phforms on
(upper(trim(phforms.form)) = upper(trim(pfd.form_title))) group by dept_id, visit_type) as foo
where foo.dept_id=tmp_consult_form_comp.dept_id and foo.visit_type=tmp_consult_form_comp.visit_type;


insert into consultation_form_components (dept_id, allergies, triage_summary, consultation_notes, vitals, forms,
	visit_type) select dept_id, allergies, triage_summary, consultation_notes, vitals, forms, visit_type
	from tmp_consult_form_comp;

update consultation_form_components set forms=regexp_replace(forms, ' ', '', 'g');

drop table if exists tmp_consult_form_comp;