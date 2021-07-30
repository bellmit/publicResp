-- liquibase formatted sql
-- changeset rajendratalekar:create-external-links-table

CREATE TABLE external_links (
	id SERIAL UNIQUE,
	label varchar(150) NOT NULL,
	link varchar(500) NOT NULL,
	center_id int REFERENCES hospital_center_master(center_id) NOT NULL,
	screen_id character varying(40) NOT NULL
);

SELECT comment_on_table_or_sequence_if_exists('external_links',true, 'Master','External Links in Screen Footers');
SELECT comment_on_table_or_sequence_if_exists('external_links_id_seq', false, 'Master','');
