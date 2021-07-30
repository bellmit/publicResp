-- liquibase formatted sql
-- changeset janakivg:note-type-template-master-dbchanges

CREATE SEQUENCE note_type_template_master_seq
      START WITH 1
      INCREMENT BY 1
      NO MAXVALUE
      NO MINVALUE
      CACHE 1;


CREATE TABLE note_type_template_master (
  template_id integer not null default nextval('note_type_template_master_seq') PRIMARY KEY,
  note_type_id integer,
  template_name character varying(150),
  template_content  text
);
