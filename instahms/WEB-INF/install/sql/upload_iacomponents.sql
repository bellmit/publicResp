--
-- Upload TriageScreen optional Components from a spreadsheet for initial master data.
-- this spreadsheet contains tha data of, which dept consists of which physician forms.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_assessment_comp;
CREATE TABLE tmp_assessment_comp (
	dept_name text NOT NULL,
	vitals text not null,
	forms text
);

COPY tmp_assessment_comp FROM '/tmp/masters/iacomponents.csv' csv header;

delete from assessment_components;
update tmp_assessment_comp set
	vitals = clean_yn(vitals, 'Y'),
	dept_name = trim(dept_name);
alter table tmp_assessment_comp add column dept_id text;

-- update the dept_id column with the deptid using filled department name.
update tmp_assessment_comp set dept_id=d.dept_id from department d
where upper(tmp_assessment_comp.dept_name)=upper(trim(d.dept_name));

update tmp_assessment_comp SET dept_id = -1 where dept_name = '(All)';

INSERT INTO tmp_assessment_comp
SELECT '(All)', 'Y', '-1'
WHERE NOT EXISTS (SELECT * FROM tmp_assessment_comp WHERE dept_id = '-1');

-- update the forms column with the formid's, replacing the formnames.
-- form name should not contain the comma(,). because we are using comma as a delimeter for the separation of form ids.
update tmp_assessment_comp set forms=form_id from (select textcat_commacat(form_id||'') as form_id, dept_id from physician_form_desc pfd
join (select regexp_split_to_table(forms, E',') as form, dept_id from tmp_assessment_comp) as phforms on
(upper(trim(phforms.form)) = upper(trim(pfd.form_title))) group by dept_id) as foo where foo.dept_id=tmp_assessment_comp.dept_id;


insert into assessment_components (dept_id, vitals, forms)
	select dept_id, vitals, forms
	from tmp_assessment_comp;

update assessment_components set forms=regexp_replace(forms, ' ', '', 'g');

drop table if exists tmp_assessment_comp;