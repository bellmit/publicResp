--
-- Upload TriageScreen optional Components from a spreadsheet for initial master data.
-- this spreadsheet contains tha data of, which dept consists of which physician forms.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_triage_form_comp;
CREATE TABLE tmp_triage_form_comp (
	dept_name text NOT NULL,
	allergies text not null,
	vitals text not null,
	forms text ,
	immunization text not null
);

COPY tmp_triage_form_comp FROM '/tmp/masters/triage_comp.csv' csv header;

delete from triage_components;
update tmp_triage_form_comp set
	dept_name = trim(dept_name),
	allergies = clean_yn(allergies, 'Y'),
	vitals = clean_yn(vitals, 'Y'),
	immunization = clean_yn(immunization, 'Y');
alter table tmp_triage_form_comp add column dept_id text;

-- update the dept_id column with the deptid using filled department name.
update tmp_triage_form_comp set dept_id=d.dept_id from department d
where upper(tmp_triage_form_comp.dept_name)=upper(trim(d.dept_name));

update tmp_triage_form_comp SET dept_id = -1 where dept_name = '(All)';

INSERT INTO tmp_triage_form_comp
SELECT '(All)', 'Y','Y','','Y', '-1'
WHERE NOT EXISTS (SELECT * FROM tmp_triage_form_comp WHERE dept_id = '-1');

-- update the forms column with the formid's, replacing the formnames.
-- form name should not contain the comma(,). because we are using comma as a delimeter for the separation of form ids.
update tmp_triage_form_comp set forms=form_id from (select textcat_commacat(form_id||'') as form_id, dept_id from physician_form_desc pfd
join (select regexp_split_to_table(forms, E',') as form, dept_id from tmp_triage_form_comp) as phforms on
(upper(trim(phforms.form)) = upper(trim(pfd.form_title))) group by dept_id) as foo where foo.dept_id=tmp_triage_form_comp.dept_id;


insert into triage_components (dept_id, allergies, vitals, forms, immunization)
	select dept_id, allergies, vitals, forms, immunization
	from tmp_triage_form_comp;

update triage_components set forms=regexp_replace(forms, ' ', '', 'g');

drop table if exists tmp_triage_form_comp;