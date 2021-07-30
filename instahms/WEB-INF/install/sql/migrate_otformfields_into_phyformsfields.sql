--
-- Migrate Otforms fields into phyforms fields from a spreadsheet for initial master data.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_ot_forms_into_phys_forms;
CREATE TABLE tmp_ot_forms_into_phys_forms (
	form_name text NOT NULL,
	field_name text NOT NULL,
	display_order integer,
	field_type text, allow_others text, allow_normal text, normal_text text,
	no_of_lines integer DEFAULT 3,
	option1 text, option2 text, option3 text, option4 text, option5 text,
	option6 text, option7 text, option8 text, option9 text, option10 text,
	option11 text, option12 text, option13 text, option14 text, option15 text,
	option16 text, option17 text, option18 text, option19 text, option20 text,
	option21 text, option22 text, option23 text, option24 text, option25 text,
	option26 text, option27 text, option28 text, option29 text, option30 text
);

COPY tmp_ot_forms_into_phys_forms FROM '/tmp/masters/physforms_fields_options_Ot.csv' csv header;
ALTER TABLE tmp_ot_forms_into_phys_forms ADD COLUMN field_id integer;
ALTER TABLE tmp_ot_forms_into_phys_forms ADD COLUMN form_id integer;

UPDATE tmp_ot_forms_into_phys_forms SET form_id=pfd.form_id from physician_form_desc pfd
WHERE upper(trim(pfd.form_title))=upper(trim(tmp_ot_forms_into_phys_forms.form_name));

UPDATE tmp_ot_forms_into_phys_forms SET
	field_name = trim(field_name),
	field_type = trim(lower(field_type)),
	allow_others = clean_yn(allow_others, 'Y'),
	allow_normal = clean_yn(allow_normal, 'Y'),
	normal_text = trim(normal_text);


INSERT INTO physician_form_field_desc (field_id, form_id, field_name, display_order,
	field_type, allow_others, allow_normal, normal_text, no_of_lines, status)
SELECT nextval('physician_form_field_desc_seq'), form_id, field_name, display_order,
	field_type, allow_others, allow_normal, coalesce(normal_text, ''), no_of_lines, 'A'
FROM tmp_ot_forms_into_phys_forms;

UPDATE tmp_ot_forms_into_phys_forms t SET field_id =
	(SELECT field_id FROM physician_form_field_desc
		WHERE form_id = t.form_id AND field_name = t.field_name);

DROP FUNCTION IF EXISTS phys_form_add_options();
CREATE FUNCTION phys_form_add_options() returns void as $BODY$
DECLARE
	i integer;
BEGIN
	FOR i IN 1..30 LOOP
	EXECUTE $$INSERT INTO physician_form_field_options (option_id, field_id, display_order, option_value, status)
		SELECT nextval('physician_form_field_options_seq'), field_id, $$ || i || $$, option$$ || i || $$, 'A'
		FROM tmp_ot_forms_into_phys_forms
		WHERE option$$ || i || $$ IS NOT NULL AND option$$ || i || $$ != ''$$;
	END LOOP;
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT phys_form_add_options();

DROP TABLE IF EXISTS tmp_ot_forms_into_phys_forms;
DROP FUNCTION IF EXISTS phys_form_add_options();

