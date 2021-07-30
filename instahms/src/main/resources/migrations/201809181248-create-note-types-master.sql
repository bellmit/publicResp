-- liquibase formatted sql
-- changeset janakivg:note-types-master-dbchanges

CREATE SEQUENCE note_type_master_seq
      START WITH 1
      INCREMENT BY 1
      NO MAXVALUE
      NO MINVALUE
      CACHE 1;

CREATE TABLE note_type_master (
  note_type_id integer not null default nextval('note_type_master_seq') PRIMARY KEY,
  note_type_name character varying(150),
  assoc_hosp_role_id integer,
  editable_by character(1) default 'O',
  status character(1) default 'A',
  billing_option character(1) default 'N',
  transcribing_role_id integer,
  created_by  character varying(100),
  created_time timestamp without time zone DEFAULT now(),
  mod_user character varying(100),
  mod_time timestamp without time zone DEFAULT now() NOT NULL
) ;
