-- liquibase formatted sql
-- changeset mohamedanees:section-type-changes-for-accumed

CREATE TABLE section_type_master (
	section_type_id INTEGER PRIMARY KEY,
	section_type character varying (100) NOT NULL
);

COMMENT ON TABLE section_type_master is '{ "type": "Txn", "comment": "Section types for Accumed. Multiple sections in section master can be mapped to a section type" }';

INSERT INTO section_type_master (section_type_id, section_type)
	VALUES (1, 'Past History');
	
INSERT INTO section_type_master (section_type_id, section_type)
	VALUES (2, 'Main Symptoms');

INSERT INTO section_type_master (section_type_id, section_type)
	VALUES (3, 'Physical Exam');

ALTER TABLE section_master
	ADD COLUMN section_type_id INTEGER REFERENCES section_type_master (section_type_id);
