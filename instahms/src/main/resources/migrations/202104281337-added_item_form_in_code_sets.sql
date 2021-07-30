-- liquibase formatted sql
-- changeset javalkarvinay:added_item_form_in_code_sets

insert into code_system_categories values (18,'Item Form Master','A','item_form_master','item_form_name','item_form_id');
insert into code_system_categories values (19,'Ward Names','A','ward_names','ward_name','ward_name_id');


ALTER TABLE ward_names ADD COLUMN ward_name_id serial;
COMMENT ON SEQUENCE ward_names_ward_name_id_seq IS '{ "type": "Master", "comment": "Holds sequence for ward names" }';



