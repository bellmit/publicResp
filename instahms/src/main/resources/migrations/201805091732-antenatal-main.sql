-- liquibase formatted sql
-- changeset yaskumar:antenatal-header splitStatements:false
CREATE TABLE antenatal_main(
	antenatal_main_id integer not null primary key,
	section_detail_id integer,
	lmp date,
	edd date,
	final_edd date,
	pregnancy_result character varying(100),
	pregnancy_result_date date,
	number_of_birth integer,
	pregnancy_count integer,
	remarks character varying(1000),
	created_by character varying(30),
	modified_by character varying(30),
	modified_at timestamp without time zone
);

COMMENT ON COLUMN antenatal_main.pregnancy_result IS 'MTP / Miscarriage / Delivery';
COMMENT ON COLUMN antenatal_main.pregnancy_result_date IS 'MTP date / Miscarriage date / Delivery date';

CREATE SEQUENCE antenatal_main_seq start with 1;

ALTER TABLE antenatal DROP COLUMN IF EXISTS lmp;
ALTER TABLE antenatal DROP COLUMN IF EXISTS edd;
ALTER TABLE antenatal DROP COLUMN IF EXISTS final_edd;

ALTER TABLE antenatal ADD COLUMN movement character varying(500);
ALTER TABLE antenatal ADD COLUMN position character varying(500);
ALTER TABLE antenatal ADD COLUMN antenatal_main_id integer;
ALTER TABLE antenatal ADD CONSTRAINT fk_antenatal_main_id FOREIGN KEY(antenatal_main_id) REFERENCES antenatal_main(antenatal_main_id) ON DELETE CASCADE;
ALTER TABLE antenatal rename COLUMN section_detail_id to obsolete_section_detail_id;

INSERT INTO antenatal_main (antenatal_main_id, section_detail_id, pregnancy_count)
SELECT nextval('antenatal_main_seq'), obsolete_section_detail_id, 1 from antenatal al group by obsolete_section_detail_id
HAVING NOT EXISTS (SELECT 1 FROM antenatal_main WHERE section_detail_id = al.obsolete_section_detail_id);

update antenatal al set antenatal_main_id = am.antenatal_main_id from antenatal_main am where am.section_detail_id = al.obsolete_section_detail_id;
